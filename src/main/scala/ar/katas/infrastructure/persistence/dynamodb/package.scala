package ar.katas.infrastructure.persistence

import ar.katas.model.user.Nickname
import dynosaur.Schema
import meteor.codec.Codec

package object dynamodb {

  final case class NicknameIndex(nickname: Nickname)
  final case class CategoryIndex(value: String)

  object schemas {

    val nicknameIndexSchema: Schema[NicknameIndex] =
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
    implicit val partitionKeyCodec: Codec[NicknameIndex] = schemaToCodec(
      nicknameIndexSchema
    )
    implicit val sortKeyCodec: Codec[CategoryIndex] = schemaToCodec(
      categoryIndexSchema
    )

  }

}
