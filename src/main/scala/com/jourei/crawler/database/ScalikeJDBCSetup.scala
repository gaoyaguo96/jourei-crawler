package com.jourei.crawler.database

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import scalikejdbc.config.{
  DBs,
  NoEnvPrefix,
  TypesafeConfig,
  TypesafeConfigReader
}
import scalikejdbc.{
  ConnectionPool,
  DataSourceCloser,
  DataSourceConnectionPool
}

object ScalikeJDBCSetup {
  def init(config: Config): Unit = {
    val dbs = new DBsFromConfig(config)
    dbs.loadGlobalSettings()
    val dataSource = buildDataSource(config)
    ConnectionPool.singleton(
      new DataSourceConnectionPool(
        dataSource = dataSource,
        closer = new HikariCloser(dataSource)))
  }

  private class DBsFromConfig(val config: Config)
      extends DBs
      with TypesafeConfigReader
      with TypesafeConfig
      with NoEnvPrefix

  private def buildDataSource(config: Config): HikariDataSource = {
    val dataSource = new HikariDataSource()
    dataSource.setPoolName("read-side-connection-pool")
    dataSource.setMaximumPoolSize(
      config.getInt("connection-pool.max-pool-size"))
    dataSource.setConnectionTimeout(
      config.getDuration("connection-pool.timeout").toMillis)
    dataSource.setDriverClassName(config.getString("driver"))
    dataSource.setJdbcUrl(config.getString("url"))
    dataSource.setUsername(config.getString("username"))
    dataSource.setPassword(config.getString("password"))

    dataSource
  }

  private class HikariCloser(dataSource: HikariDataSource)
      extends DataSourceCloser {
    override def close(): Unit = dataSource.close()
  }
}
