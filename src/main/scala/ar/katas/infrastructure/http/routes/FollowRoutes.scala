package ar.katas.infrastructure.http.routes

import ar.katas.actions.{FollowUser, WhoIsFollowing}
import ar.katas.infrastructure.http.routes.FollowCodecs._
import ar.katas.infrastructure.http.routes.UserCodecs._
import ar.katas.model.user
import ar.katas.model.user._
import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

final case class FollowRoutes(
    followUser: FollowUser,
    whoIsFollowing: WhoIsFollowing
) extends Http4sDsl[IO] {

  private[routes] val usersPrefixPath = "/users"
  private[routes] val prefixPath = "/follows"

  private val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case followee @ POST -> Root / followerId / "follows" =>
      followee
        .as[FolloweeParam]
        .flatMap { followeeId =>
          followUser.exec(Nickname(followerId), followeeId.toDomain)
        }
        .flatMap(_ => NoContent())
        .handleErrorWith { case UserNotFound(_) =>
          NotFound()
        }

    case GET -> Root / followerId / "follows" =>
      whoIsFollowing
        .exec(Nickname(followerId))
        .map(
          _.map(UserParam.apply)
        )
        .flatMap(Ok(_))
        .handleErrorWith { case UserNotFound(_) =>
          NotFound()
        }
  }

  val routes: HttpRoutes[IO] = Router(
    usersPrefixPath -> httpRoutes
  )

}

private object FollowCodecs {
  implicit val f: EntityDecoder[IO, FolloweeParam] = jsonOf[IO, FolloweeParam]

  implicit val g: EntityEncoder[IO, List[UserParam]] =
    jsonEncoderOf[IO, List[UserParam]]

  final case class FolloweeParam(followeeId: String) {
    val toDomain: user.Nickname = Nickname(followeeId)
  }
  object FolloweeParam {
    implicit val d: Decoder[FolloweeParam] = deriveDecoder
    implicit val e: Encoder[FolloweeParam] = deriveEncoder
  }

}
