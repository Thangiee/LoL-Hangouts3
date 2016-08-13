package share

import java.util.Date

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

trait Api {
  def getMsgsBtw(userId: Int, friendId: Int): Seq[Message]

  def getRecentMsgsBtw(userId: Int, friendId: Int, n: Int, filterRead: Boolean): Seq[Message]

  def getNewestMsg(userId: Int, friendId: Int): Option[Message]

  def saveMsgs(msgs: Seq[Message]): Seq[Long]

  def saveMsg(msg: Message): Seq[Long] = saveMsgs(Seq(msg))

  def deleteMsgs(userId: Int, friendId: Int): Long

  def markMsgsRead(userId: Int, friendId: Int): Long

  def friendsNewestMsg(userId: Int): Map[Int, Message]

  def getBuildVersion(): Option[BuildVersion]
}

case class Message(
  userId: Int,
  friendId: Int,
  text: String,
  sender: Boolean = true,
  read: Boolean = true,
  timestamp: Date = new Date()
)

object Message {
  implicit val msgFmt    = Json.format[Message]
  implicit val mapIntFmt = new MapIntFormats[Message]()
}

case class BuildVersion(code: Int, version: String, msg: String, timestamp: Date)

object BuildVersion {
  implicit val fmt = Json.format[BuildVersion]
}

class MapIntFormats[T]()(implicit format: Format[T]) extends Format[Map[Int, T]]{
  def reads(json: JsValue): JsResult[Map[Int, T]] =
    JsSuccess(json.as[Map[String, T]].map{case (k, v) => k.toString.toInt -> v .asInstanceOf[T]})
  def writes(map: Map[Int, T]): JsValue =
    Json.obj(map.map{case (s, o) => val ret: (String, JsValueWrapper) = s.toString -> Json.toJson(o); ret}.toSeq:_*)
}