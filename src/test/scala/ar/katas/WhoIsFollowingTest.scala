package ar.katas

import ar.katas.actions.{FollowUser, RegisterUser, WhoIsFollowing}
import ar.katas.domain.user._
import ar.katas.infrastructure.persistence.inmemory.{
  FollowsInMemory,
  UsersInMemory
}
import munit.CatsEffectSuite

class WhoIsFollowingTest extends CatsEffectSuite {

  test("Anyone can ask who is following who") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))

    for {
      users <- UsersInMemory.make
      follows <- FollowsInMemory.make
      whosFollowing = WhoIsFollowing.make(follows, users)
      register = RegisterUser.make(users)
      follow = FollowUser.make(follows)

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
