package io.blindsend.repo

import java.time.LocalDateTime

trait LinkRepository {

  def getExpiredFileIds(current: LocalDateTime): List[String]

  def deleteExpiredLinks(before: LocalDateTime)
}
