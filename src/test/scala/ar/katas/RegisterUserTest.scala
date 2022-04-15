package ar.katas

import ar.katas.actions.RegisterUser
import ar.katas.domain.user._
import ar.katas.infrastructure.persistence.inmemory.UsersInMemory
import munit.CatsEffectSuite

class RegisterUserTest extends CatsEffectSuite {

  test("A user can register with his real name and nickname") {
    val user = User(Username("fala"), Nickname("Jorge"))
    for {
      users <- UsersInMemory.make
      register = RegisterUser.make(users)

      _ <- register.exec(user)
      registered <- users.exists(user.nickname)
    } yield assert(registered)
  }

  test(
    "If another person has been already registered using the same nickname return error"
  ) {
    val alreadyRegistered = UserAlreadyRegistered(Nickname("@jack"))
    val user = User(Username("Jack Bauer"), Nickname("@jack"))

    val exec = for {
      users <- UsersInMemory.make
      register = RegisterUser.make(users)

      _ <- register.exec(user)
      _ <- register.exec(user)
    } yield ()

    exec.attempt.assertEquals(Left(alreadyRegistered))
  }

}
