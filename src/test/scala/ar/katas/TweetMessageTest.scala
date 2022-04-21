package ar.katas

import ar.katas.actions.{RequestTweets, TweetMessage}
import ar.katas.infrastructure.persistence.inmemory.TweetsInMemory
import ar.katas.model.tweet.{Tweet, TweetId}
import ar.katas.model.user._
import ar.katas.model.{Tweets, tweet}
import cats.effect.IO
import munit.CatsEffectSuite

class TweetMessageTest extends CatsEffectSuite {
  test("A user can tweet a message") {
    val user = User(Username("user who tweet"), Nickname("@uwt"))

    for {
      tweets <- TweetsInMemory.make
      tweetMessage = TweetMessage.make(tweets)
      message = "Hello twitter!"

      _ <- tweetMessage.exec(user.nickname, message)
      usrTweets <- tweets.getTweets(user.nickname)
    } yield assert(usrTweets.head.message == message)

  }
}
