package smartHomeAlarm

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import smartHomeAlarm.AlarmProtocol.*

object Keypad:
  enum Event:
    case TypeDigit(digit: Char)
    case SelectZone(zone: Zone)
    case Enter
    case Message(msg: String)

  export Event.*

  def apply(controller: ActorRef[Command]): Behavior[Event] = Behaviors.setup: ctx =>
    val messageAdapter = ctx.messageAdapter[AlarmProtocol.Message](msg => Message(msg.toString))
    idle(controller, messageAdapter)


  private def idle(controller: ActorRef[Command], replyTo: ActorRef[AlarmProtocol.Message]): Behavior[Event] =
    Behaviors.receiveMessagePartial:
      case TypeDigit(digit) if digit.isDigit =>
        typingPin(controller, digit.toString, replyTo)
      case SelectZone(zone) =>
        selectingZones(controller, Set(zone), replyTo)
      case Message(msg) =>
        println(s"[Keypad display]: $msg")
        Behaviors.same

  private def typingPin(controller: ActorRef[Command], currentPin: String, replyTo: ActorRef[AlarmProtocol.Message]): Behavior[Event] =
    Behaviors.receiveMessagePartial:
      case TypeDigit(digit) if digit.isDigit =>
        typingPin(controller, currentPin + digit, replyTo)
      case Enter =>
        controller ! AlarmProtocol.PinEntered(currentPin, replyTo)
        idle(controller, replyTo)

  private def selectingZones(controller: ActorRef[Command], currentZones: Set[Zone], replyTo: ActorRef[AlarmProtocol.Message]): Behavior[Event] =
    Behaviors.receiveMessagePartial:
      case SelectZone(zone) =>
        val updatedZones = if currentZones.contains(zone) then currentZones - zone else currentZones + zone
        selectingZones(controller, updatedZones, replyTo)
      case Enter =>
        controller ! AlarmProtocol.SelectZones(currentZones, replyTo)
        idle(controller, replyTo)