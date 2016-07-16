package riotapi.utils

import riotapi.models._

import scala.math._

object Stats {

  def calcKDA(stats: AggregatedStats): Double =
    (stats.totalChampionKills + stats.totalAssists) / max(stats.totalDeathsPerSession, 1.0)

  def calcKDARatios(stats: AggregatedStats): (Double, Double, Double) = {
    val games = max(stats.totalSessionsPlayed.toDouble, 1.0)
    (stats.totalChampionKills / games, stats.totalDeathsPerSession / games, stats.totalAssists / games)
  }

  def calcWinRate(stats: AggregatedStats): Double = {
    (stats.totalSessionsWon / max(stats.totalSessionsPlayed.toDouble, 1.0)) * 100
  }

  def calcElo(league: League): Int = {
    val entry = league.entries.headOption.getOrElse(LeagueEntry())
    val TierWeight = 350.0
    val DivisionWeight = 70.0
    val baseElo = 450.0

    val tierPoints: Double =
      league.tier.toUpperCase match {
        case "BRONZE"     => TierWeight * 1
        case "SILVER"     => TierWeight * 2
        case "GOLD"       => TierWeight * 3
        case "PLATINUM"   => TierWeight * 4
        case "DIAMOND"    => TierWeight * 5
        case "MASTER"     => TierWeight * 6
        case "CHALLENGER" => TierWeight * 6
        case _            => TierWeight * 0
      }

    val divisionPoints: Double =
      if (league.tier.toUpperCase.equals("CHALLENGER")) 0
      else entry.division.toUpperCase match {
        case "I"    => DivisionWeight * 4
        case "II"   => DivisionWeight * 3
        case "III"  => DivisionWeight * 2
        case "IV"   => DivisionWeight * 1
        case "V"    => DivisionWeight * 0
        case _      => DivisionWeight * 0
      }

    val seriesPoints: Double = {
      val s = entry.miniSeries
      val n = if (s.target == 3) 5 else 3 // determine 5 or 3 games series
      20.0 * (s.wins / n)
    }

    (baseElo + entry.leaguePoints * .5 + tierPoints + divisionPoints + seriesPoints).toInt
  }

}
