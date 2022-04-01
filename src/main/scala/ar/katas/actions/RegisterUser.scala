package ar.katas.actions

import ar.katas.domain.UsersService
import ar.katas.domain.user.User
import cats.effect.IO

trait RegisterUser {
  def exec(user:User): IO[Unit]
}
object RegisterUser{
  def make(usersService:UsersService): RegisterUser =
      (user: User) =>
        usersService.register(user)
}
