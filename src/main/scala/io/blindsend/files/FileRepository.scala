package io.blindsend.files

import cats.effect.IO

trait FileRepository {

//  def deleteFile(fileId: String): IO[Int]
  def deleteFiles(fileIds: List[String]): IO[Int]
}
