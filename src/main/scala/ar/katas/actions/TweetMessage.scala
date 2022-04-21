package ar.katas.actions

import ar.katas.model.Tweets
import ar.katas.model.user.Nickname
import cats.effect.IO

trait TweetMessage {
  def exec(userId: Nickname, message: String): IO[Unit]
}
object TweetMessage {
  def make(tweets: Tweets): TweetMessage =
    (userId: Nickname, message: String) =>
      tweets.persistTweet(userId, message).void

}
