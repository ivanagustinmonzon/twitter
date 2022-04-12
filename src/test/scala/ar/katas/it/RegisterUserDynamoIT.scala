package ar.katas.it

import ar.katas.actions.RegisterUser
import ar.katas.domain.user._
import ar.katas.infrastructure.dynamodb.client.{DynamoClient, UsersClient}
import munit.CatsEffectSuite

class RegisterUserDynamoIT extends CatsEffectSuite {

  test("Register user") {
    val john = User(Username("Jhon Bauer"), Nickname("@johnbauer1"))

    val resource = DynamoClient.localDefault
    resource.use { client =>
      for {
        _ <- cleanUsersTable(client)
        users = UsersClient.make(client)
        action = RegisterUser.make(users)
        _ <- action.exec(john)
        hasBeenSaved <- users.exists(john.nickname)
      } yield assert(hasBeenSaved)
    }

  }

  test(
    "If another person has been already registered using the same nickname return error"
  ) {
    val user = User(Username("Jack Bauer"), Nickname("@jackregistered"))
    val alreadyRegistered = UserAlreadyRegistered(user.nickname)

    val resource = DynamoClient.localDefault
    val exec = resource.use { client =>
      val users = UsersClient.make(client)
      val register = RegisterUser.make(users)

      for {
        _ <- cleanUsersTable(client)
        _ <- register.exec(user)
        _ <- register.exec(user)
      } yield ()
    }

    exec.attempt.assertEquals(Left(alreadyRegistered))
  }
}
