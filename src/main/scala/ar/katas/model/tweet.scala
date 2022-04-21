package ar.katas.model

import ar.katas.infrastructure.GenUUID
import ar.katas.model.user.Nickname
import cats.effect.IO

import java.util.UUID

object tweet {
  final case class TweetId private (value: UUID)
  object TweetId {
    def make: IO[TweetId] =
      GenUUID[IO].make
        .map(TweetId.apply)

    def read(uuid: String): IO[TweetId] = GenUUID[IO].read(uuid).map(TweetId(_))
  }

  final case class Tweet(
      id: TweetId,
      author: Nickname,
      message: String
  )

}
