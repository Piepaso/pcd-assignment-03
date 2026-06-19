package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.*
import AlarmProtocol.*

import scala.concurrent.duration.*

class AlarmController(correctPin: String, sensors: Map[String, (Zone, ActorRef[Sensor.Event])]):

  private val exitDelayDuration = 5.seconds
  private val entryDelayDuration = 5.seconds

  def apply(): Behavior[Command] = Behaviors.setup: ctx =>
    Behaviors.withTimers: timers =>
      disarmed(Zone.values.toSet, timers, ctx)

  private def createState(ctx: ActorContext[Command], behavior: Behavior[Command]): Behavior[Command] =
    Behaviors.receiveMessage:
      case PinEntered(pin, replyTo) if pin != correctPin =>
        replyTo ! DisplayMessage("Invalid PIN entered.")
        Behaviors.same
      case msg => Behavior.interpretMessage(behavior, ctx, msg)

  private def messageToSensors(zonesToArm: Set[Zone], message: Sensor.Event): Unit =
    sensors.foreach((_, sensor) =>
      if zonesToArm.contains(sensor._1) then sensor._2 ! message
    )

  private def log(m: String): Unit = println("[Controller    ] " + m)

  private def disarmed(zonesToArm: Set[Zone], timers: TimerScheduler[Command], ctx: ActorContext[Command]): Behavior[Command] =
    createState(ctx, Behaviors.receiveMessagePartial:
      case PinEntered(_, replyTo) =>
        replyTo ! DisplayMessage("Starting exit delay...")
        timers.startSingleTimer(ExitTimeout, ExitTimeout, exitDelayDuration)
        exitDelay(zonesToArm, timers, ctx)

      case SelectZones(zones, replyTo) =>
        replyTo ! DisplayMessage(s"Selected zones updated for next arming: ${zones.mkString(", ")}")
        disarmed(zones, timers, ctx)
    )

  private def exitDelay(zonesToArm: Set[Zone], timers: TimerScheduler[Command], ctx: ActorContext[Command]): Behavior[Command] =
    createState(ctx, Behaviors.receiveMessagePartial:
      case ExitTimeout =>
        log(s"Exit delay timed out. System ARMED for zones: ${zonesToArm.mkString(", ")}")
        messageToSensors(zonesToArm, Sensor.Event.SwitchOn(ctx.self))
        armed(zonesToArm, timers, ctx)

      case PinEntered(_, replyTo) =>
        replyTo ! DisplayMessage("Exit delay cancelled. System remains disarmed.")
        timers.cancel(ExitTimeout)
        disarmed(zonesToArm, timers, ctx)

      case SensorTriggered(id) =>
        log(s"Sensor triggered in zone [${sensors(id)._1}] during exit delay. Event ignored.")
        Behaviors.same
    )

  private def armed(zonesToArm: Set[Zone], timers: TimerScheduler[Command], ctx: ActorContext[Command]): Behavior[Command] =
    createState(ctx, Behaviors.receiveMessagePartial:
      case SensorTriggered(id) if zonesToArm.contains(sensors(id)._1) =>
        log(s"INTRUSION DETECTED in active zone [${sensors(id)._1}]! Starting entry delay...")
        timers.startSingleTimer(EntryTimeout, EntryTimeout, entryDelayDuration)
        entryDelay(zonesToArm, timers, ctx)

      case PinEntered(_, replyTo) =>
        replyTo ! DisplayMessage("System disarmed successfully.")
        disarmed(zonesToArm, timers, ctx)
    )

  private def entryDelay(zonesToArm: Set[Zone], timers: TimerScheduler[Command], ctx: ActorContext[Command]): Behavior[Command] =
    createState(ctx, Behaviors.receiveMessagePartial:
      case PinEntered(_, replyTo) =>
        replyTo ! DisplayMessage("Alarm deactivated during entry delay.")
        messageToSensors(zonesToArm, Sensor.Event.SwitchOff)
        timers.cancel(EntryTimeout)
        disarmed(zonesToArm, timers, ctx)

      case EntryTimeout =>
        log(s"Entry delay timed out! EMERGENCY: Activating alarm!")
        alarmActive(zonesToArm, timers, ctx)
    )

  private def alarmActive(zonesToArm: Set[Zone], timers: TimerScheduler[Command], ctx: ActorContext[Command]): Behavior[Command] =
    log(s"ALARM! Digit the correct PIN to disarm.")
    createState(ctx, Behaviors.receiveMessagePartial:
      case PinEntered(_, replyTo) =>
        replyTo ! DisplayMessage("Alarm deactivated.")
        disarmed(zonesToArm, timers, ctx)
    )