package ar.katas.persistence

import cats.effect.IO
import meteor.{Client, DynamoDbType, KeyDef}
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._

package object persistence {
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
          DeleteTableRequest
            .builder()
            .tableName("Users")
            .build()
        )
      )
    )

  def cleanUsersTable(client: DynamoDbAsyncClient): IO[Unit] =
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
        case Left(value) => IO.raiseError(value)
      }

}
