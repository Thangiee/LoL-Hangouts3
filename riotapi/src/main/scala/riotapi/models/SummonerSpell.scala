package riotapi.models

case class SummonerSpell(
  name: String,
  description: String,
  summonerLevel: Int,
  id: Int ,
  key: String
)

object SummonerSpell {
  implicit val pkl = upickle.default.macroRW[SummonerSpell]
}
