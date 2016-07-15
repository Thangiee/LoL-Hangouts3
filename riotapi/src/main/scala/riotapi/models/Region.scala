package riotapi.models

case class Region(abbr: String) extends AnyVal

trait RegionAbbr {
  val NA = Region("na")
  val BR = Region("br")
  val EUNE = Region("eune")
  val EUW = Region("euw")
}
object RegionAbbr extends RegionAbbr