package ar.katas.modules

import ar.katas.infrastructure.http.routes.{FollowRoutes, UserRoutes}
import cats.effect.IO
import cats.syntax.all._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware.{
  AutoSlash,
  RequestLogger,
  ResponseLogger,
  Timeout
}
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.duration.DurationInt

object HttpApi {
  def make(services: AppServices): HttpApi = new HttpApi(services) {}
}

sealed abstract class HttpApi private (services: AppServices) {

  private val userRoutes =
    UserRoutes(services.registerUser, services.updateUser).routes
  private val followRoutes =
    FollowRoutes(services.followUser, services.whoIsFollowing).routes
  private val appRoutes = userRoutes <+> followRoutes

  private val loggers: HttpApp[IO] => HttpApp[IO] = {
    { http: HttpApp[IO] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[IO] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  private val middleware: HttpRoutes[IO] => HttpRoutes[IO] = {
    { http: HttpRoutes[IO] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[IO] =>
      Timeout(60.seconds)(http)
    }
  }

  def httpApp: HttpApp[IO] = loggers(middleware(appRoutes).orNotFound)
}
