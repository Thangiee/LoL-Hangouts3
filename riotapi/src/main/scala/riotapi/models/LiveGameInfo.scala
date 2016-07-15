package riotapi.models

case class LiveGameInfo(gameInfo: CurrentGameInfo, players: Vector[(Participant, League)])

object LiveGameInfo {
  implicit val pkl = upickle.default.macroRW[LiveGameInfo]
}


