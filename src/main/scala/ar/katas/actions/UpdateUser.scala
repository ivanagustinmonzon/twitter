package ar.katas.actions

import ar.katas.model.Users
import ar.katas.model.user.{User, UserNotFound}
import cats.effect.IO

trait UpdateUser {
  def exec(user: User): IO[Unit]
}

object UpdateUser {
  def make(users: Users): UpdateUser =
    (user: User) =>
      users.get(user.nickname) *>
        users.update(user)
}
