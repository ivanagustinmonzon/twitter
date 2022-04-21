package ar.katas.actions

import ar.katas.model.{Follows, Users}
import ar.katas.model.following.{FolloweeId, FollowerId}
import ar.katas.model.user.Nickname
import cats.effect.IO

trait FollowUser {
  def exec(followerId: Nickname, followeeId: Nickname): IO[Unit]
}

object FollowUser {
  def make(follows: Follows, users: Users): FollowUser =
    (followerId: Nickname, followeeId: Nickname) =>
      users.get(followeeId) *> users.get(followerId) *>
        follows.persistFollowing(
          FollowerId(followerId.value),
          FolloweeId(followeeId.value)
        )
}
