package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContextExecutor
import java.time.Duration
import scala.util.Random

object SensorActor:
  case object SimulateDetection

  def apply(sensorId: String, zone: AlarmProtocol.Zone, controller: ActorRef[AlarmProtocol.Command]): Behavior[SimulateDetection.type] =
    Behaviors.setup: context =>
      implicit val ec: ExecutionContextExecutor = context.executionContext

      val randomDelay =Duration.ofSeconds(5 + Random.nextInt(5))

      println(s"[Sensore] Avviato '$sensorId' nella zona '$zone' (genererà eventi ogni $randomDelay)")

      val _ = context.system.scheduler.scheduleWithFixedDelay(
        randomDelay,
        randomDelay,
        () => context.self ! SimulateDetection,
        ec
      )

      Behaviors.receiveMessage:
        case SimulateDetection =>
          println(s"[Sensore $sensorId] !! Movimento rilevato autonomamente nella zona: $zone !!")
          controller ! AlarmProtocol.SensorTriggered(zone)
          Behaviors.same