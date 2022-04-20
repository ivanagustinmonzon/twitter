package ar.katas.infrastructure.persistence.dynamodb

import ar.katas.infrastructure.persistence.dynamodb.codecs._
import ar.katas.infrastructure.persistence.dynamodb.schemas._
import ar.katas.model.Users
import ar.katas.model.user._
import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxTuple3Semigroupal}
import dynosaur.Schema
import meteor.api.hi.CompositeTable
import meteor.codec.Codec
import meteor.dynosaur.formats.conversions.schemaToCodec
import meteor.syntax.RichWriteAttributeValue
import meteor.{DynamoDbType, Expression, KeyDef}
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object UsersClient {
  import UsersDynamodb._
  def make(client: DynamoDbAsyncClient): Users =
    new Users {
      override def get(nickname: Nickname): IO[User] =
        usersTable(client)
          .get[UserWithCategory](
            partitionKey = PartitionKey(nickname),
            sortKey = SortKey(nickname.value),
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
          partitionKey = PartitionKey(user.nickname),
          sortKey = SortKey(user.nickname.value),
          update = Expression(
            expression = "SET #realname = :curValue",
            attributeNames = Map("#realname" -> "realname"),
            attributeValues = Map(":curValue" -> user.username.asAttributeValue)
          )
        )
    }
}

private object UsersDynamodb {
  val sortKeySchema: Schema[SortKey] = schemas.sortKeySchema("USER")
  implicit val sortKeyCodec: Codec[SortKey] = codecs.sortKeyCodec("USER")
  lazy val usersTable: DynamoDbAsyncClient => CustomCompositeKey =
    jclient =>
      CompositeTable(
        "Users",
        KeyDef[PartitionKey]("nickname", DynamoDbType.S),
        KeyDef[SortKey]("category", DynamoDbType.S),
        jclient
      )

  val usernameSchema: Schema[Username] =
    Schema[String].imap(Username.apply)(it => it.value)

  implicit val usernameCodec: Codec[Username] = schemaToCodec(usernameSchema)

  final case class UserWithCategory(
      username: Username,
      nickname: Nickname,
      category: String
  )

  val userWithCategory: Schema[UserWithCategory] =
    Schema.record[UserWithCategory](field =>
      (
        field("nickname", it => PartitionKey(it.nickname))(
          Schema[PartitionKey](partitionKeySchema)
        ),
        field("realname", it => it.username)(
          Schema[Username](usernameSchema)
        ),
        field("category", it => SortKey(it.category))(
          Schema[SortKey](sortKeySchema)
        )
      ).mapN { case (nickname, username, category) =>
        UserWithCategory(username, nickname.nickname, category.value)
      }
    )

  implicit val userCodec: Codec[UserWithCategory] = schemaToCodec(
    userWithCategory
  )
}
