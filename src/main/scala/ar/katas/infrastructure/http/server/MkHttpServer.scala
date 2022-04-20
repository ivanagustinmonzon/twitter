package ar.katas.infrastructure.http.server

import cats.effect.IO
import cats.effect.kernel.Resource
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner

trait MkHttpServer {
  def newEmber(
      cfg: HttpServerConfig,
      httpApp: HttpApp[IO]
  ): Resource[IO, Server]
}

object MkHttpServer {
  val make: MkHttpServer =
    (cfg: HttpServerConfig, httpApp: HttpApp[IO]) =>
      EmberServerBuilder
        .default[IO]
        .withHost(cfg.host)
        .withPort(cfg.port)
        .withHttpApp(httpApp)
        .build
        .evalTap(showEmberBanner)

  private def showEmberBanner(s: Server): IO[Unit] =
    IO.println(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

}
