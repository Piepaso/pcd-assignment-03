package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random
import AlarmProtocol.Zone.*

object App:

  private case class Event[E](delay: FiniteDuration, device: ActorRef[E], msg: E)

  def apply(): Behavior[Unit] = Behaviors.setup: context =>

    def simulatePhysicalEvent[E] (e: Event[E]): Unit = e match
      case Event(delay, device, msg) =>
        val _ = context.spawn(
          Behaviors.withTimers[Unit]: timers =>
            timers.startSingleTimer((), delay)
            Behaviors.receiveMessage: _ =>
              device ! msg
              Behaviors.stopped
          ,
          Random().nextInt().toString
        )

    val motion1 = context.spawn(Sensor("motion1"), "motion1")
    val motion2 = context.spawn(Sensor("motion2"), "motion2")
    val window1 = context.spawn(Sensor("window1"), "window1")
    val alarmController = AlarmController("123", Map(
        "motion1" -> (Perimeter, motion1),
        "motion2" -> (LivingRoom, motion2),
        "window1" -> (Bedroom, window1)
    ))
    val controller = context.spawn(alarmController(), "alarm-controller")
    val keypad = context.spawn(Keypad(controller), "keypad")


    List(
      Event(1.seconds, keypad, Keypad.TypeDigit('1')),
      Event(2.seconds, keypad, Keypad.TypeDigit('2')),
      Event(3.seconds, keypad, Keypad.TypeDigit('3')),
      Event(4.seconds, keypad, Keypad.Enter),
      Event(6.seconds, motion2, Sensor.Trigger),
      Event(10.seconds, window1, Sensor.Trigger),
      Event(11.seconds, keypad, Keypad.TypeDigit('1')),
      Event(12.seconds, keypad, Keypad.TypeDigit('2')),
      Event(13.seconds, keypad, Keypad.TypeDigit('3')),
      Event(14.seconds, keypad, Keypad.Enter),
      Event(15.seconds, keypad, Keypad.SelectZone(LivingRoom)),
      Event(15.seconds, keypad, Keypad.SelectZone(Kitchen)),
      Event(16.seconds, keypad, Keypad.Enter),
      Event(17.seconds, keypad, Keypad.TypeDigit('1')),
      Event(18.seconds, keypad, Keypad.TypeDigit('2')),
      Event(19.seconds, keypad, Keypad.TypeDigit('3')),
      Event(20.seconds, keypad, Keypad.Enter),
      Event(22.seconds, keypad, Keypad.TypeDigit('1')),
      Event(23.seconds, keypad, Keypad.Enter),
      Event(26.seconds, window1, Sensor.Trigger),
      Event(27.seconds, motion2, Sensor.Trigger),
      Event(32.seconds, keypad, Keypad.TypeDigit('1')),
      Event(33.seconds, keypad, Keypad.TypeDigit('2')),
      Event(34.seconds, keypad, Keypad.TypeDigit('3')),
      Event(35.seconds, keypad, Keypad.Enter),
    ).foreach(simulatePhysicalEvent)

    Behaviors.empty

  @main def run(): Unit =

    println("Starting Smart Home Alarm system ...")
    val _ = ActorSystem(App(), "SmartHomeSystem")