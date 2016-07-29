package com.thangiee

import android.content.Context
import cats.free.Free
import cats.data._
import com.thangiee.lolhangouts3.free.KVStoreA
import riotapi._

import scala.language.implicitConversions

package object lolhangouts3 extends AnyRef with Conversions {
  type Ctx = Context
  type KVStore[A] = Free[KVStoreA, A]
  type Error = lolchat.data.Error
  val Error = lolchat.data.Error

  val KVStoreOps = com.thangiee.lolhangouts3.free.KVStoreOps
  val PrefStore = com.thangiee.lolhangouts3.free.interp.PrefStore

  implicit val exeCtx = scala.concurrent.ExecutionContext.Implicits.global

  val riotApi = RiotApi(NonEmptyVector("456267a6-1777-4763-a77f-f3b1f06ed99d"))
}
