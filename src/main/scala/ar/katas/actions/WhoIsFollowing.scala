package ar.katas.actions

import ar.katas.domain.following.FollowerId
import ar.katas.domain.user.{Nickname, User}
import ar.katas.domain.{Follows, Users}
import cats.effect.IO
import cats.syntax.traverse._

trait WhoIsFollowing {
  def exec(followerId: Nickname): IO[List[User]]
}

object WhoIsFollowing {
  def make(follows: Follows, users: Users): WhoIsFollowing =
    (followerId: Nickname) =>
      follows
        .getFollowees(FollowerId(followerId.value))
        .flatMap(followees =>
          followees.traverse(followeeId =>
            users.get(Nickname(followeeId.value))
          )
        )
}
