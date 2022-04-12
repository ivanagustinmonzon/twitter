package ar.katas.infrastructure.dynamodb.client

import ar.katas.domain.Users
import ar.katas.domain.user._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import meteor._
import meteor.api.hi.CompositeTable
import meteor.syntax.RichWriteAttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import UsersDynamodb.usersTable
import codecs._

object UsersClient {
  def make(client: DynamoDbAsyncClient): Users =
    new Users {
      override def get(nickname: Nickname): IO[User] =
        usersTable(client)
          .get[UserWithCategory](
            partitionKey = NicknameIndex(nickname),
            sortKey = CategoryIndex(nickname.value),
            consistentRead = true
          )
          .flatMap {
            case Some(value) => User(value.username, value.nickname).pure[IO]
            case None        => IO.raiseError(UserNotFound(nickname))
          }

      override def persist(user: User): IO[Unit] =
        usersTable(client).put(
          UserWithCategory(
            user.username,
            user.nickname,
            user.nickname.value
          )
        )

      override def exists(id: Nickname): IO[Boolean] =
        get(id).attempt
          .flatMap {
            case Left(_: UserNotFound) => false.pure[IO]
            case Right(_: User)        => true.pure[IO]
            case Left(throwable)       => IO.raiseError(throwable)
          }

      override def update(user: User): IO[Unit] =
        usersTable(client).update(
          partitionKey = NicknameIndex(user.nickname),
          sortKey = CategoryIndex(user.nickname.value),
          update = Expression(
            expression = "SET #realname = :curValue",
            attributeNames = Map("#realname" -> "realname"),
            attributeValues = Map(":curValue" -> user.username.asAttributeValue)
          )
        )
    }
}

private object UsersDynamodb {
  import codecs._
  lazy val usersTable: DynamoDbAsyncClient => CompositeTable[
    IO,
    NicknameIndex,
    CategoryIndex
  ] =
    jclient =>
      CompositeTable(
        "Users",
        KeyDef[NicknameIndex]("nickname", DynamoDbType.S),
        KeyDef[CategoryIndex]("category", DynamoDbType.S),
        jclient
      )

}
