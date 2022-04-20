package ar.katas.infrastructure.persistence.dynamodb

import ar.katas.model.Users
import ar.katas.model.user._
import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxTuple3Semigroupal}
import dynosaur.Schema
import meteor.api.hi.CompositeTable
import meteor.codec.Codec
import meteor.syntax.RichWriteAttributeValue
import meteor.{DynamoDbType, Expression, KeyDef}
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object UsersClient {
  import UsersDynamodb._
  import codecs._
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
  import meteor.dynosaur.formats.conversions.schemaToCodec
  import schemas._
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

  final case class UserWithCategory(
      username: Username,
      nickname: Nickname,
      category: String
  )

  val userWithCategory: Schema[UserWithCategory] =
    Schema.record[UserWithCategory](field =>
      (
        field("nickname", it => NicknameIndex(it.nickname))(
          Schema[NicknameIndex](nicknameIndexSchema)
        ),
        field("realname", it => it.username)(
          Schema[Username](usernameSchema)
        ),
        field("category", it => CategoryIndex(it.category))(
          Schema[CategoryIndex](categoryIndexSchema)
        )
      ).mapN { case (nickname, username, category) =>
        UserWithCategory(username, nickname.nickname, category.value)
      }
    )

  implicit val userCodec: Codec[UserWithCategory] = schemaToCodec(
    userWithCategory
  )
}
