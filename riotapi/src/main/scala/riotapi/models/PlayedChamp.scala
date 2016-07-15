package riotapi.models

import riotapi._

case class PlayedChamp(
  id: Int,
  name: String,
  killsRatio: Double,
  deathsRatio: Double,
  assistsRatio: Double,
  games: Int,
  winRate: Double
)

object PlayedChamp {
  import utils.Stats._
  import scala.math._

  implicit val pkl = upickle.default.macroRW[PlayedChamp]

  def apply(champion: (Champion, ChampionStats)): PlayedChamp = {
    val (champ, champStats) = champion
    val stats = champStats.stats
    val (k, d, a) = calcKDARatios(champStats.stats)
    PlayedChamp(champ.id, champ.name, k, d, a, max(stats.totalSessionsPlayed, 1), calcWinRate(stats))
  }
}