package riotapi.utils

import cats.data._
import riotapi.models._
import play.api.libs.json._
import upickle.default._

import scala.util.Try

object Parsing {

  def select(json: String, keys: String*): String =
    keys.foldLeft(json)((js, key) => (Json.parse(js) \ key).toString())

  def select(json: String, key: Int): String = select(json, key.toString)

  def parseSummoner(key: String, json: String): Xor[Throwable, Summoner] =
    Xor.fromTry(Try(read[Summoner](select(json, key.toLowerCase))))

  def parseSummonerName(id: Int, json: String): Xor[Throwable, String] =
    Xor.fromTry(Try(select(json, id)))

  def parseChampion(json: String): Xor[Throwable, Champion] =
    Xor.fromTry(Try(read[Champion](json)))

  def parseChampsStats(json: String): Xor[Throwable, NonEmptyVector[ChampionStats]] =
    asNonEmpty(read[Vector[ChampionStats]](select(json, "champions")))

  def parseCurrentGameInfo(json: String): Xor[Throwable, CurrentGameInfo] =
    Xor.fromTry(Try(read[CurrentGameInfo](json)))

  def parseSummonerSpell(json: String): Xor[Throwable, SummonerSpell] =
    Xor.fromTry(Try(read[SummonerSpell](json)))

  def parsePlayerStatsSummary(json: String): Xor[Throwable, NonEmptyVector[PlayerStatsSummary]] =
    asNonEmpty(read[Vector[PlayerStatsSummary]](select(json, "playerStatSummaries")))

  def parseLeagueEntries(id: Int, json: String): Xor[Throwable, NonEmptyVector[League]] =
    asNonEmpty(read[Vector[League]](select(json, id)))

  def parseLeagueEntries(ids: Seq[Int], json: String): Map[Int, NonEmptyVector[League]] =
    ids.map(k => (k, parseLeagueEntries(k, json).getOrElse(NonEmptyVector(League(), Vector.empty)))).toMap

  private def asNonEmpty[A](value: => Seq[A]): Xor[Throwable, NonEmptyVector[A]] = Xor.fromTry(Try(value)).flatMap {
    case Nil => Xor.left(new Throwable("Empty"))
    case h +: t => Xor.Right(NonEmptyVector(h, t.toVector))
  }
}

