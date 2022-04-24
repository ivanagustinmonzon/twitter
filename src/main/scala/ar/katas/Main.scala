package ar.katas

import ar.katas.infrastructure.http.server.{HttpServerConfig, MkHttpServer}
import ar.katas.modules._
import cats.effect._

object Main extends IOApp.Simple {

  def run: IO[Unit] = program

  def program: IO[Unit] =
    HttpServerConfig
      .make(
        host = "0.0.0.0",
        port = "9000"
      )
      .flatMap { httpCfg =>
        AppResources.make
          .map { appResources =>
            val service = AppServices.make(appResources)
            val api = HttpApi.make(service)
            httpCfg -> api.httpApp
          }
          .flatMap { case (cfg, httpApp) =>
            MkHttpServer.make.newEmber(cfg, httpApp)
          }
          .useForever
      }

}
