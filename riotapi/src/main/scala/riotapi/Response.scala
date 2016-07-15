package riotapi

import cats.data.{Xor, XorT}
import scala.concurrent.{ExecutionContext, Future}

object Response {

  def apply[A](future: Future[Xor[ApiError, A]]): Response[A] = XorT(future)

  def apply[A](xor: => Xor[ApiError, A])(implicit ec: ExecutionContext): Response[A] = XorT(Future.successful(xor))

  def right[A](a: A)(implicit ec: ExecutionContext): Response[A] = XorT.right[Future, ApiError, A](Future.successful(a))

  def left[A](err: ApiError)(implicit ec: ExecutionContext): Response[A] = XorT.left[Future, ApiError, A](Future.successful(err))

  def pure[A](a: A)(implicit ec: ExecutionContext): Response[A] = XorT.pure[Future, ApiError, A](a)

  def catchNonFatal[A](f: => A, g: Throwable => ApiError)(implicit ec: ExecutionContext): Response[A] =
    XorT(Future(Xor.catchNonFatal(f).leftMap(g)))

  def catchNonFatal[A](f: => A)(implicit ec: ExecutionContext): Response[A] =
    XorT(Future(Xor.catchNonFatal(f).leftMap(err => ApiError(500, err.getMessage))))
  
}
