package ar.katas.infrastructure.persistence

import ar.katas.model.user.{Nickname, Username}
import dynosaur.Schema
import meteor.codec.Codec

package object dynamodb {

  final case class NicknameIndex(nickname: Nickname)
  final case class CategoryIndex(value: String)

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

  }
  object codecs {
    import meteor.dynosaur.formats.conversions._
    import schemas._
    implicit val usernameCodec: Codec[Username] = schemaToCodec(usernameSchema)
    implicit val nicknameIndexCodec: Codec[NicknameIndex] = schemaToCodec(
      nicknameIndexSchema
    )
    implicit val categoryIndexCodec: Codec[CategoryIndex] = schemaToCodec(
      categoryIndexSchema
    )

  }

}
