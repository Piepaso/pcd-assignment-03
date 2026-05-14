package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration.*

object AlarmController:
  import AlarmProtocol._

  val CorrectPin = "1234"

  def apply(): Behavior[Command] = disarmed()

  private def disarmed(): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == CorrectPin =>
      println("Starting exit delay...")
      Behaviors.withTimers: timers =>
        timers.startSingleTimer(Timeout.Exit, Timeout.Exit, 30.seconds) // rewrite active timer with same key
        exitDelay()
    case _ => Behaviors.same

  private def exitDelay(): Behavior[Command] = Behaviors.receiveMessage:
    case Timeout.Exit =>
      println("Alarm armed.")
      armed()
    case PinEntered(pin) if pin == CorrectPin =>
      println("Exit delay cancelled. Alarm remains disarmed.")
      disarmed()
    case _ => Behaviors.same

  private def armed(): Behavior[Command] = Behaviors.receiveMessage:
    case SensorTriggered(zone) =>
      println(s"Movement detected in $zone. Starting entry delay...")
      Behaviors.withTimers: timers =>
        timers.startSingleTimer(Timeout.Entry, Timeout.Entry, 20.seconds)
        entryDelay()
    case PinEntered(pin) if pin == CorrectPin =>
      disarmed()
    case _ => Behaviors.same

  private def entryDelay(): Behavior[Command] = Behaviors.receiveMessage:
    case PinEntered(pin) if pin == CorrectPin =>
      println("Alarm deactivated during entry delay.")
      disarmed()
    case Timeout.Entry =>
      println("Timeout! Activating alarm...")
      alarmActive()
    case _ => Behaviors.same

  private def alarmActive(): Behavior[Command] =
    println("ALARM! Digit the correct PIN to disarm.")
    Behaviors.receiveMessage:
      case PinEntered(pin) if pin == CorrectPin =>
        println("Alarm stopped.")
        disarmed()
      case _ => Behaviors.same