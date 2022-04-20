package ar.katas.modules

import ar.katas.actions.{FollowUser, RegisterUser, UpdateUser, WhoIsFollowing}

final case class AppServices private (
    registerUser: RegisterUser,
    updateUser: UpdateUser,
    followUser: FollowUser,
    whoIsFollowing: WhoIsFollowing
)

object AppServices {
  def make(resources: AppResources): AppServices = {
    new AppServices(
      RegisterUser.make(resources.users),
      UpdateUser.make(resources.users),
      FollowUser.make(resources.follows),
      WhoIsFollowing.make(resources.follows, resources.users)
    )
  }
}
