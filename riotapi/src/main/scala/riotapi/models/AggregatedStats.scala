package riotapi.models

case class AggregatedStats(
  totalAssists: Int = 0,
  totalChampionKills: Int = 0,
  totalDeathsPerSession: Int = 0,
  totalSessionsLost: Int = 0,
  totalSessionsPlayed: Int = 0,
  totalSessionsWon: Int = 0
)

object AggregatedStats {
  implicit val pkl = upickle.default.macroRW[AggregatedStats]
}
