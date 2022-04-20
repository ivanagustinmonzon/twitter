package ar.katas.infrastructure.http.routes

import ar.katas.actions.{RegisterUser, UpdateUser}
import ar.katas.infrastructure.http.routes.UserCodecs._
import ar.katas.model.user._
import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

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

private object UserCodecs {
  implicit val d: EntityDecoder[IO, UserParam] = jsonOf[IO, UserParam]
  implicit val e: EntityDecoder[IO, UpdateParam] = jsonOf[IO, UpdateParam]

  final case class UserParam(nickname: String, realname: String) {
    def toDomain: User = User(Username(realname), Nickname(nickname))
  }
  object UserParam {
    def apply(user: User): UserParam =
      new UserParam(user.nickname.value, user.username.value)
    implicit val encoder: Encoder[UserParam] = deriveEncoder
    implicit val decoder: Decoder[UserParam] = deriveDecoder
  }

  final case class UpdateParam(realname: String) {
    def toDomain: Username = Username(realname)
  }
  object UpdateParam {
    implicit val encoder: Encoder[UpdateParam] = deriveEncoder
    implicit val decoder: Decoder[UpdateParam] = deriveDecoder
  }
}
