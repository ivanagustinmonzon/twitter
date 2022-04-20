package ar.katas.modules

import ar.katas.infrastructure.persistence.dynamodb.client.DynamoClient
import ar.katas.infrastructure.persistence.dynamodb.{FollowsClient, UsersClient}
import ar.katas.model.{Follows, Users}
import cats.effect.IO
import cats.effect.kernel.Resource

final case class AppResources private (
    users: Users,
    follows: Follows
)

object AppResources {
  // TODO: Should receive resource configurations
  def make: Resource[IO, AppResources] =
    DynamoClient.localDefault.map { dynamoClient =>
      AppResources(
        UsersClient.make(dynamoClient),
        FollowsClient.make(dynamoClient)
      )
    }
}
