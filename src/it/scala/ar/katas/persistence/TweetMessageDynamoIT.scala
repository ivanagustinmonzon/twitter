package ar.katas.persistence

import ar.katas.actions.{RegisterUser, TweetMessage}
import ar.katas.infrastructure.persistence.dynamodb.{
  DynamoDbResource,
  TweetsClient,
  UsersClient
}
import ar.katas.model.user._
import munit.CatsEffectSuite

class TweetMessageDynamoIT extends CatsEffectSuite {

  test("A user can tweet a message") {
    val john = User(Username("A user who tweets it"), Nickname("@auwti"))

    val client = DynamoDbResource.localDefault
    client.use { jclient =>
      val users = UsersClient.make(jclient)
      val tweets = TweetsClient.make(jclient)
      val register = RegisterUser.make(users)
      val tweetMessage = TweetMessage.make(tweets, users)
      val message = "Hello twitter, IT"

      for {
        _ <- cleanUsersTable(jclient)
        _ <- register.exec(john)
        _ <- tweetMessage.exec(john.nickname, message)
        usrTweets <- tweets.getTweets(john.nickname)
      } yield assert(usrTweets.head.message == message)
    }
  }
}
