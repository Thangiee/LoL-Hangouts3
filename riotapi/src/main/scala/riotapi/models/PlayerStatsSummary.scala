package riotapi.models

case class PlayerStatsSummary(
  aggregatedStats: AggregatedStats = AggregatedStats(),
  losses: Int = 0,
  playerStatSummaryType: String = "",
  wins: Int = 0
)

object PlayerStatsSummary {
  implicit val pkl = upickle.default.macroRW[PlayerStatsSummary]
}