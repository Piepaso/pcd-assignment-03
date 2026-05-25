package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

object App:
  import AlarmProtocol.Zone.*
  
  def apply(): Behavior[Unit] = Behaviors.setup: context =>

    def simulatePhysicalEvent[E](event: E, device: ActorRef[E], delay: FiniteDuration): Unit =
      val _ = context.spawn(
        Behaviors.withTimers[E]: timers =>
          timers.startSingleTimer(event, delay)
          Behaviors.receiveMessage: e =>
            device ! e
            Behaviors.stopped
          ,
        Random().nextInt().toString
      )

    val controller = context.spawn(AlarmController(), "alarm-controller")

    val keypad = context.spawn(Keypad(controller), "keypad")

    val door = context.spawn(Sensor("door", LivingRoom, controller), "door-sensor")
    val _ = context.spawn(Sensor("window", LivingRoom, controller), "window-sensor")
    val _ = context.spawn(Sensor("motion", Perimeter, controller), "motion-sensor")
    val _ = context.spawn(Sensor("smoke", Kitchen, controller), "smoke-sensor")
    val bedroom = context.spawn(Sensor("bedroom-window", Bedroom, controller), "bedroom-window-sensor")
    val simulationEvents = List(
      (Keypad.TypeDigit('1'), keypad, 5.seconds),
      (Keypad.TypeDigit('2'), keypad, 6.seconds),
      (Keypad.TypeDigit('3'), keypad, 7.seconds),
      (Keypad.Enter, keypad, 8.seconds),
      (Sensor.Trigger, door, 10.seconds),
      (Sensor.Trigger, bedroom, 15.seconds),
      (Keypad.TypeDigit('1'), keypad, 16.seconds),
      (Keypad.TypeDigit('2'), keypad, 17.seconds),
      (Keypad.TypeDigit('3'), keypad, 18.seconds),
      (Keypad.Enter, keypad, 19.seconds)
    )
    simulationEvents.foreach(t => simulatePhysicalEvent(t._1, t._2, t._3))
    
    Behaviors.empty

  @main def run(): Unit =
    println("Starting Smart Home Alarm system ...")
    val _ = ActorSystem(App(), "SmartHomeSystem")