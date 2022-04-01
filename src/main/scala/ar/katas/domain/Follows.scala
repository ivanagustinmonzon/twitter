package ar.katas.domain

import ar.katas.domain.following.{FolloweeId, FollowerId}
import cats.effect.IO

trait Follows {
  def persistFollowing(idFollower: FollowerId, idFollowee: FolloweeId): IO[Unit]
  def isFollowing(idFollower: FollowerId, idFollowee: FolloweeId): IO[Boolean]
}
