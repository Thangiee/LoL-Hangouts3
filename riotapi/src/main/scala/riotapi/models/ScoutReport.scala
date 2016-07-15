package riotapi.models

case class ScoutReport(
  queueType: String,
  mapName: String,
  blueTeam: Seq[PlayerStats],
  purpleTeam: Seq[PlayerStats]
)

object ScoutReport {

  implicit val pkl = upickle.default.macroRW[ScoutReport]

  lazy val mapNames = Map[Int, String](
    1 -> "Summoner's Rift",
    2 -> "Summoner's Rift",
    3 -> "The Proving Grounds",
    4 -> "Twisted Treeline",
    8 -> "The Crystal Scar",
    10 -> "Twisted Treeline",
    11 -> "Summoner's Rift",
    12 -> "Howling Abyss"
  )

  def apply(gameInfo: CurrentGameInfo, players: Vector[PlayerStats]): ScoutReport =
    ScoutReport(
      gameInfo.gameType,
      mapNames.getOrElse(gameInfo.mapId, s"Unknown map ${gameInfo.mapId}"),
      players.filter(_.teamNumber == 100).toList,
      players.filter(_.teamNumber == 200).toList
    )
}