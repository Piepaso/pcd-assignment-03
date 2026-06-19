package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import smartHomeAlarmSystem.AlarmProtocol.*

object Sensor:
  
  enum Event:
    case Trigger
    case SwitchOn(replyTo: ActorRef[Message])
    case SwitchOff

  export Event.*

  def apply(id: String): Behavior[Event] = off(id)

  private def off(id: String): Behavior[Event] = Behaviors.receiveMessagePartial:
    case SwitchOn(replyTo) =>
      println(s"[Sensor $id] is now ON.")
      on(id, replyTo)

  private def on(id: String, replyTo: ActorRef[Message]): Behavior[Event] = Behaviors.receiveMessagePartial:
    case Trigger =>
      println(s"[Sensor $id] triggered")
      replyTo ! SensorTriggered(id)
      Behaviors.same
    case SwitchOff =>
        println(s"[Sensor $id] is now OFF.")
        off(id)