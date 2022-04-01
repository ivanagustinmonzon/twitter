package ar.katas.domain

import ar.katas.domain.following.Following
import cats.effect.IO

trait FollowsService {
  def create(following: Following): IO[Unit]
}

object FollowsService{
  def make(follows: Follows): FollowsService = new FollowsService {
    override def create(following: Following): IO[Unit] =
      follows.persistFollowing(following.followerId, following.followeeId)
  }
}
