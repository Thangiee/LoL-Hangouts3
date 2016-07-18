package com.thangiee.lolhangouts3.free

import cats.free.Free
import com.thangiee.lolhangouts3.KVStore
import com.thangiee.lolhangouts3.free.KVStoreA.{Delete, Get, Put}
import simulacrum.typeclass

sealed trait KVStoreA[A]
object KVStoreA {
  case class Get[A](key: String, canStore: CanStore[A]) extends KVStoreA[Option[A]]
  case class Put[A](key: String, value: A, canStore: CanStore[A]) extends KVStoreA[Unit]
  case class Delete(key: String) extends KVStoreA[Unit]
}

object KVStoreOps {
  def get[T](key: String)(implicit canStore: CanStore[T]): KVStore[Option[T]] =
    Free.liftF[KVStoreA, Option[T]](Get(key, canStore))

  def put[T](key: String, value: T)(implicit canStore: CanStore[T]): KVStore[Unit] =
    Free.liftF[KVStoreA, Unit](Put(key, value, canStore))

  def delete(key: String): KVStore[Unit] = Free.liftF(Delete(key))

  def update[T](key: String, f: T => T)(implicit canStore: CanStore[T]): KVStore[Unit] =
    for {
      vMaybe <- get(key)
      _ <- vMaybe.map(v => put(key, f(v))).getOrElse(Free.pure(()))
    } yield ()
}

@typeclass trait CanStore[A] {
  def storeFmt(value: A): String
  def fetchFmt(string: String): A
}