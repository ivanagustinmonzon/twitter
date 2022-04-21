package ar.katas

import ar.katas.actions.TweetMessage
import ar.katas.infrastructure.persistence.inmemory.TweetsInMemory
import ar.katas.model.Users
import ar.katas.model.user._
import cats.effect.IO
import munit.CatsEffectSuite

class TweetMessageTest extends CatsEffectSuite {
  test("A user can tweet a message") {
    val user = User(Username("user who tweet"), Nickname("@uwt"))

    for {
      tweets <- TweetsInMemory.make
      users = new FakeOkUsers()
      tweetMessage = TweetMessage.make(tweets, users)
      message = "Hello twitter!"

      _ <- tweetMessage.exec(user.nickname, message)
      usrTweets <- tweets.getTweets(user.nickname)
    } yield assert(usrTweets.head.message == message)

  }
}

private class FakeOkUsers extends Users {
  override def get(nickname: Nickname): IO[User] = IO(
    User(Username("foo"), nickname)
  )

  override def persist(user: User): IO[Unit] = ???

  override def exists(id: Nickname): IO[Boolean] = ???

  override def update(user: User): IO[Unit] = ???
}
