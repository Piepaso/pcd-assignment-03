package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.ActorRef

object AlarmProtocol:

  enum Zone:
    case LivingRoom
    case Kitchen
    case Bedroom
    case Perimeter
    
  trait Command
  
  enum Message extends Command:
    case PinEntered(pin: String, replyTo: ActorRef[DisplayMessage])
    case SelectZones(zones: Set[Zone], replyTo: ActorRef[DisplayMessage])
    case SensorTriggered(id: String)

  case class DisplayMessage(msg: String)
  
  enum Timeout extends Command:
    case ExitTimeout
    case EntryTimeout
    
  export Zone.*, Message.*, Timeout.*