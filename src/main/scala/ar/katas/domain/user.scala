package ar.katas.domain

import scala.util.control.NoStackTrace

object user {

  final case class User(username: Username, nickname: Nickname)

  final case class Username(value: String) extends AnyVal

  final case class Nickname(value: String) extends AnyVal

  final case class UserAlreadyRegistered(id: Nickname) extends NoStackTrace

  final case class UserNotFound(id: Nickname) extends NoStackTrace
}
