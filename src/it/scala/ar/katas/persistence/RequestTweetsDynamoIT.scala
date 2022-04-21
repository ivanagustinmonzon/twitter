package ar.katas.persistence

import ar.katas.actions.{RegisterUser, RequestTweets, TweetMessage}
import ar.katas.infrastructure.persistence.dynamodb.{
  DynamoDbResource,
  TweetsClient,
  UsersClient
}
import ar.katas.model.user._
import munit.CatsEffectSuite

class RequestTweetsDynamoIT extends CatsEffectSuite {
  test("Request a user list of tweets") {
    val user = User(Username("user who tweet a lot"), Nickname("@uwtal"))

    val client = DynamoDbResource.localDefault
    client.use { jclient =>
      val users = UsersClient.make(jclient)
      val tweets = TweetsClient.make(jclient)

      val registerUser = RegisterUser.make(users)
      val tweetMessage = TweetMessage.make(tweets, users)
      val requestTweets = RequestTweets.make(tweets, users)
      val message = "Hello twitter!"

      for {
        _ <- cleanUsersTable(jclient)
        _ <- registerUser.exec(user)
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
