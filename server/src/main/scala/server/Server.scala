package server

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LogEntry
import akka.stream.ActorMaterializer
import play.api.libs.json.{Json, Reads, Writes}
import share.Api

import scala.io.StdIn

object AutowireServer extends autowire.Server[String, Reads, Writes] {
  def read[Result: Reads](p: String): Result = Json.parse(p).as[Result]
  def write[Result: Writes](r: Result): String = Json.toJson(r).toString()
}

object Server extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val requestLog = (req: HttpRequest, info: String) =>
    s"\nRequest $info:" +
      s"\n\t${req.method} ${req.uri}" +
      s"\n\tHeaders: ${req.headers.mkString(", ")}" +
      s"\n\t${req.entity.toString.split("\n").take(3).mkString("\n ")}"

  def customLogging(req: HttpRequest): Any => Option[LogEntry] = {
    case Complete(response) => Some(LogEntry(requestLog(req, s"[${response.status}]"), Logging.InfoLevel))
    case Rejected(_)        => Some(LogEntry(requestLog(req, "[Rejected]"), Logging.InfoLevel))
    case _                  => None // other kind of responses
  }

  val routes = logRequestResult(customLogging _) {
    get {
      path("api" / Segments) { segs =>
        parameterMap { params =>
          complete {
            AutowireServer.route[Api](ApiImpl)(autowire.Core.Request(segs, params))
          }
        }
      }
    }
  }

  val binding = Http().bindAndHandle(routes, "0.0.0.0", 80)
  binding.map(server => println(s"Server listening on ${server.localAddress.getHostName}:${server.localAddress.getPort}"))
  StdIn.readLine()

  binding
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}

