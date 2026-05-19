package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import smartHomeAlarmSystem.AlarmProtocol.*

object Sensor:
  
  enum Event:
    case Trigger
    case Toggle

  export Event.*

  def apply(id: String, zone: Zone, controller: ActorRef[Command], on: Boolean = false): Behavior[Event] =
    Behaviors.receiveMessage:
      case Trigger if on =>
        controller ! SensorTriggered(zone)
        Behaviors.same
      case Toggle => apply(id, zone, controller, !on)
      case _ => Behaviors.same