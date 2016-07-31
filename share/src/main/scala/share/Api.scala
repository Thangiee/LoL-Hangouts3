package share

trait Api {
  def echo(txt: String): String

  def getMsgs(userId: Int, friendId: Int): Vector[Message]

  def getRecentMsgs(userId: Int, friendId: Int, n: Int, filterRead: Boolean = false): Vector[Message]

  def getNewestMsg(userId: Int, friendId: Int): Option[Message]

  def saveMsgs(msgs: Seq[Message]): Unit

  def deleteChatLog(userId: Int, friendId: Int): Unit

  def markChatLogRead(userId: Int, friendId: Int): Unit
}

case class Message(userId: Int, friendId: Int, text: String, sender: Boolean, read: Boolean, deleted: Boolean, timestamp: java.util.Date)
