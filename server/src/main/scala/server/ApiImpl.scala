package server

import io.getquill.{JdbcContext, PostgresDialect, SnakeCase}
import share.{Api, BuildVersion, Message}

object ApiImpl extends Api with Schema[PostgresDialect, SnakeCase] {
  val ctx = new JdbcContext[PostgresDialect, SnakeCase]("db")
  import ctx._

  def getMsgsBtw(userId: Int, friendId: Int): Seq[Message] =
    ctx.run(Messages.all(userId, friendId))

  def getRecentMsgsBtw(userId: Int, friendId: Int, n: Int, onlyUnread: Boolean): Seq[Message] =
    if (onlyUnread) ctx.run(Messages.unreadRecentN(userId, friendId, n))
    else            ctx.run(Messages.recentN(userId, friendId, n))

  def getNewestMsg(userId: Int, friendId: Int): Option[Message] =
    ctx.run(Messages.newest(userId, friendId)).headOption

  def saveMsgs(msgs: Seq[Message]): Seq[Long] =
    ctx.run(quote(query[Message]).insert)(msgs.toList)

  def deleteMsgs(userId: Int, friendId: Int): Long =
    ctx.run(Messages.delete(userId, friendId))

  def markMsgsRead(userId: Int, friendId: Int): Long =
    ctx.run(Messages.markRead(userId, friendId))

  def friendsNewestMsg(userId: Int): Map[Int, Message] =
    ctx.run(Messages.all(userId)).groupBy(_.friendId).mapValues(_.maxBy(_.timestamp))

  def getBuildVersion(): Option[BuildVersion] = ctx.run(BuildVersions.newest).headOption
}
