package ar.katas.actions

import ar.katas.model.{Tweets, Users}
import ar.katas.model.tweet.Tweet
import ar.katas.model.user.Nickname
import cats.effect.IO

trait RequestTweets {
  def exec(userId: Nickname): IO[List[Tweet]]
}

object RequestTweets {
  def make(tweets: Tweets, users: Users): RequestTweets =
    (userId: Nickname) =>
      users.get(userId) *>
        tweets.getTweets(userId)

}
