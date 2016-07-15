package riotapi.models

case class ChampionStats(id: Int = 0, stats: AggregatedStats = AggregatedStats())

object ChampionStats {
  implicit val pkl = upickle.default.macroRW[ChampionStats]
}