package ar.katas.infrastructure.dynamodb.client

import ar.katas.domain.Users
import ar.katas.domain.user._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.apply._
import dynosaur.Schema
import meteor._
import meteor.api.hi.CompositeTable
import meteor.codec.Codec
import meteor.dynosaur.formats.conversions._
import meteor.syntax.RichWriteAttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object UsersClient {
  def make(client: DynamoDbAsyncClient): Users =
    new Users {
      import UsersDynamodb.codecs._
      import UsersDynamodb._
      override def get(nickname: Nickname): IO[User] = {
        CompositeTable[IO, NicknameIndex, CategoryIndex](
          "Users",
          KeyDef[NicknameIndex]("nickname", DynamoDbType.S),
          KeyDef[CategoryIndex]("category", DynamoDbType.S),
          client
        )
          .get[User](
            partitionKey = NicknameIndex(nickname),
            sortKey = CategoryIndex(nickname.value),
            consistentRead = true
          )
          .flatMap {
            case Some(value) => value.pure[IO]
            case None        => IO.raiseError(UserNotFound(nickname))
          }
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
            expression = "#realname = #curValue",
            attributeNames = Map("#realname" -> "realname"),
            attributeValues = Map("#curValue" -> user.username.asAttributeValue)
          )
        )
    }
}

private object UsersDynamodb {
  import codecs._
  def usersTable: DynamoDbAsyncClient => CompositeTable[
    IO,
    NicknameIndex,
    CategoryIndex
  ] =
    jclient =>
      CompositeTable(
        "Users",
        KeyDef[NicknameIndex]("nickname", DynamoDbType.S),
        KeyDef[CategoryIndex]("nickname", DynamoDbType.S),
        jclient
      )

  object codecs {
    implicit val userCodec: Codec[User] = schemaToCodec(userSchema)
    implicit val usernameCodec: Codec[Username] = schemaToCodec(usernameSchema)
    implicit val nicknameIndexCodec: Codec[NicknameIndex] = schemaToCodec(
      nicknameIndexSchema
    )
    implicit val categoryIndexCodec: Codec[CategoryIndex] = schemaToCodec(
      categoryIndexSchema
    )
    implicit val userWithCategoryCodec: Codec[UserWithCategory] = schemaToCodec(
      userWithCategory
    )

  }

  val nicknameSchema: Schema[Nickname] =
    Schema[String].imap(Nickname.apply)(it => it.value)

  val userSchema: Schema[User] =
    Schema.record[User](field =>
      (
        field("nickname", _.nickname.value),
        field("realname", _.username.value)
      ).mapN { case (nickname, username) =>
        User(Username(username), Nickname(nickname))
      }
    )

  implicit val usernameSchema: Schema[Username] =
    Schema[String].imap(Username.apply)(it => it.value)

  final case class NicknameIndex(nickname: Nickname)

  implicit val nicknameIndexSchema: Schema[NicknameIndex] =
    Schema[String].imap[NicknameIndex](it =>
      NicknameIndex(Nickname(it.split("USER#").last))
    )(it => s"USER#${it.nickname.value}")

  final case class CategoryIndex(value: String)
  implicit val categoryIndexSchema: Schema[CategoryIndex] =
    Schema[String].imap[CategoryIndex](it =>
      CategoryIndex(it.split("USER#").last)
    )(it => s"USER#${it.value}")

  final case class UserWithCategory(
      username: Username,
      nickname: Nickname,
      category: String
  )

  val userWithCategory: Schema[UserWithCategory] =
    Schema.record[UserWithCategory](field =>
      (
        field("nickname", it => NicknameIndex(it.nickname))(
          Schema[NicknameIndex]
        ),
        field("realname", it => it.username)(Schema[Username]),
        field("category", it => CategoryIndex(it.category))(
          Schema[CategoryIndex]
        )
      ).mapN { case (nickname, username, category) =>
        UserWithCategory(username, nickname.nickname, category.value)
      }
    )

}
