package ar.katas.infrastructure

import ar.katas.domain.Follows
import ar.katas.domain.following.{FolloweeId, FollowerId}
import cats.effect.IO

object FollowsInMemory {
  val make: IO[Follows] =
    IO.ref(Map.empty[FollowerId, List[FolloweeId]])
      .map(database => {
        new Follows {
          override def persistFollowing(
              idFollower: FollowerId,
              idFollowee: FolloweeId
          ): IO[Unit] =
            database.update(m => {
              val updatedList =
                idFollowee :: m.get(idFollower).getOrElse(List.empty)
              m + (idFollower -> updatedList)
            })

          override def isFollowing(
              idFollower: FollowerId,
              idFollowee: FolloweeId
          ): IO[Boolean] =
            database.get.map(m =>
              m
                .get(idFollower)
                .map(l => l.contains(idFollowee))
                .getOrElse(false)
            )

          override def getFollowees(
              idFollower: FollowerId
          ): IO[List[FolloweeId]] =
            database.get
              .map(m => m.get(idFollower).getOrElse(List.empty[FolloweeId]))
        }
      })

}
