package ar.katas.infrastructure.persistence

import ar.katas.model.user.Nickname
import cats.effect.IO
import dynosaur.Schema
import meteor.api.hi.CompositeTable
import meteor.codec.Codec

package object dynamodb {

  type CustomCompositeKey = CompositeTable[IO, PartitionKey, SortKey]
  final case class PartitionKey(nickname: Nickname)
  final case class SortKey(value: String)

  object schemas {

    val partitionKeySchema: Schema[PartitionKey] =
      Schema[String].imap[PartitionKey](it =>
        PartitionKey(Nickname(it.split("USER#").last))
      )(it => s"USER#${it.nickname.value}")

    def sortKeySchema(prefix: String): Schema[SortKey] =
      Schema[String].imap[SortKey](it => SortKey(it.split(s"$prefix#").last))(
        it => s"$prefix#${it.value}"
      )

  }
  object codecs {
    import meteor.dynosaur.formats.conversions._
    import schemas._
    implicit val partitionKeyCodec: Codec[PartitionKey] = schemaToCodec(
      partitionKeySchema
    )
    def sortKeyCodec(prefix: String): Codec[SortKey] = schemaToCodec(
      sortKeySchema(prefix)
    )

  }

}
