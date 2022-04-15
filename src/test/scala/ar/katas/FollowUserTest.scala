package ar.katas

import ar.katas.actions.{FollowUser, RegisterUser}
import ar.katas.domain.following.{FolloweeId, FollowerId}
import ar.katas.domain.user._
import ar.katas.infrastructure.persistence.inmemory._
import munit.CatsEffectSuite

class FollowUserTest extends CatsEffectSuite {

  test("A user can follow other users") {
    val john = User(Username("Jhon Bauer"), Nickname("@jb"))
    val jake = User(Username("Jake Doe"), Nickname("@jd"))
    val jane = User(Username("Jane Perez"), Nickname("@jp"))

    for {
      users <- UsersInMemory.make
      follows <- FollowsInMemory.make
      register = RegisterUser.make(users)
      follow = FollowUser.make(follows)

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
  }
}
