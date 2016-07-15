package riotapi.models

case class Participant(
  bot: Boolean = false,
  championId: Int = 0,
  profileIconId: Int = 0,
  spell1Id: Int = 0,
  spell2Id: Int = 0,
  summonerId: Int = 0 ,
  summonerName: String = "",
  teamId: Int = 0
)

object Participant {
  implicit val pkl = upickle.default.macroRW[Participant]
}
