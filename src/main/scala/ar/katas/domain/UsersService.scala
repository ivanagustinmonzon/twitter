package ar.katas.domain

import ar.katas.domain.user.{User, UserAlreadyRegistered, UserNotFound}
import cats.effect.IO

trait UsersService{
  def register(user:User): IO[Unit]
  def update(user:User): IO[Unit]
}
object UsersService {
  def make(users:Users): UsersService =
    new UsersService {
      override def register(user: User): IO[Unit] =
        users.exists(user.nickname).ifM(
          ifTrue = IO.raiseError(UserAlreadyRegistered(user.nickname)),
          ifFalse = users.persist(user)
        )

      override def update(user: User): IO[Unit] =
         users.exists(user.nickname).ifM(
            ifTrue = users.update(user),
            ifFalse = IO.raiseError(UserNotFound(user.nickname))
         )
    }

}
