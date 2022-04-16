package ar.katas.persistence

import ar.katas.actions.{FollowUser, RegisterUser, WhoIsFollowing}
import ar.katas.model.user._
import ar.katas.infrastructure.persistence.dynamodb.client.DynamoClient
import ar.katas.infrastructure.persistence.dynamodb.{FollowsClient, UsersClient}
import munit.CatsEffectSuite

class WhoIsFollowingDynamoIT extends CatsEffectSuite {

  test("Anyone can ask who is following who") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))

    val resource = DynamoClient.localDefault

    resource.use { client =>
      val users = UsersClient.make(client)
      val follows = FollowsClient.make(client)
      val whosFollowing = WhoIsFollowing.make(follows, users)
      val register = RegisterUser.make(users)
      val follow = FollowUser.make(follows)

      for {
        _ <- cleanUsersTable(client)
        _ <- register.exec(john)
        _ <- register.exec(jake)
        _ <- register.exec(jane)

        _ <- follow.exec(john.nickname, jake.nickname)
        _ <- follow.exec(john.nickname, jane.nickname)

        followees <- whosFollowing.exec(john.nickname)
        expectedFollowees = List(jake, jane)

      } yield assert(expectedFollowees.forall(user => followees.contains(user)))

    }
  }
}
