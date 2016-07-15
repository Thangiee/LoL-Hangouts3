package riotapi.models

case class MiniSeries(losses: Int = 0, progress: String = "", target: Int = 0, wins: Int = 0)

object MiniSeries {
  implicit val pkl = upickle.default.macroRW[MiniSeries]
}
