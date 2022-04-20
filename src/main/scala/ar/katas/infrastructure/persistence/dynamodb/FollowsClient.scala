package ar.katas.infrastructure.persistence.dynamodb

import ar.katas.infrastructure.persistence.dynamodb.FollowsDynamodb.{
  sortKeyCodec,
  _
}
import ar.katas.infrastructure.persistence.dynamodb.codecs._
import ar.katas.infrastructure.persistence.dynamodb.schemas._
import ar.katas.model.following.{FolloweeId, FollowerId, Following}
import ar.katas.model.user.Nickname
import ar.katas.model.{Follows, following}
import cats.effect.IO
import cats.syntax.apply._
import dynosaur.Schema
import meteor._
import meteor.api.hi.CompositeTable
import meteor.codec.Codec
import meteor.dynosaur.formats.conversions._
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object FollowsClient {
  def make(client: DynamoDbAsyncClient): Follows =
    new Follows {
      override def persistFollowing(
          idFollower: following.FollowerId,
          idFollowee: following.FolloweeId
      ): IO[Unit] =
        followsTable(client).put(
          Following(
            idFollower,
            idFollowee
          )
        )

      override def isFollowing(
          idFollower: following.FollowerId,
          idFollowee: following.FolloweeId
      ): IO[Boolean] = followsTable(client)
        .get[Following](
          partitionKey = PartitionKey(Nickname(idFollower.value)),
          sortKey = SortKey(idFollowee.value),
          consistentRead = true
        )
        .map(_.nonEmpty)

      override def getFollowees(
          idFollower: following.FollowerId
      ): IO[List[following.FolloweeId]] =
        followsTable(client)
          .retrieve[Following](
            Query[PartitionKey, SortKey](
              partitionKey = PartitionKey(Nickname(idFollower.value)),
              sortKeyQuery = SortKeyQuery.BeginsWith(SortKey("")),
              filter = Expression.empty
            ),
            consistentRead = true,
            limit = 2
          )
          .map(_.followeeId)
          .compile
          .toList
    }
}

private object FollowsDynamodb {
  val sortKeySchema: Schema[SortKey] = schemas.sortKeySchema("FOLLOWED")
  implicit val sortKeyCodec: Codec[SortKey] = codecs.sortKeyCodec("FOLLOWED")

  lazy val followsTable: DynamoDbAsyncClient => CustomCompositeKey =
    jclient =>
      CompositeTable(
        "Users",
        KeyDef[PartitionKey]("nickname", DynamoDbType.S),
        KeyDef[SortKey]("category", DynamoDbType.S),
        jclient
      )

  val followingSchema: Schema[Following] = Schema.record(field =>
    (
      field("nickname", it => PartitionKey(Nickname(it.followerId.value)))(
        Schema[PartitionKey](partitionKeySchema)
      ),
      field("category", it => SortKey(it.followeeId.value))(
        Schema[SortKey](sortKeySchema)
      )
    ).mapN { case (nickname, category) =>
      Following(
        FollowerId(nickname.nickname.value),
        FolloweeId(category.value)
      )
    }
  )

  implicit val followingCodec: Codec[Following] = schemaToCodec(followingSchema)

}
