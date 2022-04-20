package ar.katas.presentation

import ar.katas.actions.{FollowUser, WhoIsFollowing}
import ar.katas.infrastructure.http.routes.FollowRoutes
import ar.katas.model.user
import ar.katas.model.user.{Nickname, User, UserNotFound, Username}
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.circe.literal._
import munit.CatsEffectSuite
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax

class FollowRoutesTest extends CatsEffectSuite with AssertionsHelper {

  test("POST follow user returns 204 if succeed") {
    val followUserRequest: Request[IO] = POST(
      json"""
              {
               "followeeId": "@jh"
              }
              """,
      uri"/users/@jru/follows"
    )

    val routes = FollowRoutes(new OkFollowUser, new OkWhoIsfollowing).routes

    routes.run(followUserRequest).value.map {
      case Some(resp) => assert(resp.status == Status.NoContent)
      case None       => failed()
    }
  }

  test("POST follow user returns 404 if user not found") {
    val followUserRequest: Request[IO] = POST(
      json"""
              {
               "followeeId": "@jh"
              }
              """,
      uri"/users/@jru/follows"
    )

    val routes =
      FollowRoutes(new NotFoundFollowUser, new OkWhoIsfollowing).routes

    routes.run(followUserRequest).value.map {
      case Some(resp) => assert(resp.status == Status.NotFound)
      case None       => failed()
    }
  }

  test("GET who is a user following returns 200 if succeed") {
    val getUserFolloweesRequest: Request[IO] = GET(uri"/users/@jru/follows")

    val routes =
      FollowRoutes(new OkFollowUser, new OkWhoIsfollowing).routes

    routes.run(getUserFolloweesRequest).value.map {
      case Some(resp) => assert(resp.status == Status.Ok)
      case None       => failed()
    }
  }

  test("GET who is a user following returns 404 if user not found") {
    val getUserFolloweesRequest: Request[IO] = GET(uri"/users/@jru/follows")

    val routes =
      FollowRoutes(new OkFollowUser, new NotFoundWhoIsFollowing).routes

    routes.run(getUserFolloweesRequest).value.map {
      case Some(resp) => assert(resp.status == Status.NotFound)
      case None       => failed()
    }
  }
}

private class NotFoundWhoIsFollowing() extends WhoIsFollowing {
  override def exec(followerId: user.Nickname): IO[List[user.User]] =
    IO.raiseError(UserNotFound(followerId))
}

private class OkFollowUser() extends FollowUser {
  override def exec(followerId: Nickname, followeeId: Nickname): IO[Unit] =
    IO.unit
}

class OkWhoIsfollowing() extends WhoIsFollowing {
  override def exec(followerId: Nickname): IO[List[user.User]] = List(
    User(Username("Test"), followerId)
  ).pure[IO]
}

class NotFoundFollowUser() extends FollowUser {
  override def exec(followerId: Nickname, followeeId: Nickname): IO[Unit] =
    IO.raiseError(UserNotFound(followerId))
}
