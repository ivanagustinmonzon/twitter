package ar.katas

import ar.katas.actions.{FollowUser, RegisterUser}
import ar.katas.domain.following.{FolloweeId, FollowerId}
import ar.katas.domain.{FollowsService, UsersService}
import ar.katas.domain.user._
import ar.katas.infrastructure.{FollowsInMemory, UsersInMemory}
import cats.effect.IO
import munit.CatsEffectSuite

class FollowUserTest extends CatsEffectSuite{

  test("A user can follow other users") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))

    for {
      users <- UsersInMemory.make
      usersService = UsersService.make(users)
      register = RegisterUser.make(usersService)

      _ <- register.exec(john)
      _ <- register.exec(jake)
      _ <- register.exec(jane)

      follows <- FollowsInMemory.make
      followsService = FollowsService.make(follows)
      follow = FollowUser.make(followsService)

      _ <- follow.exec(john.nickname, jake.nickname)
      _ <- follow.exec(john.nickname, jane.nickname)


      isFollowing1 <- follows.isFollowing(FollowerId(john.nickname.value), FolloweeId(jake.nickname.value))
      isFollowing2 <- follows.isFollowing(FollowerId(john.nickname.value), FolloweeId(jane.nickname.value))

    } yield assert(isFollowing1 && isFollowing2)
  }
}
