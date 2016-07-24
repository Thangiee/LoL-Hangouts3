package com.thangiee.lolhangouts3.free.interp

import cats._
import com.pixplicity.easyprefs.library.Prefs
import com.thangiee.lolhangouts3.KVStore
import com.thangiee.lolhangouts3.free.KVStoreA
import com.thangiee.lolhangouts3.free.KVStoreA.{Delete, Get, Put}
import play.api.libs.json.{Json, Reads}

import scala.language.higherKinds

trait KVStoreInterp[M[_]] {
  type Interpreter = (KVStoreA ~> M)
  def interpreter: Interpreter
  def run[A](ops: KVStore[A])(implicit M: Monad[M]): M[A] = ops.foldMap(interpreter)
}

object PrefStore extends KVStoreInterp[Id] {
  val interpreter: Interpreter = new Interpreter {
    def apply[A](fa: KVStoreA[A]): Id[A] = fa match {
      case Get(key, fmt)        => Json.parse(Prefs.getString(key, "{}")).asOpt[A](fmt.asInstanceOf[Reads[A]])
      case Put(key, value, fmt) => Prefs.putString(key, Json.toJson(value)(fmt).toString())
      case Delete(key)          => Prefs.remove(key)
    }
  }
}
