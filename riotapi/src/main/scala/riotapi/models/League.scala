package riotapi.models

case class League(
  entries: Vector[LeagueEntry] = Vector.empty,
  name: String = "N/A",
  participantId: Option[String] = None,
  queue: String = "",
  tier: String = "Unranked"
)

object League {
  implicit val pkl = upickle.default.macroRW[League]
}