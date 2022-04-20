package ar.katas.infrastructure.http.routes

import ar.katas.actions.{FollowUser, WhoIsFollowing}
import ar.katas.model.following.FolloweeParam
import ar.katas.model.user._
import cats.effect.IO
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import FollowCodecs._

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
          followUser.exec(followeeId.toDomain, Nickname(followerId))
        }
        .flatMap(_ => NoContent())
        .handleErrorWith { case UserNotFound(_) =>
          NotFound()
        }

    case GET -> Root / followerId / "follows" =>
      whoIsFollowing
        .exec(Nickname(followerId))
        .map(
          _.map(UserParam.apply(_))
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
}
