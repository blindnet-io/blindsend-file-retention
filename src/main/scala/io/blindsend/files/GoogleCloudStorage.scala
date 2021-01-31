package io.blindsend.files

import java.io.FileInputStream

import cats.effect.IO
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.{BlobId, StorageOptions}
import io.blindsend.config.GoogleCloudStorage
import scala.jdk.CollectionConverters._

object GoogleCloudStorage {

  def apply(conf: GoogleCloudStorage): FileRepository = new FileRepository {

    val serviceAccountPath = conf.accountFilePath
    val bucketName = conf.bucketName

    val storage = StorageOptions.newBuilder()
      .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(serviceAccountPath)))
      .build()
      .getService();

    override def deleteFiles(fileIds: List[String]): IO[Int] = IO {
      val blobIds: List[BlobId] = fileIds.map(BlobId.of(bucketName,_))
      if (blobIds.nonEmpty) storage.delete(blobIds:_*).asScala.count(_==true) else 0
    }
  }
}
