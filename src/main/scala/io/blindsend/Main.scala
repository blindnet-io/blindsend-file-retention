package io.blindsend

import pureconfig.ConfigSource
import cats.implicits._
import io.blindsend.config._
import io.blindsend.files.GoogleCloudStorage
import io.blindsend.repo.PostgresLinkRepository
import io.blindsend.scheduler.FileRemovalScheduler
import pureconfig.generic.FieldCoproductHint
import pureconfig.generic.auto._

object Main extends App {

  implicit val storageConfHint = new FieldCoproductHint[StorageConf]("type")
  implicit val linkRepoConfHint = new FieldCoproductHint[LinkRepoConf]("type")

  val config = ConfigSource.file("app.conf").load[Config].leftMap(e => new Throwable(s"Error reading config: ${e.prettyPrint()}"))

  config match {
    case Left(l) => print(l.getMessage)
    case Right(conf) => {
      val linkRepo = conf.linkRepo match {
        case conf:Postgres => PostgresLinkRepository(conf)
      }
      val fileRepo = conf.storage match {
        case conf: GoogleCloudStorage => GoogleCloudStorage(conf)
      }
      FileRemovalScheduler.runScheduler(linkRepo, fileRepo)
    }
  }
}
