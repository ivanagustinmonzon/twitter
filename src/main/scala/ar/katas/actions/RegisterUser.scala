package ar.katas.actions

import ar.katas.model.Users
import ar.katas.model.user.{User, UserAlreadyRegistered}
import cats.effect.IO

trait RegisterUser {
  def exec(user: User): IO[Unit]
}
object RegisterUser {
  def make(users: Users): RegisterUser =
    (user: User) =>
      users
        .exists(user.nickname)
        .ifM(
          ifTrue = IO.raiseError(UserAlreadyRegistered(user.nickname)),
          ifFalse = users.persist(user)
        )
}
