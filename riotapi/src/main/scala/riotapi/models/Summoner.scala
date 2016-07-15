package riotapi.models

case class Summoner(
  id: Int = -1,
  name: String = "???",
  profileIconId: Int = 1,
  summonerLevel: Int = 1
)

object Summoner {
  implicit val pkl = upickle.default.macroRW[Summoner]
}