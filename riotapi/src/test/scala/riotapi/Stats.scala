package riotapi

import riotapi.models._
import riotapi.utils.Stats._

class Stats extends BaseSpec {

  "calcKDA()" must "not be less than 0.0" in {
    forAll() { stats: AggregatedStats =>
      calcKDA(stats) shouldNot be < 0.0
    }
  }

  val testStats = AggregatedStats(
    totalChampionKills = 1450,
    totalDeathsPerSession = 921,
    totalAssists = 1452,
    totalSessionsLost = 70,
    totalSessionsWon = 128,
    totalSessionsPlayed = 128+70
  )

  it should "yield the correct result" in {
    calcKDA(testStats) shouldEqual 3.15 +- .01
  }

  "calcKDARatios()" must "yield the correct result" in {
    val (k, d, a) = calcKDARatios(testStats)
    k shouldEqual 7.32 +- .01
    d shouldEqual 4.65 +- .01
    a shouldEqual 7.33 +- .01
  }

  "calcWinRate()" must "yield the correct result" in {
    calcWinRate(testStats) shouldEqual 64.64 +- .01
  }

  "calcElo" must "yield the correct result" in {
    val entry = LeagueEntry(division = "I", leaguePoints = 75, losses = 70, wins =  128)
    val league = League(entries = Vector(entry), tier = "Diamond")
    calcElo(league) shouldEqual 2517
  }
}
