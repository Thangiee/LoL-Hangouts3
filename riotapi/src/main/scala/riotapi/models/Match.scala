package riotapi.models

case class Match(
  champName: String,
  queueType: String,
  outCome: String,
  date: String,
  duration: String,
  cs: Short,
  gold: Int,
  kills: Short,
  deaths: Short,
  assists: Short,
  items1Id: Short,
  items2Id: Short,
  items3Id: Short,
  items4Id: Short,
  items5Id: Short,
  items6Id: Short,
  trinketId: Short
)

object Match {
  implicit val pkl = upickle.default.macroRW[Match]
}
