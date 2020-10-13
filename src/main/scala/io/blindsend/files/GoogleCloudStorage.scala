package io.blindsend.files

import java.io.FileInputStream
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.StorageOptions
import io.blindsend.config.GoogleCloudStorage

object GoogleCloudStorage {

  def apply(conf: GoogleCloudStorage) = new FileRepository {

    val serviceAccountPath = conf.accountFilePath
      val bucketName = conf.bucketName

    val storage  = StorageOptions.newBuilder()
      .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(serviceAccountPath)))
      .build()
      .getService();

    override def deleteFile(fileId: String): Unit = {
      storage.delete(bucketName, fileId)
    }
  }
}
