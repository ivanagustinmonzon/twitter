package ar.katas.presentation

import cats.effect.IO
import io.circe.Json
import munit.Assertions
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoder

import scala.util.control.NoStackTrace

trait AssertionsHelper extends Assertions {
  private class AssertionError(msg: String = "") extends NoStackTrace
  def failed(msg: String = "") = fail(msg, new AssertionError)

  implicit val b: EntityEncoder[IO, Json] = jsonEncoder[IO]
}
