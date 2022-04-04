package ar.katas.actions

import ar.katas.domain.UsersService
import ar.katas.domain.user.User
import cats.effect.IO

trait UpdateUser {
  def exec(user: User): IO[Unit]
}

object UpdateUser {
  def make(usersService: UsersService): UpdateUser =
    (user: User) => usersService.update(user)
}
