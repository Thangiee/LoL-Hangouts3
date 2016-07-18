package com.thangiee.lolhangouts3.free.interp

import cats._
import com.pixplicity.easyprefs.library.Prefs
import com.thangiee.lolhangouts3.KVStore
import com.thangiee.lolhangouts3.free.KVStoreA
import com.thangiee.lolhangouts3.free.KVStoreA.{Delete, Get, Put}

import scala.language.higherKinds
import scala.util.Try

trait KVStoreInterp[M[_]] {
  type Interpreter = (KVStoreA ~> M)
  def interpreter: Interpreter
  def run[A](ops: KVStore[A])(implicit M: Monad[M]): M[A] = ops.foldMap(interpreter)
}

object PrefStore extends KVStoreInterp[Id] {
  val interpreter: Interpreter = new Interpreter {
    def apply[A](fa: KVStoreA[A]): Id[A] = fa match {
      case Get(key, canStore)        => Try(canStore.fetchFmt(Prefs.getString(key, ""))).toOption
      case Put(key, value, canStore) => Prefs.putString(key, canStore.storeFmt(value))
      case Delete(key)               => Prefs.remove(key)
    }
  }
}
