package io.blindsend.scheduler

import java.time.{LocalDateTime, ZoneId}
import akka.actor.ActorSystem
import io.blindsend.files.FileRepository
import io.blindsend.repo.LinkRepository
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object FileRemovalScheduler {

  val system = ActorSystem("FileRetentionSystem")

  def runScheduler(linkRepo: LinkRepository, fileRepo: FileRepository): Unit = {
    system.scheduler.scheduleAtFixedRate(0 seconds, 1 hour)(
      () => {
        val currentDateTime = LocalDateTime.now(ZoneId.of("UTC"))
        deleteFiles(linkRepo, fileRepo, currentDateTime)
      })
  }

  def deleteFiles(linkRepo: LinkRepository, fileRepo: FileRepository, currentDateTime: LocalDateTime) = {
    val fileIds = linkRepo.getExpiredFileIds(currentDateTime)
    fileIds.foreach(id => {
      fileRepo.deleteFile(id)
    })
    linkRepo.deleteExpiredLinks(currentDateTime)
  }
}
