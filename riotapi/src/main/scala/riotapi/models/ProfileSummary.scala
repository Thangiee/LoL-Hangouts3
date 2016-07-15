package riotapi.models

import riotapi.utils.Stats._

case class ProfileSummary(
  summonerName: String,
  leagueDivision: String,
  leagueName: String,
  leaguePoints: Int,
  leagueTier: String,
  level: Int,
  loses: Int,
  wins: Int,
  games: Int,
  kda: Double,
  killsRatio: Double,
  deathsRatio: Double,
  assistsRatio: Double,
  elo: Int,
  winRate: Double,
  mostPlayedChamps: Seq[PlayedChamp]
)

object ProfileSummary {
  implicit val pkl = upickle.default.macroRW[ProfileSummary]

  def apply(summ: Summoner, league: League, stats: AggregatedStats, mostPlayedChamps: Vector[PlayedChamp]): ProfileSummary = {
    val entry = league.entries.headOption.getOrElse(LeagueEntry())
    val (k, d, a) = calcKDARatios(stats)

    ProfileSummary(
      summ.name,
      entry.division,
      league.name,
      entry.leaguePoints,
      league.tier,
      summ.summonerLevel,
      stats.totalSessionsLost,
      stats.totalSessionsWon,
      stats.totalSessionsPlayed,
      calcKDA(stats),
      k, d, a,
      calcElo(league),
      calcWinRate(stats),
      mostPlayedChamps
    )
  }
}
