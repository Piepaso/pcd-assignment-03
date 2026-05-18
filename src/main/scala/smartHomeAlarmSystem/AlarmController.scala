package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.*

object AlarmController:
  import AlarmProtocol.*

  private val correctPin = "1234"

  def apply(): Behavior[Command] = disarmed()

  private def disarmed(): Behavior[Command] =
    var zonesToArm: Set[Zone] = Zone.values.toSet
    
    Behaviors.receiveMessage:
      case PinEntered(pin) if pin == correctPin =>
        println("Starting exit delay...")
        Behaviors.withTimers: timers =>
          timers.startSingleTimer(Timeout.Exit, Timeout.Exit, 30.seconds) // rewrite active timer with same key
          exitDelay(zonesToArm)
      case SelectZones(zones) =>
        zonesToArm = zones
        Behaviors.same
      case _ => Behaviors.same

  private def exitDelay(zonesToArm: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case Timeout.Exit =>
      println("Alarm armed.")
      armed(zonesToArm)
    case PinEntered(pin) if pin == correctPin =>
      println("Exit delay cancelled. Alarm remains disarmed.")
      Behaviors.withTimers:
        timers => timers.cancel(Timeout.Exit)
        disarmed()
    case _ => Behaviors.same

  private def armed(zonesToArm: Set[Zone]): Behavior[Command] = Behaviors.receiveMessage:
    case SensorTriggered(zone) if zonesToArm.contains(zone) =>
      println(s"Movement detected in $zone. Starting entry delay...")
      Behaviors.withTimers: timers =>
        timers.startSingleTimer(Timeout.Entry, Timeout.Entry, 20.seconds)
        entryDelay()
    case PinEntered(pin) if pin == correctPin =>
      disarmed()
    case _ => Behaviors.same

  private def entryDelay(): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == correctPin =>
      println("Alarm deactivated during entry delay.")
      disarmed()
    case Timeout.Entry =>
      println("Timeout! Activating alarm...")
      alarmActive()
    case _ => Behaviors.same

  private def alarmActive(): Behavior[Command] =
    println("ALARM! Digit the correct PIN to disarm.")
    Behaviors.receiveMessage:
      case PinEntered(pin) if pin == correctPin =>
        println("Alarm stopped.")
        disarmed()
      case _ => Behaviors.same