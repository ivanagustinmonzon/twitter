package ar.katas.domain

import ar.katas.domain.user._
import cats.effect.IO

trait Users {
  def persist(user: User): IO[Unit]
  def exists(id: Nickname): IO[Boolean]
}
