package ar.katas.infrastructure.persistence.dynamodb.client

import cats.effect.IO
import cats.effect.kernel.Resource
import meteor.Client
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import java.net.URI

object DynamoClient {
  val localDefault: Resource[IO, DynamoDbAsyncClient] =
    Resource
      .fromAutoCloseable[IO, DynamoDbAsyncClient] {
        IO.delay {
          val cred = DefaultCredentialsProvider.create()
          DynamoDbAsyncClient
            .builder()
            .credentialsProvider(cred)
            .region(Region.EU_WEST_1)
            .endpointOverride(new URI("http://localhost:8000"))
            .build()
        }
      }

  val localDefaultHI: Resource[IO, Client[IO]] =
    localDefault.map(jclient => Client[IO](jclient))

}
