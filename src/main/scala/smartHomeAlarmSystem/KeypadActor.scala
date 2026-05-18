package smartHomeAlarmSystem

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}

import java.io.BufferedReader
import java.io.InputStreamReader

object KeypadActor:
  case object StartListening

  def apply(controller: ActorRef[AlarmProtocol.Command]): Behavior[StartListening.type] =
    Behaviors.setup { context =>

      def readConsole(): Unit =
        val reader = new BufferedReader(new InputStreamReader(System.in))
        while (true)
          val line = reader.readLine()
          if (line != null && line.trim.nonEmpty)
            println(s"[Tastierino] Invio PIN alla centrale...")
            controller ! AlarmProtocol.PinEntered(line.trim)

      Behaviors.receiveMessage:
        case StartListening =>
          println("[Tastierino] Pronto. Digita il PIN nella console e premi INVIO in qualsiasi momento.")
          val thread = new Thread(() => readConsole())
          thread.setDaemon(true)
          thread.start()
          Behaviors.same
    }
