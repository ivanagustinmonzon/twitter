package ar.katas

import ar.katas.actions.{RequestTweets, TweetMessage}
import ar.katas.infrastructure.persistence.inmemory.TweetsInMemory
import ar.katas.model.tweet.{Tweet, TweetId}
import ar.katas.model.user._
import ar.katas.model.{Tweets, tweet}
import cats.effect.IO
import munit.CatsEffectSuite

class RequestTweetsTest extends CatsEffectSuite {
  test("Request a user list of tweets") {
    val user = User(Username("user who tweet a lot"), Nickname("@uwtal"))

    for {
      tweets <- TweetsInMemory.make
      tweetMessage = TweetMessage.make(tweets)
      requestTweets = RequestTweets.make(tweets)
      message = "Hello twitter!"

      _ <- tweetMessage.exec(user.nickname, message)
      _ <- tweetMessage.exec(user.nickname, message)
      _ <- tweetMessage.exec(user.nickname, message)
      _ <- tweetMessage.exec(user.nickname, message)
      usrTweets <- requestTweets.exec(user.nickname)
    } yield assert(
      usrTweets.size == 4 && usrTweets.forall(it => it.message == message)
    )
  }
}
