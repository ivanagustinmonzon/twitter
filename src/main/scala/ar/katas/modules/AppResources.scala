package ar.katas.modules

import ar.katas.infrastructure.persistence.dynamodb.{
  DynamoDbResource,
  FollowsClient,
  UsersClient
}
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
    DynamoDbResource.localDefault.map { dynamoClient =>
      AppResources(
        UsersClient.make(dynamoClient),
        FollowsClient.make(dynamoClient)
      )
    }
}
