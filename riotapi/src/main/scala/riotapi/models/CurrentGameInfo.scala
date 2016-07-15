package riotapi.models

case class CurrentGameInfo(
  gameId: Double,
  gameLength: Int,
  gameMode: String,
  gameQueueConfigId: Int,
  gameType: String,
  mapId: Int,
  participants: Vector[Participant],
  platformId: String
)

object CurrentGameInfo {
  implicit val pkl = upickle.default.macroRW[CurrentGameInfo]
}