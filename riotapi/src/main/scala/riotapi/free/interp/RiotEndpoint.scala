package riotapi.free.interp

import java.util.concurrent.{Executors, ThreadLocalRandom}

import cats._
import cats.data.{NonEmptyVector, Xor, XorT}
import cats.implicits.vectorInstance
import cats.syntax.all._
import play.api.libs.json.Json
import lolchat.data._
import riotapi._
import riotapi.free.RiotApiF
import riotapi.free.RiotApiF._
import riotapi.models._
import riotapi.utils.Parsing._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}
import scalacache._
import scalacache.guava._
import scalaj.http.{Http, HttpResponse}

case class RiotEndpoint(keys: NonEmptyVector[String], numOfThreads: Int = 8) {
  implicit val exeCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numOfThreads))
  implicit val scalaCache = ScalaCache(GuavaCache())

  val gameVer       = "v1.3"
  val summVer       = "v1.4"
  val teamVer       = "v2.4"
  val staticDataVer = "v1.2"
  val statsVer      = "v1.3"
  val leagueVer     = "v2.5"
  val matchVer      = "v2.2"

  def run[A](ops: RiotApiOp[A], region: Region): AsyncResult[A] = ops.foldMap(interpreter(region))

  def randomKey: String = new Random(ThreadLocalRandom.current()).shuffle(keys.unwrap).head

  def baseUrl(region: Region) = s"https://${region.abbr}.api.pvp.net/api/lol/${region.abbr}"

  def interpreter(region: Region) = new (RiotApiF ~> AsyncResult) {
    def call(url: String, params: (String, String)*)(ttl: FiniteDuration): AsyncResult[String] = {
      val cacheKey = url + params.mkString

      @tailrec def attemptCall(i: Int, max: Int): Xor[Error, String] = {
        val request = Http(url).params(params :+ ("api_key" -> randomKey))
        val response = Try(request.asString)

        response match {
          case Success(resp) if resp.is2xx =>
            sync.cachingWithTTL(url)(ttl)(resp.body)
            Xor.Right(resp.body)
          case Success(HttpResponse(_, 403, _)) =>
            if (i <= max) attemptCall(i+1, max) else Xor.Left(Error(408, "Request Timeout. Please try again."))
          case Success(HttpResponse(body, code, _)) =>
            val errMsg = Try(Json.parse(body) \ "status" \ "message").getOrElse("") + s":${request.url}"
            Xor.Left(Error(code, errMsg))
          case Failure(err) => Xor.Left(Error(500, s"${err.getMessage}:${request.url}"))
        }
      }

      XorT(get[String, NoSerialization](cacheKey).map {
        case Some(cacheHit) => Xor.Right(cacheHit)
        case None => attemptCall(1, max = 7 min keys.unwrap.size)
      })
    }

    def callAndParseResp[A](url: String, params: (String, String)*)(ttl: FiniteDuration)(parser: String => Xor[Throwable, A]): AsyncResult[A] =
      for {
        json <- call(url, params:_*)(ttl)
        resp <- AsyncResult(parser(json).leftMap(err => Error(500, err.getMessage)))
      } yield resp

    override def apply[A](fa: RiotApiF[A]): AsyncResult[A] = fa match {
      case SummonerByName(name)      =>
        callAndParseResp(s"${baseUrl(region)}/$summVer/summoner/by-name/$name")(1.day)(json => parseSummoner(name, json))

      case SummonerById(id)          =>
        callAndParseResp(s"${baseUrl(region)}/$summVer/summoner/$id")(1.day)(json => parseSummoner(id.toString, json))

      case SummonerNameById(id)      =>
        callAndParseResp(s"${baseUrl(region)}/$summVer/summoner/$id/name")(1.day)(json => parseSummonerName(id, json))

      case ChampsStatsById(id, year) =>
        callAndParseResp(s"${baseUrl(region)}/$statsVer/stats/by-summoner/$id/ranked", "season" -> s"SEASON$year")(20.minutes)(parseChampsStats)
          .recover {
            case Error(404, _, _) => NonEmptyVector(ChampionStats())
          }

      case LeagueEntriesByIds(ids)   =>
        (for {
          json <- call(s"${baseUrl(region)}/$leagueVer/league/by-summoner/${ids.mkString(",")}/entry")(20.minutes)
          resp <- AsyncResult.pure(parseLeagueEntries(ids, json))
        } yield resp) recover {
          case Error(404, _, _) =>
            val league = NonEmptyVector(League())
            ids.map(id => (id, league)).toMap
        }

      case ChampStaticDataByIds(ids)   =>
        ids.map(id => {
          val url = s"https://${region.abbr}.api.pvp.net/api/lol/static-data/${region.abbr}/v1.2/champion/$id"
          callAndParseResp(url)(1.day)(json => parseChampion(json))
        }).sequenceU


      case CurrentGameInfoById(id)   =>
        val platformId = region.abbr.toLowerCase match {
          case "na" => "NA1"
          case "euw" => "EUW1"
          case "eune" => "EUN1"
          case "kr" => "KR"
          case "oce" => "OC1"
          case "br" => "BR1"
          case "lan" => "LA1"
          case "las" => "LA2"
          case "ru" => "RU"
          case "tr" => "TR1"
          case _ => "PBE1"
        }
        val url = s"https://${region.abbr}.api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/$platformId/$id"
        callAndParseResp(url)(20.minutes)(json => parseCurrentGameInfo(json))

      case SpellStaticData(id)       =>
        val url = s"https://global.api.pvp.net/api/lol/static-data/${region.abbr}/$staticDataVer/summoner-spell/$id"
        callAndParseResp(url)(1.day)(json => parseSummonerSpell(json))

      case SummaryStatsById(id, year)      =>
        val url = s"${baseUrl(region)}/$statsVer/stats/by-summoner/$id/summary?season=SEASON$year"
        callAndParseResp(url)(20.minutes)(parsePlayerStatsSummary)
    }

  }

}
