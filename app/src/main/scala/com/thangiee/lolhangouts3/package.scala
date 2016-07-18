package com.thangiee

import android.content.Context
import cats.free.Free
import com.thangiee.lolhangouts3.free.KVStoreA

import scala.language.implicitConversions

package object lolhangouts3 extends AnyRef with Conversions with AuxFunctions {
  type Ctx = macroid.ContextWrapper
  type KVStore[A] = Free[KVStoreA, A]

  val KVStoreOps = free.KVStoreOps
  val PrefStore = free.interp.PrefStore

  implicit val exeCtx = scala.concurrent.ExecutionContext.Implicits.global

  implicit def macroidCtxToAndroidCtx(implicit ctx: Ctx): Context = ctx.bestAvailable


}
