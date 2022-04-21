package ar.katas.modules

import ar.katas.actions.{
  FollowUser,
  RegisterUser,
  RequestTweets,
  TweetMessage,
  UpdateUser,
  WhoIsFollowing
}

final case class AppServices private (
    registerUser: RegisterUser,
    updateUser: UpdateUser,
    followUser: FollowUser,
    whoIsFollowing: WhoIsFollowing,
    tweetMessage: TweetMessage,
    requestTweets: RequestTweets
)

object AppServices {
  def make(res: AppResources): AppServices = {
    new AppServices(
      RegisterUser.make(res.users),
      UpdateUser.make(res.users),
      FollowUser.make(res.follows, res.users),
      WhoIsFollowing.make(res.follows, res.users),
      TweetMessage.make(res.tweets, res.users),
      RequestTweets.make(res.tweets, res.users)
    )
  }
}
