package com.jourei.crawler.database

import akka.japi.function
import akka.projection.jdbc.JdbcSession
import scalikejdbc.DBConnection

import java.sql.Connection

object ScalikeJdbcSession {
  def apply(db: DBConnection): ScalikeJdbcSession =
    new ScalikeJdbcSession(db.autoClose(false))
}

final class ScalikeJdbcSession private (db: DBConnection) extends JdbcSession {
  def getDB: DBConnection = db

  def withConnection[Result](
      func: function.Function[Connection, Result]): Result = {
    db.begin()
    db.withinTxWithConnection(func(_))
  }

  def commit(): Unit = db.commit()

  def rollback(): Unit = db.rollback()

  def close(): Unit = db.close()
}
