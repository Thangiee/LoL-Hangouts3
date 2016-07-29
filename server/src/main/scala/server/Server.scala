package server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import play.api.libs.json.{Json, Reads, Writes}
import share.{Api, ServerConfig}

object AutowireServer extends autowire.Server[String, Reads, Writes] {
  def read[Result: Reads](p: String): Result = Json.parse(p).as[Result]
  def write[Result: Writes](r: Result): String = Json.toJson(r).toString()
}

object Server extends App with Api {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def echo(txt: String): String = "ECHO from server: " + txt

  val routes = get {
    path("api" / Segments) { segs =>
      parameterMap { params =>
        complete {
          AutowireServer.route[Api](Server)(autowire.Core.Request(segs, params))
        }
      }
    }
  }

  Http().bindAndHandle(routes, ServerConfig.interface, ServerConfig.port)
  println(s"Server listening on ${ServerConfig.interface}:${ServerConfig.port}")
}

