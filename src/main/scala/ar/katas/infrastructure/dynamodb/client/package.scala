package ar.katas.infrastructure.dynamodb

import ar.katas.domain.user.{Nickname, Username}
import cats.syntax.apply._
import dynosaur.Schema
import meteor.codec.Codec

package object client {

  final case class NicknameIndex(nickname: Nickname)
  final case class CategoryIndex(value: String)
  final case class UserWithCategory(
      username: Username,
      nickname: Nickname,
      category: String
  )

  object schemas {
    val usernameSchema: Schema[Username] =
      Schema[String].imap(Username.apply)(it => it.value)

    implicit val nicknameIndexSchema: Schema[NicknameIndex] =
      Schema[String].imap[NicknameIndex](it =>
        NicknameIndex(Nickname(it.split("USER#").last))
      )(it => s"USER#${it.nickname.value}")

    val categoryIndexSchema: Schema[CategoryIndex] =
      Schema[String].imap[CategoryIndex](it =>
        CategoryIndex(it.split("USER#").last)
      )(it => s"USER#${it.value}")

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
  }
  object codecs {
    import schemas._
    import meteor.dynosaur.formats.conversions._
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

}
