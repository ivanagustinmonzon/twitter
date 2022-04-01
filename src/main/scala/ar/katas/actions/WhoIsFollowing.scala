package ar.katas.actions

import ar.katas.domain.FollowsService
import ar.katas.domain.following.FollowerId
import ar.katas.domain.user.{Nickname, User}
import cats.effect.IO

trait WhoIsFollowing {
  def exec(followerId: Nickname): IO[List[User]]
}

object WhoIsFollowing{
  def make(followsService: FollowsService): WhoIsFollowing =
      (followerId: Nickname) =>
        followsService.findAllFollowees(FollowerId(followerId.value))
}
