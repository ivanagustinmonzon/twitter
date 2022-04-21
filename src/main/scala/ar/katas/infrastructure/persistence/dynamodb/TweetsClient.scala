package ar.katas.infrastructure.persistence.dynamodb

import ar.katas.infrastructure.persistence.dynamodb.codecs.partitionKeyCodec
import ar.katas.infrastructure.persistence.dynamodb.schemas.partitionKeySchema
import ar.katas.model.Tweets
import ar.katas.model.tweet.{Tweet, TweetId}
import ar.katas.model.user.Nickname
import cats.effect.IO
import cats.syntax.all._
import dynosaur.Schema
import meteor._
import meteor.api.hi.CompositeTable
import meteor.codec.Codec
import meteor.dynosaur.formats.conversions.schemaToCodec
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object TweetsClient {
  import TweetsDynamodb._
  def make(db: DynamoDbAsyncClient): Tweets = new Tweets {
    override def persistTweet(id: Nickname, message: String): IO[TweetId] =
      TweetId.make.flatMap { tweetId =>
        tweetsTable(db)
          .put(TweetRecord(tweetId.value.toString, id, message))
          .as(tweetId)
      }

    override def getTweets(user: Nickname): IO[List[Tweet]] =
      tweetsTable(db)
        .retrieve[TweetRecord](
          Query[PartitionKey, SortKey](
            partitionKey = PartitionKey(Nickname(user.value)),
            sortKeyQuery = SortKeyQuery.BeginsWith(SortKey("")),
            filter = Expression.empty
          ),
          consistentRead = true,
          limit = 100
        )
        .evalMap(dto =>
          TweetId
            .read(dto.tweetId)
            .map(uuid => Tweet(uuid, dto.nickname, dto.message))
        )
        .compile
        .toList
  }
}

private object TweetsDynamodb {
  import codecs.partitionKeyCodec
  val sortKeySchema: Schema[SortKey] = schemas.sortKeySchema("TWEET")
  implicit val sortKeyCodec: Codec[SortKey] = codecs.sortKeyCodec("TWEET")

  lazy val tweetsTable: DynamoDbAsyncClient => CustomCompositeKey =
    jclient =>
      CompositeTable(
        "Users",
        KeyDef[PartitionKey]("nickname", DynamoDbType.S),
        KeyDef[SortKey]("category", DynamoDbType.S),
        jclient
      )

  final case class TweetRecord(
      tweetId: String,
      nickname: Nickname,
      message: String
  )

  def tweetRecordSchema: Schema[TweetRecord] = Schema.record { field =>
    (
      field("nickname", it => PartitionKey(it.nickname))(
        Schema[PartitionKey](partitionKeySchema)
      ),
      field("category", it => SortKey(it.tweetId))(
        Schema[SortKey](sortKeySchema)
      ),
      field("message", it => it.message)
    ).mapN { case (nickname, tweetId, message) =>
      TweetRecord(tweetId.value, nickname.nickname, message)
    }

  }

  implicit val tweetRecordCodec: Codec[TweetRecord] = schemaToCodec(
    tweetRecordSchema
  )

}
