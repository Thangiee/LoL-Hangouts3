package server

import io.getquill._
import io.getquill.context.sql.idiom.SqlIdiom
import share.Message

trait Schema[T <: SqlIdiom, S <: NamingStrategy] {
  val ctx: JdbcContext[T, S]
  import ctx._

  val Messages = quote(query[Message])

  object Message {
    def all(userId: Int, friendId: Int) =
      quote(Messages.filter(msg => msg.userId == lift(userId) && msg.friendId == lift(friendId)))

    def deleteAll(userId: Int, friendId: Int) =
      quote(all(userId, friendId).update(m => m.deleted -> true))

    def recentN(userId: Int, friendId: Int, n: Int) =
      quote(all(userId, friendId).sortBy(_.timestamp)(Ord.desc).take(lift(n)))

    def newest(userId: Int, friendId: Int) =
      quote(recentN(userId, friendId, 1))

    def unreadRecentN(userId: Int, friendId: Int, n: Int) =
      quote(recentN(userId, friendId, n).filter(!_.read))
  }

}
