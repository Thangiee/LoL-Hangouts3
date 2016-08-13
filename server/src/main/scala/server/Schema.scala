package server

import io.getquill._
import io.getquill.context.sql.idiom.SqlIdiom
import share.{BuildVersion, Message}

trait Schema[T <: SqlIdiom, S <: NamingStrategy] {
  val ctx: JdbcContext[T, S]
  import ctx._

  object Messages {
    def all(userId: Int) =
      quote(query[Message].filter(_.userId == lift(userId)))

    def all(userId: Int, friendId: Int) =
      quote(query[Message].filter(msg => msg.userId == lift(userId) && msg.friendId == lift(friendId)))

    def delete(userId: Int, friendId: Int) =
      quote(all(userId, friendId).delete)

    def markRead(userId: Int, friendId: Int) =
      quote(all(userId, friendId).update(m => m.read -> true))

    def recentN(userId: Int, friendId: Int, n: Int) =
      quote(all(userId, friendId).sortBy(_.timestamp)(Ord.desc).take(lift(n)))

    def newest(userId: Int, friendId: Int) =
      quote(recentN(userId, friendId, 1))

    def unreadRecentN(userId: Int, friendId: Int, n: Int) =
      quote(recentN(userId, friendId, n).filter(!_.read))
  }

  object BuildVersions {
    def newest = quote(query[BuildVersion].sortBy(_.timestamp)(Ord.desc))
  }
}
