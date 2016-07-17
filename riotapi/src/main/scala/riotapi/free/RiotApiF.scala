package riotapi.free

import cats.data.NonEmptyVector
import cats.free.Free
import cats.implicits._
import riotapi._
import riotapi.free.RiotApiF._
import riotapi.models._

trait RiotApiF[A]
object RiotApiF {
  case class SummonerByName(username: String)       extends RiotApiF[Summoner]
  case class SummonerById(id: Int)                  extends RiotApiF[Summoner]
  case class SummonerNameById(id: Int)              extends RiotApiF[String]
  case class ChampsStatsById(id: Int, year: Int)    extends RiotApiF[NonEmptyVector[ChampionStats]]
  case class LeagueEntriesByIds(ids: Vector[Int])   extends RiotApiF[Map[Int, NonEmptyVector[League]]]
  case class ChampStaticDataByIds(ids: Vector[Int]) extends RiotApiF[Vector[Champion]]
  case class CurrentGameInfoById(id: Int)           extends RiotApiF[CurrentGameInfo]
  case class SpellStaticData(id: Int)               extends RiotApiF[SummonerSpell]
  case class SummaryStatsById(id: Int, year: Int)   extends RiotApiF[NonEmptyVector[PlayerStatsSummary]]
}

trait RiotApiOps {
  def summonerByName(username: String): RiotApiOp[Summoner] = Free.liftF(SummonerByName(username.replace(" ", "")))

  def summonerById(id: Int): RiotApiOp[Summoner] = Free.liftF(SummonerById(id))

  def summonerNameById(id: Int): RiotApiOp[String] = Free.liftF(SummonerNameById(id))

  def champStaticDataByIds(ids: Vector[Int]): RiotApiOp[Vector[Champion]] = Free.liftF(ChampStaticDataByIds(ids))

  def champStaticDataById(id: Int): RiotApiOp[Champion] = champStaticDataByIds(Vector(id)).map(_.head)

  def champsStatsById(id: Int, year: Int): RiotApiOp[NonEmptyVector[ChampionStats]] = Free.liftF(ChampsStatsById(id, year))

  def currentGameInfoById(id: Int): RiotApiOp[CurrentGameInfo] = Free.liftF(CurrentGameInfoById(id))

  def spellStaticData(id: Int): RiotApiOp[SummonerSpell] = Free.liftF(SpellStaticData(id))

  def leagueEntriesById(id: Int): RiotApiOp[NonEmptyVector[League]] = leagueEntriesByIds(Vector(id)).map(_.getOrElse(id, NonEmptyVector(League())))

  def leagueEntriesByIds(id: Vector[Int]): RiotApiOp[Map[Int, NonEmptyVector[League]]] = Free.liftF(LeagueEntriesByIds(id))

  def summaryStatsById(id: Int, year: Int): RiotApiOp[NonEmptyVector[PlayerStatsSummary]] = Free.liftF(SummaryStatsById(id, year))

  def liveGameInfo(username: String): RiotApiOp[LiveGameInfo] = {
    for {
      id <- summonerByName(username.replace(" ", "")).map(_.id)
      gameInfo <- currentGameInfoById(id)
      allPlayers = gameInfo.participants
      leagues <- leagueEntriesByIds(allPlayers.map(_.summonerId))
    } yield LiveGameInfo(gameInfo, allPlayers.map(p => (p, leagues(p.summonerId).head)))
  }

  def playerStats(player: Participant, league: League): RiotApiOp[PlayerStats] =
    for {
      champ <- champStaticDataById(player.championId)
      spell1 <- spellStaticData(player.spell1Id)
      spell2 <- spellStaticData(player.spell2Id)
      champsStats <- champsStatsById(player.summonerId, 2016)
      summaryStats <- summaryStatsById(player.summonerId, 2016)
    } yield {
      PlayerStats(player, league, champ, spell1, spell2, champsStats, summaryStats)
    }

  def profileSummary(username: String, year: Int): RiotApiOp[ProfileSummary] =
    for {
      summ    <- summonerByName(username.replace(" ", ""))
      champs  <- champsStatsById(summ.id, year)
      leagues <- leagueEntriesById(summ.id)
      rankStats = champs.find(_.id == 0).map(_.stats).getOrElse(AggregatedStats())
      top4PlayedChamps <- mostPlayedChamps(champs.unwrap, 4)
    } yield {
      ProfileSummary(summ, leagues.head, rankStats, top4PlayedChamps)
    }

  private def mostPlayedChamps(champsStats: Vector[ChampionStats], n: Int): RiotApiOp[Vector[PlayedChamp]] = {
    val champsStatsSorted = champsStats
      .filter(_.id != 0) // filter out id 0 since that's the stats of all champs combined
      .sortBy(_.stats.totalSessionsPlayed) // sort by most games
      .reverse
      .take(n)

    champStaticDataByIds(champsStatsSorted.map(_.id)).map(_.zip(champsStatsSorted).map(PlayedChamp(_)))
  }

}
object RiotApiOps extends RiotApiOps

