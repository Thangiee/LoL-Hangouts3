package share

import java.util.Date

import play.api.libs.json.Json

trait Api {
  def getMsgs(userId: Int, friendId: Int): Seq[Message]

  def getRecentMsgs(userId: Int, friendId: Int, n: Int, filterRead: Boolean): Seq[Message]

  def getNewestMsg(userId: Int, friendId: Int): Option[Message]

  def saveMsgs(msgs: Seq[Message]): Seq[Long]

  def saveMsg(msg: Message): Seq[Long] = saveMsgs(Seq(msg))

  def deleteMsgs(userId: Int, friendId: Int): Long

  def markMsgsRead(userId: Int, friendId: Int): Long
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
}