import cats.data.XorT
import cats.free.Free
import cats.std.FutureInstances
import riotapi.free.{RiotApiF, RiotApiOps}

import scala.concurrent.Future

package object riotapi extends AnyRef with RiotApiOps with FutureInstances {
  type RiotApiOp[A] = Free[RiotApiF, A]
  type Response[A] = XorT[Future, ApiError, A]
  val RiotApi = free.interp.RiotEndpoint
}
