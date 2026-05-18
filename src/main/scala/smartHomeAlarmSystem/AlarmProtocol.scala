package smartHomeAlarmSystem

object AlarmProtocol:

  enum Zone:
    case LivingRoom
    case Kitchen
    case Bedroom
    case Garage

  trait Command

  case class PinEntered(pin: String) extends Command
  case class SelectZones(zones: Set[Zone]) extends Command
  case class SensorTriggered(zone: Zone) extends Command

  enum Timeout(val key: String, val command: Command):
    case Exit
    case Entry
    case ZoneSelection