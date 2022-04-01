package ar.katas.domain

object following {

  final case class FolloweeId(value: String) extends AnyVal
  final case class FollowerId(value: String) extends AnyVal

  final case class Following(followerId: FollowerId, followeeId: FolloweeId)
}
