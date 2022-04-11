package ar.katas.it

import ar.katas.actions.RegisterUser
import ar.katas.domain.user._
import ar.katas.infrastructure.dynamodb.client.{DynamoClient, UsersClient}
import cats.effect.IO
import meteor.{Client, DynamoDbType, KeyDef}
import munit.CatsEffectSuite
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._
import TestUtilities.cleanTable

class RegisterUserDynamoIT extends CatsEffectSuite {

  test("Register user") {
    val john = User(Username("Jhon Bauer"), Nickname("@johnbauer1"))

    val resource = DynamoClient.localDefault
    resource.use { client =>
      for {
        _ <- cleanTable(client)
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
        _ <- register.exec(user)
        _ <- register.exec(user)
      } yield ()
    }

    exec.attempt.assertEquals(Left(alreadyRegistered))
  }
}

object TestUtilities {
  def createUserTable(client: Client[IO]): IO[Unit] =
    client
      .createCompositeKeysTable(
        tableName = "Users",
        partitionKeyDef = KeyDef[String]("nickname", DynamoDbType.S),
        sortKeyDef = KeyDef[String]("category", DynamoDbType.S),
        billingMode = BillingMode.PAY_PER_REQUEST,
        attributeDefinition = Map(
          "nickname" -> DynamoDbType.S,
          "category" -> DynamoDbType.S
        )
      )

  def deleteUserTable(client: DynamoDbAsyncClient): IO[DeleteTableResponse] =
    IO.fromCompletableFuture[DeleteTableResponse](
      IO(
        client.deleteTable(
          DeleteTableRequest.builder().tableName("Users").build()
        )
      )
    )

  def cleanTable(client: DynamoDbAsyncClient): IO[Unit] =
    IO.fromCompletableFuture(
      IO(
        client.describeTable(
          DescribeTableRequest.builder().tableName("Users").build()
        )
      )
    ).attempt
      .flatMap {
        case Left(_: ResourceNotFoundException) =>
          createUserTable(Client[IO](client))
        case Right(_) =>
          deleteUserTable(client) *> createUserTable(Client[IO](client))
      }

}
