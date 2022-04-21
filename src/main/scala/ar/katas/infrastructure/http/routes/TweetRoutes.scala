package ar.katas.infrastructure.http.routes

import ar.katas.actions.{RequestTweets, TweetMessage, WhoIsFollowing}
import ar.katas.infrastructure.http.routes.TweetCodecs._
import ar.katas.model.user
import ar.katas.model.user._
import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

final case class TweetRoutes(
    tweetMessage: TweetMessage,
    requestTweets: RequestTweets
) extends Http4sDsl[IO] {

  private[routes] val usersPrefixPath = "/users"
  private[routes] val prefixPath = "/tweets"

  private val httpRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case followee @ POST -> Root / author / "tweets" =>
      followee
        .as[TweetParam]
        .flatMap { tweet =>
          tweetMessage.exec(Nickname(author), tweet.message)
        }
        .flatMap(_ => Created())
        .handleErrorWith { case UserNotFound(_) =>
          NotFound()
        }

    case GET -> Root / author / "tweets" =>
      requestTweets
        .exec(Nickname(author))
        .map(
          _.map(it => TweetResponse(it.id.value.toString, it.message))
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

private object TweetCodecs {
  implicit val f: EntityDecoder[IO, TweetParam] = jsonOf[IO, TweetParam]
  implicit val g: EntityEncoder[IO, List[TweetResponse]] =
    jsonEncoderOf[IO, List[TweetResponse]]

  final case class TweetParam(message: String) {
    val toDomain: user.Nickname = Nickname(message)
  }
  object TweetParam {
    implicit val d: Decoder[TweetParam] = deriveDecoder
    implicit val e: Encoder[TweetParam] = deriveEncoder
  }
  final case class TweetResponse(id: String, message: String)
  object TweetResponse {
    implicit val d: Decoder[TweetResponse] = deriveDecoder
    implicit val e: Encoder[TweetResponse] = deriveEncoder
  }

}
