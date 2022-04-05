package ar.katas.actions

import ar.katas.domain.Follows
import ar.katas.domain.following.{FolloweeId, FollowerId}
import ar.katas.domain.user.Nickname
import cats.effect.IO

trait FollowUser {
  def exec(followerId: Nickname, followeeId: Nickname): IO[Unit]
}

object FollowUser {
  def make(follows: Follows): FollowUser =
    (followerId: Nickname, followeeId: Nickname) =>
      follows.persistFollowing(
        FollowerId(followerId.value),
        FolloweeId(followeeId.value)
      )
}
