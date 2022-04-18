package ar.katas.infrastructure.http.routes

import ar.katas.actions.{RegisterUser, UpdateUser}
import ar.katas.infrastructure.http.routes.Params._
import ar.katas.model.user._
import cats.effect.IO
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpRoutes}

final case class UserRoutes(
    registerUser: RegisterUser,
    updateUser: UpdateUser
) extends Http4sDsl[IO] {

  private[routes] val prefixPath = "/users"

  private val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case usr @ POST -> Root =>
      usr
        .as[UserParam]
        .flatMap { user =>
          registerUser.exec(user.toDomain)
        }
        .flatMap(Created(_))
        .handleErrorWith { case UserAlreadyRegistered(_) =>
          Conflict()
        }

    case update @ PUT -> Root / nickname =>
      update
        .as[UpdateParam]
        .flatMap(newUsername =>
          updateUser.exec(
            User(newUsername.toDomain, Nickname(nickname))
          )
        )
        .flatMap(Ok(_))
        .handleErrorWith { case UserNotFound(_) => NotFound() }
  }

  val routes: HttpRoutes[IO] = Router(
    prefixPath -> httpRoutes
  )

}

private object Params {
  implicit val d: EntityDecoder[IO, UserParam] = jsonOf[IO, UserParam]
  implicit val e: EntityDecoder[IO, UpdateParam] = jsonOf[IO, UpdateParam]

}
