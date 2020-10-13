package io.blindsend.config

sealed trait StorageConf
case class GoogleCloudStorage(accountFilePath: String, bucketName: String) extends StorageConf

sealed trait LinkRepoConf
case class Postgres(host: String, db: String, user: String, pass: String)  extends LinkRepoConf

case class Config(
  storage: StorageConf,
  linkRepo: LinkRepoConf
)

