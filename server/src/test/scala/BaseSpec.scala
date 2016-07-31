import java.util.Calendar

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import server.Message

trait BaseSpec extends FlatSpec
                       with BeforeAndAfter
                       with BeforeAndAfterAll
                       with Matchers
                       with ScalaFutures
                       with GeneratorDrivenPropertyChecks
{

  val aliceId = 0
  val bobId = 1
  val eveId = 2

  val genPastDate =
    for {
      date <- Gen.chooseNum(1, 30)
      hr  <- Gen.chooseNum(0, 23)
      min <- Gen.chooseNum(0, 59)
      sec <- Gen.chooseNum(0, 59)
    } yield {
      val cal = Calendar.getInstance()
      cal.set(2016, Calendar.JUNE, date, hr, min, sec)
      cal.getTime
    }

  implicit val arbPastDate = Arbitrary(genPastDate)

  val genMsg =
    for {
      userId <- Gen.oneOf(aliceId, bobId, eveId)
      friendId <- Gen.oneOf(Seq(aliceId, bobId, eveId).filterNot(_ == userId))
      text <- Gen.alphaStr.map(_.take(10))
      sender <- Gen.oneOf(true, false)
      read <- Gen.oneOf(true, false)
      past <- Gen.posNum[Long].map(_ * 1000)
      date <- genPastDate
    } yield Message(userId, friendId, text, sender, read, deleted = false, date)

  implicit val arbMsg = Arbitrary(genMsg)
}
