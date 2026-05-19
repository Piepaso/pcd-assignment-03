package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import smartHomeAlarmSystem.AlarmProtocol.Command

object Keypad:
  enum Event:
    case TypeDigit(digit: Char)
    case Enter
    
  export Event.*

  def apply(controller: ActorRef[Command], currentPin: String = ""): Behavior[Event] = Behaviors.receiveMessage:
    case TypeDigit(digit) if digit.isDigit =>
      apply(controller, currentPin + digit)
    case Enter =>
      controller ! AlarmProtocol.PinEntered(currentPin)
      apply(controller)
    case _ =>
      Behaviors.same