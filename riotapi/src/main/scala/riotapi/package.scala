import cats.free.Free
import cats.std.FutureInstances
import riotapi.free.{RiotApiF, RiotApiOps}

package object riotapi extends AnyRef with RiotApiOps with FutureInstances {
  type RiotApiOp[A] = Free[RiotApiF, A]
  val RiotApi = free.interp.RiotEndpoint
}
