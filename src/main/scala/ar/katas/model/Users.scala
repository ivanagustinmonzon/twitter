package ar.katas.model

import ar.katas.model.user._
import cats.effect.IO

trait Users {
  def get(nickname: Nickname): IO[User]
  def persist(user: User): IO[Unit]
  def exists(id: Nickname): IO[Boolean]
  def update(user: User): IO[Unit]
}
