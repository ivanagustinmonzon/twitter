package ar.katas

import ar.katas.actions.{RequestTweets, TweetMessage}
import ar.katas.infrastructure.persistence.inmemory.TweetsInMemory
import ar.katas.model.Users
import ar.katas.model.user._
import cats.effect.IO
import munit.CatsEffectSuite

class RequestTweetsTest extends CatsEffectSuite {
  test("Request a user list of tweets") {
    val user = User(Username("user who tweet a lot"), Nickname("@uwtal"))

    for {
      tweets <- TweetsInMemory.make
      users = new TestOkUsers()
      tweetMessage = TweetMessage.make(tweets, users)
      requestTweets = RequestTweets.make(tweets, users)
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

private class TestOkUsers extends Users {
  override def get(nickname: Nickname): IO[User] = IO(
    User(Username("foo"), nickname)
  )

  override def persist(user: User): IO[Unit] = ???

  override def exists(id: Nickname): IO[Boolean] = ???

  override def update(user: User): IO[Unit] = ???
}
