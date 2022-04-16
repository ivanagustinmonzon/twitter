package ar.katas.model

import ar.katas.model.following.{FolloweeId, FollowerId}
import cats.effect.IO

trait Follows {
  def persistFollowing(idFollower: FollowerId, idFollowee: FolloweeId): IO[Unit]
  def isFollowing(idFollower: FollowerId, idFollowee: FolloweeId): IO[Boolean]
  def getFollowees(idFollower: FollowerId): IO[List[FolloweeId]]
}
