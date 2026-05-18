package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}

object App:
  import AlarmProtocol.Zone.*
  
  def apply(): Behavior[Unit] = Behaviors.setup: context =>
    val controller = context.spawn(AlarmController(), "CentraleAllarme")

    val keypad = context.spawn(KeypadActor(controller), "TastierinoFisico")
    keypad ! KeypadActor.StartListening

    val _ = context.spawn(SensorActor("PortaPrincipale", LivingRoom, controller), "Sensore_Porta_Giorno")
    val _ = context.spawn(SensorActor("FinestraSalotto", LivingRoom, controller), "Sensore_Salotto_Giorno")
    val _ = context.spawn(SensorActor("VeluxMansarda", Bedroom, controller), "Sensore_Mansarda_Notte")
    val _ = context.spawn(SensorActor("CorridoioCamere", Bedroom, controller), "Sensore_Corridoio_Notte")
    val _ = context.spawn(SensorActor("InfrarossoGiardino", Garage, controller), "Sensore_Giardino")

    Behaviors.empty

  @main def run(): Unit =
    println("Avvio del Sistema Smart Home Alarm...")
    val _ = ActorSystem(App(), "SmartHomeSystem")