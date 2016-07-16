package riotapi

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import riotapi.models.AggregatedStats

trait BaseSpec extends FlatSpec
  with BeforeAndAfter
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures
  with GeneratorDrivenPropertyChecks {

  implicit val arbAggStats: Arbitrary[AggregatedStats] = Arbitrary(
    for {
      asst <- Gen.chooseNum[Int](0, 100)
      kill <- Gen.chooseNum[Int](0, 100)
      death <- Gen.chooseNum[Int](0, 100)
      won <- Gen.chooseNum[Int](0, 100)
      lost <- Gen.chooseNum[Int](0, 100)
    } yield AggregatedStats(asst, kill, death, lost, won + lost, won)
  )
}
