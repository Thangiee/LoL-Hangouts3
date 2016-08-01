import java.util.Date

import io.getquill._
import server.Schema
import share.Message

import scala.util.Random._

class DBSpec extends BaseSpec with Schema[H2Dialect, LowerCase] {
  val ctx = new JdbcContext[H2Dialect, LowerCase]("testDB")
  import ctx._

  "Message DB" can "get all saved messages between two friends" in {
    forAll { msgs: List[Message] =>
      val (msgsBtw, _) = msgs.partition(m => m.userId == aliceId && m.friendId == bobId)
      ctx.run(quote(query[Message].delete))
      ctx.run(quote(query[Message].insert))(msgs)
      ctx.run(Messages.all(aliceId, bobId)) should be(msgsBtw)
    }
  }

  it can "mark messages as deleted" in {
    forAll { msgs: List[Message] =>
      ctx.run(quote(query[Message].delete))
      ctx.run(quote(query[Message].insert))(msgs)
      ctx.run(Messages.markDeleted(aliceId, bobId))
      ctx.run(Messages.all(aliceId, bobId)).foreach(_.deleted should be(true))
    }
  }

  it can "get the recent N newest messages" in {
    forAll { (msgs: List[Message], msgs2: List[Message]) =>
      val newMsgs = msgs2.map(_.copy(userId = aliceId, friendId = bobId, timestamp = new Date()))
      ctx.run(quote(query[Message].delete))
      ctx.run(quote(query[Message].insert))(shuffle(msgs ++ newMsgs))
      ctx.run(Messages.recentN(aliceId, bobId, newMsgs.size)) should contain theSameElementsAs newMsgs
    }
  }

  it should "yield the same msg for newest and recentN where N=1" in {
    forAll { msgs: List[Message] =>
      ctx.run(quote(query[Message].delete))
      ctx.run(quote(query[Message].insert))(msgs)
      ctx.run(Messages.newest(aliceId, bobId)).headOption shouldEqual ctx.run(Messages.recentN(aliceId, bobId, 1)).headOption
    }
  }
}
