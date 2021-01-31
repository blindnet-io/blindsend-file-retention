package io.blindsend

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneId}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import pureconfig.ConfigSource
import cats.implicits._
import io.blindsend.config._
import io.blindsend.files.{FileRepository, GoogleCloudStorage}
import io.blindsend.repo.{LinkRepository, PostgresLinkRepository}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.FieldCoproductHint
import pureconfig.generic.auto._

import scala.concurrent.duration._

object Main extends IOApp {

  implicit val storageConfHint = new FieldCoproductHint[StorageConf]("type")
  implicit val linkRepoConfHint = new FieldCoproductHint[LinkRepoConf]("type")

  def performDeletionTask(linkRepo: Resource[IO, LinkRepository], fileRepo: Resource[IO, FileRepository]): IO[Unit] = for {
    logger <- Slf4jLogger.create[IO]
    currentTime <- IO(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))))
    fileIds <- linkRepo.use(x => x.getExpiredFileIds(currentTime))
    _ <- logger.info(s"Retrieved ids for ${fileIds.size} files")
    deletedFilesCount <- fileRepo.use(_.deleteFiles(fileIds))
    _ <- logger.info(s"$deletedFilesCount files deleted from the file repo")
    deletedLinksCount <- if (fileIds.nonEmpty) linkRepo.use(_.deleteExpiredLinks(currentTime)) else IO.unit
    _ <- logger.info(s"$deletedLinksCount links deleted from the link repo")
  } yield ()

  def repeatTask(task : IO[Unit]) : IO[Nothing] = task >> IO.sleep(1.hour) >> repeatTask(task)

  def run(args: List[String]): IO[ExitCode] = for {
    config <- IO(ConfigSource.file("app.conf").load[Config].leftMap(e => new Throwable(s"Error reading config: ${e.prettyPrint()}")))
    (linkRepo, fileRepo) = config match {
      case Right(conf) =>
        val linkRepo = conf.linkRepo match {
          case conf: Postgres => PostgresLinkRepository(conf)
        }
        val fileRepo = conf.storage match {
          case conf: GoogleCloudStorage => GoogleCloudStorage(conf)
        }
        (Resource.pure[IO,LinkRepository](linkRepo), Resource.pure[IO,FileRepository](fileRepo))
    }
    _ <- repeatTask(performDeletionTask(linkRepo, fileRepo))
  } yield ExitCode.Success

}
