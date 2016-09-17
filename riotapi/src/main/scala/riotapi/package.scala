import riotapi.free.RiotApi

package object riotapi extends AnyRef with cats.instances.FutureInstances {
  type RiotApiOp[A] = RiotApi.ops.RiotApiOp[A]
  val RiotApiOps = RiotApi.ops
  val RiotApiEndpoint = free.interp.RiotEndpoint
}
