package ar.katas.persistence

import ar.katas.actions.{RequestTweets, TweetMessage}
import ar.katas.infrastructure.persistence.dynamodb.{
  DynamoDbResource,
  TweetsClient
}
import ar.katas.model.user._
import munit.CatsEffectSuite

class RequestTweetsDynamoIT extends CatsEffectSuite {
  test("Request a user list of tweets") {
    val user = User(Username("user who tweet a lot"), Nickname("@uwtal"))

    val client = DynamoDbResource.localDefault
    client.use { jclient =>
      val tweets = TweetsClient.make(jclient)
      val tweetMessage = TweetMessage.make(tweets)
      val requestTweets = RequestTweets.make(tweets)
      val message = "Hello twitter!"

      for {
        _ <- cleanUsersTable(jclient)
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

}
