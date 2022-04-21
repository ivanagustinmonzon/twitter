package ar.katas.model

import ar.katas.model.tweet.{Tweet, TweetId}
import ar.katas.model.user.Nickname
import cats.effect.IO

trait Tweets {
  def persistTweet(id: Nickname, message: String): IO[TweetId]
  def getTweets(user: Nickname): IO[List[Tweet]]
}
