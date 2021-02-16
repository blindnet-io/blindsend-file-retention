package io.blindsend

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneId}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import pureconfig.ConfigSource
import cats.implicits._
import io.blindsend.config._
import io.blindsend.files.{FileRepository, GoogleCloudStorage}
import io.blindsend.links.{LinkRepository, PostgresLinkRepository}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.FieldCoproductHint
import pureconfig.generic.auto._

import scala.concurrent.duration._

object Main extends IOApp {

  implicit val storageConfHint = new FieldCoproductHint[StorageConf]("type")
  implicit val linkRepoConfHint = new FieldCoproductHint[LinkRepoConf]("type")

  def performDeletionTask(linkRepo: LinkRepository, fileRepo: FileRepository, logger: SelfAwareStructuredLogger[IO]): IO[Unit] = for {
    currentTime <- IO(Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC"))))
    _ <- logger.info(s"Time is ${currentTime} files")
    fileIds <- linkRepo.getExpiredFileIds(currentTime)
    _ <- logger.info(s"Retrieved ids for ${fileIds.size} files")
    deletedFilesCount <- fileRepo.deleteFiles(fileIds)
    _ <- logger.info(s"$deletedFilesCount files deleted from the file repo")
    deletedLinksCount <- if (fileIds.nonEmpty) linkRepo.deleteExpiredLinks(currentTime) else IO.unit
    _ <- logger.info(s"$deletedLinksCount links deleted from the link repo")
  } yield ()

  def repeatTask(task : IO[Unit]) : IO[Nothing] = task >> IO.sleep(1.hour) >> repeatTask(task)

  def run(args: List[String]): IO[ExitCode] = for {
    config <- IO(ConfigSource.file("app.conf").load[Config].leftMap(e => new Throwable(s"Error reading config: ${e.prettyPrint()}")))
    logger <- Slf4jLogger.create[IO]
    repos = config match {
      case Right(conf) =>
        val linkRepo = PostgresLinkRepository(conf.linkRepo.asInstanceOf[Postgres])
        val fileRepo = conf.storage match {
          case conf: GoogleCloudStorage => GoogleCloudStorage(conf)
        }
        Resource.pure[IO,(LinkRepository, FileRepository)](linkRepo, fileRepo)
    }
    _ <- repos.use(res => repeatTask(performDeletionTask(res._1, res._2, logger)))
  } yield ExitCode.Success

}
