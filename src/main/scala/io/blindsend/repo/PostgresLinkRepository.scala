package io.blindsend.repo

import java.time.LocalDateTime
import java.sql._
import io.blindsend.config.Postgres


object PostgresLinkRepository {

  def apply(conf: Postgres) = new LinkRepository {

    val conn:Connection = {
      Class.forName("org.postgresql.Driver")
      val url = s"jdbc:postgresql://${conf.host}/${conf.db}"
      val user = conf.user
      val password = conf.pass
      DriverManager.getConnection(url, user, password)
    }

    override def getExpiredFileIds(currentDateTime: LocalDateTime): List[String] = {
      getFileIds(currentDateTime, "links_request") ++ getFileIds(currentDateTime, "links_send")
    }

    private def getFileIds(currentDateTime: LocalDateTime, dbt: String): List[String] = {
      val statement = conn.createStatement()
      val resultSet = statement.executeQuery(s"SELECT file_id FROM ${dbt} WHERE created + life_expectancy * interval '1 hour' <= '${currentDateTime}';")
      new Iterator[String] {
        def hasNext = resultSet.next()
        def next() = resultSet.getString("file_id")
      }.toList
    }

    override def deleteExpiredLinks(currentDateTime: LocalDateTime): Unit = {
      deleteLinks(currentDateTime,"links_request")
      deleteLinks(currentDateTime,"links_send")
    }

    private def deleteLinks(currentDateTime: LocalDateTime, dbt: String) = {
      val statement = conn.createStatement()
      statement.executeUpdate(s"DELETE FROM ${dbt} WHERE created + life_expectancy * interval '1 hour' <= '${currentDateTime}';;")
    }

  }
}
