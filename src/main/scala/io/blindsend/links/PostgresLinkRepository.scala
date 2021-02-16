package io.blindsend.links

import java.sql.Timestamp

import cats.effect.{ContextShift, IO}
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._ // explicit import needed by doobie (see v0.8.8 release notes)
import io.blindsend.config.Postgres
import scala.concurrent.ExecutionContext
import cats.syntax.all._

object PostgresLinkRepository {

  def apply(conf: Postgres)(implicit cs: ContextShift[IO]): LinkRepository = new LinkRepository {

    val xa = Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = s"jdbc:postgresql://${conf.host}/${conf.db}",
      user = conf.user,
      pass = conf.pass
    )

    override def getExpiredFileIds(currentDateTime: Timestamp): IO[List[String]] =
      (getFileIds(currentDateTime, "links_request"), getFileIds(currentDateTime, "links_send")).parMapN(_++_)

    private def getFileIds(currentDateTime: Timestamp, dbt: String): IO[List[String]] =
      (fr"select file_id from" ++ Fragment.const(dbt) ++
        fr"WHERE created + life_expectancy * interval '1 hour' <= $currentDateTime"
        ).query[String].to[List].transact(xa)

    override def deleteExpiredLinks(currentDateTime: Timestamp): IO[Int] = for {
      reqDel <- deleteLinks(currentDateTime, "links_request")
      sendDel <- deleteLinks(currentDateTime, "links_send")
    } yield reqDel + sendDel

    private def deleteLinks(currentDateTime: Timestamp, dbt: String): IO[Int] =
      (fr"delete from" ++ Fragment.const(dbt) ++
        fr"WHERE created + life_expectancy * interval '1 hour' <= $currentDateTime"
        ).update.run.transact(xa)
  }
}
