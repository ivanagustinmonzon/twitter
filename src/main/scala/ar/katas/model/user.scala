package ar.katas.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import shapeless.ops.hlist.HNilHKernel

import scala.util.control.NoStackTrace

object user {

  final case class User(username: Username, nickname: Nickname)

  final case class Username(value: String)

  final case class Nickname(value: String)

  final case class UserAlreadyRegistered(id: Nickname) extends NoStackTrace

  final case class UserNotFound(id: Nickname) extends NoStackTrace

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
