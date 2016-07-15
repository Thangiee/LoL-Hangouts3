package riotapi.models

case class Champion(
  id: Int,
  title: String,
  name: String,
  key: String
)

object Champion {
  implicit val pkl = upickle.default.macroRW[Champion]
}