package com.thangiee.lolhangouts3.free

import cats.free.Free
import com.thangiee.lolhangouts3.KVStore
import com.thangiee.lolhangouts3.free.KVStoreA.{Delete, Get, Put}
import play.api.libs.json.{Format, Reads}

sealed trait KVStoreA[A]
object KVStoreA {
  case class Get[A: Manifest](key: String, fmt: Reads[A]) extends KVStoreA[Option[A]]
  case class Put[A: Manifest](key: String, value: A, fmt: Format[A]) extends KVStoreA[Unit]
  case class Delete(key: String) extends KVStoreA[Unit]
}

object KVStoreOps {
  def get[T: Manifest](key: String)(implicit fmt: Reads[T]): KVStore[Option[T]] =
    Free.liftF[KVStoreA, Option[T]](Get(key, fmt))

  def put[T: Manifest](key: String, value: T)(implicit fmt: Format[T]): KVStore[Unit] =
    Free.liftF[KVStoreA, Unit](Put(key, value, fmt))

  def delete(key: String): KVStore[Unit] = Free.liftF(Delete(key))

  def update[T: Manifest](key: String, f: T => T)(implicit mt: Format[T]): KVStore[Unit] =
    for {
      vMaybe <- get(key)
      _ <- vMaybe.map(v => put(key, f(v))).getOrElse(Free.pure(()))
    } yield ()
}
