package ar.katas.model

import ar.katas.model.user.Nickname
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object following {

  final case class FolloweeId(value: String)

  final case class FolloweeParam(followeeId: String) {
    val toDomain: user.Nickname = Nickname(followeeId)
  }
  object FolloweeParam {
    implicit val d: Decoder[FolloweeParam] = deriveDecoder
    implicit val e: Encoder[FolloweeParam] = deriveEncoder
  }

  final case class FollowerId(value: String)

  final case class Following(followerId: FollowerId, followeeId: FolloweeId)
}
