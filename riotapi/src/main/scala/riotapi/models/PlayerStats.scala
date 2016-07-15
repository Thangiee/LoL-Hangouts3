package riotapi.models

import cats.data.NonEmptyVector
import cats.implicits._
import riotapi.utils.Stats

case class PlayerStats(
  playerName: String,
  teamNumber: Int,
  championName: String,
  leagueTier: String,
  leagueDivision: String,
  leaguePoints: Int,
  rankWins: Int,
  rankLoses: Int,
  normalWin: Int,
  killRatio: Double,
  deathRatio: Double,
  assistRatio: Double,
  elo: Int,
  series: Vector[String],
  spellOne: String,
  spellTwo: String
)

object PlayerStats {
  implicit val pkl = upickle.default.macroRW[PlayerStats]

  def apply(player: Participant, league: League, champ: Champion, spell1: SummonerSpell, spell2: SummonerSpell,
            champsStats: NonEmptyVector[ChampionStats], summaryStats: NonEmptyVector[PlayerStatsSummary]): PlayerStats = {

    val rank = champsStats.find(_.id == 0).map(_.stats).getOrElse(AggregatedStats())
    val normal = summaryStats.find(_.playerStatSummaryType == "Unranked").getOrElse(PlayerStatsSummary())
    val numOfGames = Math.max(rank.totalSessionsPlayed, 1.0)

    PlayerStats(
      playerName = player.summonerName,
      teamNumber = player.teamId,
      championName = champ.name,
      spellOne = spell1.name,
      spellTwo = spell2.name,
      leagueTier = league.tier,
      leagueDivision = league.entries.headOption.map(_.division).getOrElse(""),
      leaguePoints = league.entries.headOption.map(_.leaguePoints).getOrElse(0),
      rankWins = rank.totalSessionsWon,
      rankLoses = rank.totalSessionsLost,
      normalWin = normal.wins,
      killRatio = rank.totalChampionKills / numOfGames,
      deathRatio = rank.totalDeathsPerSession / numOfGames,
      assistRatio = rank.totalAssists / numOfGames,
      elo = Stats.calcElo(league),
      series = league.entries.headOption.map(_.miniSeries).map(_.progress.map(_.toString).toVector).getOrElse(Vector())
    )
  }
}