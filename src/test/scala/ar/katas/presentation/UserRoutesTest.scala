package ar.katas.presentation

import ar.katas.actions.{RegisterUser, UpdateUser}
import ar.katas.infrastructure.http.routes.UserRoutes
import ar.katas.infrastructure.persistence.inmemory.UsersInMemory
import ar.katas.model.user._
import cats.effect.IO
import io.circe.literal._
import munit.CatsEffectSuite
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax

class UserRoutesTest extends CatsEffectSuite with HttpHelper {

  test("POST register user returns 201 if succeed") {
    val registerUserRequest: Request[IO] = POST(
      json"""
              {
               "realname": "John register user",
               "nickname": "@jru"
              }
              """,
      uri"/users"
    )

    val routes = UserRoutes(new TestRegisterUser, new TestUpdateUser).routes

    routes.run(registerUserRequest).value.map {
      case Some(resp) => assert(resp.status == Status.Created)
      case None       => failed()
    }
  }

  test("POST register user returns 409 if already exists") {
    val registerUserRequest: Request[IO] = POST(
      json"""
              {
               "realname": "John register user",
               "nickname": "@jru"
              }
              """,
      uri"/users"
    )

    UsersInMemory.make
      .flatMap { users =>
        val registerInMemory = RegisterUser.make(users)
        val routes = UserRoutes(registerInMemory, new TestUpdateUser).routes

        for {
          _ <- routes.run(registerUserRequest).value
          result <- routes.run(registerUserRequest).value
        } yield result
      }
      .map {
        case Some(resp) => assert(resp.status == Status.Conflict)
        case None       => failed()
      }

  }

}

protected class TestRegisterUser extends RegisterUser {
  override def exec(user: User): IO[Unit] = IO.unit
}
protected class TestUpdateUser extends UpdateUser {
  override def exec(user: User): IO[Unit] = IO.unit
}
