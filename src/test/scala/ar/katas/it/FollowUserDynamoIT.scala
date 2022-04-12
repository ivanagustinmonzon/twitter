package ar.katas.it

import ar.katas.actions.{FollowUser, RegisterUser}
import ar.katas.domain.following.{FolloweeId, FollowerId}
import ar.katas.domain.user._
import ar.katas.infrastructure.FollowsInMemory
import ar.katas.infrastructure.dynamodb.client.{
  DynamoClient,
  FollowsClient,
  UsersClient
}
import munit.CatsEffectSuite

class FollowUserDynamoIT extends CatsEffectSuite {

  test("A user can follow other users") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))

    val client = DynamoClient.localDefault
    client.use(jclient => {
      val users = UsersClient.make(jclient)
      val follows = FollowsClient.make(jclient)
      val register = RegisterUser.make(users)
      val follow = FollowUser.make(follows)

      for {
        _ <- cleanUsersTable(jclient)
        _ <- register.exec(john)
        _ <- register.exec(jake)
        _ <- register.exec(jane)

        _ <- follow.exec(john.nickname, jake.nickname)
        _ <- follow.exec(john.nickname, jane.nickname)

        isFollowing1 <- follows.isFollowing(
          FollowerId(john.nickname.value),
          FolloweeId(jake.nickname.value)
        )
        isFollowing2 <- follows.isFollowing(
          FollowerId(john.nickname.value),
          FolloweeId(jane.nickname.value)
        )

      } yield assert(isFollowing1 && isFollowing2)
    })
  }
}
