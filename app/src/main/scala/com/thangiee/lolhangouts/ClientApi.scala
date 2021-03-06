package com.thangiee.lolhangouts

import java.net.{ConnectException, SocketTimeoutException}

import cats.instances.all._
import cats.syntax.all._
import lolchat.data.AsyncResult
import play.api.libs.json.{Json, Reads, Writes}
import share.Api
import share.ServerConfig._

import scala.concurrent.Future
import scala.concurrent.duration._

private object Client extends autowire.Client[String, Reads, Writes] {

  def doCall(req: Request): Future[String] = Future {
    scalaj.http.Http(s"https://lolhangouts.com/api/" + req.path.mkString("/"))
      .timeout(connTimeoutMs = 3.seconds.toMillis.toInt, readTimeoutMs = 3.seconds.toMillis.toInt)
      .params(req.args)
      .asString.body
  }

  def read[Result: Reads](p: String): Result = Json.parse(p).as[Result]

  def write[Result: Writes](r: Result): String = Json.toJson(r).toString()

}

object ClientApi {
  val clientApi = Client[Api]

  implicit class AsyncCall[T](val api: Future[T]) extends AnyVal {
    def toAsyncResult = AsyncResult(
      api.attempt.map(_.leftMap {
        case ex: ConnectException       => Error(408, "No Internet connection.", ex)
        case ex: SocketTimeoutException => Error(503, "Unable to connect to the server", ex)
        case ex                         => Error(500, "Unexpected Error occurred", ex)
      })
    )
  }

}