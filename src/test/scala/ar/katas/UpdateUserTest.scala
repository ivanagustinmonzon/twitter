package ar.katas

import ar.katas.actions.{RegisterUser, UpdateUser}
import ar.katas.domain.UsersService
import ar.katas.domain.user._
import ar.katas.infrastructure.UsersInMemory
import munit.CatsEffectSuite

class UpdateUserTest extends CatsEffectSuite {

  test("A user can update his real name") {
    val user = User(Username("Jack Bauer"), Nickname("@jack"))
    val updatedUser = user.copy(username = Username("Jhon"))

    val exec = for {
      users <- UsersInMemory.make
      usersService = UsersService.make(users)
      register = RegisterUser.make(usersService)
      update = UpdateUser.make(usersService)

      _ <- register.exec(user)
      _ <- update.exec(updatedUser)
      actualUser <- users.get(updatedUser.nickname)
    } yield actualUser

    exec.map(user => assertEquals(user, updatedUser))
  }
}
