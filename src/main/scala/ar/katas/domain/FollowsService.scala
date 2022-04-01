package ar.katas.domain

import ar.katas.domain.following.{FollowerId, Following}
import ar.katas.domain.user.{Nickname, User}
import cats.effect.IO

trait FollowsService {
  def create(following: Following): IO[Unit]
  def findAllFollowees(idFollower: FollowerId): IO[List[User]]
}

import cats.syntax.all._
object FollowsService{
  def make(follows: Follows, users: Users): FollowsService = new FollowsService {
    override def create(following: Following): IO[Unit] =
      follows.persistFollowing(following.followerId, following.followeeId)

    override def findAllFollowees(idFollower: FollowerId): IO[List[User]] =
      follows.getFollowees(idFollower)
        .flatMap(list => list.traverse(id =>
          users.get(Nickname(id.value))
        ))
  }
}
