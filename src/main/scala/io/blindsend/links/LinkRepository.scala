package io.blindsend.links

import java.sql.Timestamp
import cats.effect.IO

trait LinkRepository {

  def getExpiredFileIds(current: Timestamp): IO[List[String]]

  def deleteExpiredLinks(before: Timestamp): IO[Int]
}
