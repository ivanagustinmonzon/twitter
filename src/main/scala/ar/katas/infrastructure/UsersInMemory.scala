package ar.katas.infrastructure

import cats.effect._
import ar.katas.domain.user._
import ar.katas.domain.Users
import cats.implicits.catsSyntaxOption

object UsersInMemory {
  val make: IO[Users] = {
    IO.ref(Map.empty[Nickname, Username]).map(database => {
        new Users {
          override def persist(user: User): IO[Unit] =
            database.update(m => m + (user.nickname -> user.username))

          override def exists(id: Nickname): IO[Boolean] =
            database.get.map(m => m.contains(id))
        }
    })
  }
}
