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
}

case class Message(
  userId: Int,
  friendId: Int,
  text: String,
  sender: Boolean = true,
  read: Boolean = true,
  deleted: Boolean = false,
  timestamp: Date = new Date()
)

object Message {
  implicit val fmt = Json.format[Message]
  implicit val ff = new MapIntFormats[Message]()
}

class MapIntReads[T]()(implicit reads: Reads[T]) extends Reads[Map[Int, T]] {
  def reads(jv: JsValue): JsResult[Map[Int, T]] =
    JsSuccess(jv.as[Map[String, T]].map{case (k, v) =>
      k.toString.toInt -> v .asInstanceOf[T]
    })
}

class MapIntWrites[T]()(implicit writes: Writes[T])  extends Writes[Map[Int, T]] {
  def writes(map: Map[Int, T]): JsValue =
    Json.obj(map.map{case (s, o) =>
      val ret: (String, JsValueWrapper) = s.toString -> Json.toJson(o)
      ret
    }.toSeq:_*)
}

class MapIntFormats[T]()(implicit format: Format[T]) extends Format[Map[Int, T]]{
  override def reads(json: JsValue): JsResult[Map[Int, T]] = new MapIntReads[T].reads(json)
  override def writes(o: Map[Int, T]): JsValue = new MapIntWrites[T].writes(o)
}