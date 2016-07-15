package riotapi.models

case class LeagueEntry(
  division: String = "",
  isFreshBlood: Boolean = false,
  isHotStreak: Boolean = false,
  isInactive: Boolean = false,
  isVeteran: Boolean = false,
  leaguePoints: Int = 0,
  losses: Int = 0,
  miniSeries: MiniSeries = MiniSeries(),
  playerOrTeamId: String = "",
  playerOrTeamName: String = "",
  wins: Int = 0
)

object LeagueEntry {
  implicit val pkl = upickle.default.macroRW[LeagueEntry]
}
