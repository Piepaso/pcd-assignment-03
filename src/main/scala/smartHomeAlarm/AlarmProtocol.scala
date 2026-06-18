package smartHomeAlarm

import org.apache.pekko.actor.typed.ActorRef

object AlarmProtocol:

  enum Zone:
    case LivingRoom
    case Kitchen
    case Bedroom
    case Perimeter
    
  trait Command
  
  enum Message extends Command:
    case PinEntered(pin: String, replyTo: ActorRef[Message])
    case SelectZones(zones: Set[Zone], replyTo: ActorRef[Message])
    case SensorTriggered(zone: Zone)
    case Reply(message: String)

  enum Timeout extends Command:
    case ExitTimeout
    case EntryTimeout
    
  export Zone.*, Message.*, Timeout.*