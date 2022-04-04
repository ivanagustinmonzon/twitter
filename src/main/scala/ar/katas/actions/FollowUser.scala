package ar.katas.actions

import ar.katas.domain.FollowsService
import ar.katas.domain.following.{FolloweeId, FollowerId, Following}
import ar.katas.domain.user.Nickname
import cats.effect.IO

trait FollowUser {
  def exec(followerId: Nickname, followeeId: Nickname): IO[Unit]
}

object FollowUser {
  def make(followsUserService: FollowsService): FollowUser =
    (followerId: Nickname, followeeId: Nickname) =>
      followsUserService.create(
        Following(FollowerId(followerId.value), FolloweeId(followeeId.value))
      )
}
