package ar.katas.domain

import ar.katas.domain.user.{User, UserAlreadyRegistered}
import cats.effect.IO

trait UsersService{
  def register(user:User): IO[Unit]
}
object UsersService {
  def make(users:Users): UsersService =
      (user: User) =>
        users.exists(user.nickname).ifM(
          ifTrue = IO.raiseError(UserAlreadyRegistered(user.nickname)),
          ifFalse = users.persist(user)
        )

}
