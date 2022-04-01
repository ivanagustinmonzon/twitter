package ar.katas

import ar.katas.actions.{FollowUser, RegisterUser, WhoIsFollowing}
import ar.katas.domain._
import ar.katas.domain.user._
import ar.katas.infrastructure.{FollowsInMemory, UsersInMemory}
import munit.CatsEffectSuite

class WhoIsFollowingTest extends CatsEffectSuite {

  test("Anyone can ask who is following who") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))
    val expectedList = List(jake, jane)

    for {
      users <- UsersInMemory.make
      usersService = UsersService.make(users)
      register = RegisterUser.make(usersService)

      _ <- register.exec(john)
      _ <- register.exec(jake)
      _ <- register.exec(jane)

      follows <- FollowsInMemory.make
      followsService = FollowsService.make(follows, users)
      follow = FollowUser.make(followsService)

      _ <- follow.exec(john.nickname, jake.nickname)
      _ <- follow.exec(john.nickname, jane.nickname)

      whosFollowing = WhoIsFollowing.make(followsService)
      list <- whosFollowing.exec(john.nickname)

    } yield assert(list.forall(user => expectedList.contains(user)))
  }
}