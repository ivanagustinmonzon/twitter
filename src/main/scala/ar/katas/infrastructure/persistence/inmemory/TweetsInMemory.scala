package ar.katas.infrastructure.persistence.inmemory

import ar.katas.model.Tweets
import ar.katas.model.tweet.{Tweet, TweetId}
import ar.katas.model.user._
import cats.effect.IO

object TweetsInMemory {
  def make: IO[Tweets] = IO
    .ref[Map[Nickname, List[(TweetId, String)]]](Map.empty)
    .map { database =>
      new Tweets {
        override def persistTweet(
            id: Nickname,
            message: String
        ): IO[TweetId] =
          TweetId.make.flatMap { tweetId =>
            database
              .update { it =>
                val previous = it.getOrElse(id, List.empty)
                val updated = (tweetId -> message) :: previous
                it + (id -> updated)
              }
              .as(tweetId)
          }

        override def getTweets(user: Nickname): IO[List[Tweet]] =
          database.get.map(m =>
            m.getOrElse(user, List.empty).map { case (tweetId, message) =>
              Tweet(tweetId, user, message)
            }
          )
      }

    }
}
