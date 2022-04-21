package ar.katas.actions

import ar.katas.model.user.Nickname
import ar.katas.model.{Tweets, Users}
import cats.effect.IO

trait TweetMessage {
  def exec(userId: Nickname, message: String): IO[Unit]
}
object TweetMessage {
  def make(tweets: Tweets, users: Users): TweetMessage =
    (userId: Nickname, message: String) =>
      users.get(userId) *>
        tweets.persistTweet(userId, message).void

}
