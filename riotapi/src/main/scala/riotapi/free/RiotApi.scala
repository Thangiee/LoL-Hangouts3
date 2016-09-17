package riotapi.free

import cats.data.NonEmptyVector
import cats.free.Free
import freasymonad._
import riotapi.models._

@free trait RiotApi {
  type RiotApiOp[A] = Free[GrammarADT, A]
  sealed trait GrammarADT[A]

  def summonerByName(username: String): RiotApiOp[Summoner]

  def summonerById(id: Int): RiotApiOp[Summoner]

  def summonerNameById(id: Int): RiotApiOp[String]

  def champStaticDataByIds(ids: Vector[Int]): RiotApiOp[Vector[Champion]]

  def champStaticDataById(id: Int): RiotApiOp[Champion] = champStaticDataByIds(Vector(id)).map(_.head)

  def champsStatsById(id: Int, year: Int): RiotApiOp[NonEmptyVector[ChampionStats]]

  def currentGameInfoById(id: Int): RiotApiOp[CurrentGameInfo]

  def spellStaticData(id: Int): RiotApiOp[SummonerSpell]

  def leagueEntriesById(id: Int): RiotApiOp[NonEmptyVector[League]] = leagueEntriesByIds(Vector(id)).map(_.getOrElse(id, NonEmptyVector(League(), Vector.empty)))

  def leagueEntriesByIds(ids: Vector[Int]): RiotApiOp[Map[Int, NonEmptyVector[League]]]

  def summaryStatsById(id: Int, year: Int): RiotApiOp[NonEmptyVector[PlayerStatsSummary]]

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
      top4PlayedChamps <- mostPlayedChamps(champs.toVector, 4)
    } yield {
      ProfileSummary(summ, leagues.head, rankStats, top4PlayedChamps)
    }

  def mostPlayedChamps(champsStats: Vector[ChampionStats], n: Int): RiotApiOp[Vector[PlayedChamp]] = {
    val champsStatsSorted = champsStats
      .filter(_.id != 0) // filter out id 0 since that's the stats of all champs combined
      .sortBy(_.stats.totalSessionsPlayed) // sort by most games
      .reverse
      .take(n)

    champStaticDataByIds(champsStatsSorted.map(_.id)).map(_.zip(champsStatsSorted).map(PlayedChamp(_)))
  }

}
