package ar.katas.infrastructure.http.server

import cats.effect.IO
import cats.syntax.all._
import com.comcast.ip4s.{Host, Port}

case class HttpServerConfig private (host: Host, port: Port)

object HttpServerConfig {
  def make(host: String, port: String): IO[HttpServerConfig] = {
    (
      Host.fromString(host),
      Port.fromString(port)
    ).mapN(new HttpServerConfig(_, _))
      .liftTo[IO]
      .apply(new RuntimeException("Cannot configure http server "))
  }
}
