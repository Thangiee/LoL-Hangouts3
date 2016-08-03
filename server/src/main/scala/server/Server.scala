package server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import play.api.libs.json.{Json, Reads, Writes}
import share.{Api, ServerConfig}

import scala.io.StdIn

object AutowireServer extends autowire.Server[String, Reads, Writes] {
  def read[Result: Reads](p: String): Result = Json.parse(p).as[Result]
  def write[Result: Writes](r: Result): String = Json.toJson(r).toString()
}

object Server extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val routes = get {
    path("api" / Segments) { segs =>
      parameterMap { params =>
        complete {
          AutowireServer.route[Api](ApiImpl)(autowire.Core.Request(segs, params))
        }
      }
    }
  }

  val binding = Http().bindAndHandle(routes, ServerConfig.interface, ServerConfig.port)
  println(s"Server listening on ${ServerConfig.interface}:${ServerConfig.port}")
  StdIn.readLine()

  binding
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
}

