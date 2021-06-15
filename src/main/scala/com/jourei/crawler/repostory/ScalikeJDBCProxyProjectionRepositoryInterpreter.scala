package com.jourei.crawler.repostory

import com.jourei.crawler.database.ScalikeJdbcSession
import com.jourei.crawler.repostory.entity.CrawlerProxy
import scalikejdbc.{
  scalikejdbcSQLInterpolationImplicitDef,
  DBConnection,
  DBSession
}

object ScalikeJDBCProxyProjectionRepositoryInterpreter
    extends JDBCProjectionRepository[
      (String, Int),
      CrawlerProxy,
      ScalikeJdbcSession] {
  def save(entity: CrawlerProxy)(session: ScalikeJdbcSession): Unit =
    session.getDB.withinTx { implicit dbSession =>
      if (select(entity.host -> entity.port).isEmpty) insert(entity)
    }

  def get(id: ID)(session: ScalikeJdbcSession): Option[CrawlerProxy] =
    getOne(id)(session.getDB)

  def remove(id: ID)(session: ScalikeJdbcSession): Option[CrawlerProxy] =
    session.getDB.withinTx { implicit dbSession =>
      for {
        proxy <- select(id)
        _ = delete(id)
      } yield proxy
    }

  private type ID = (String, Int)
  private def getOne(id: ID)(db: DBConnection): Option[CrawlerProxy] =
    if (db.isTxAlreadyStarted) db.withinTx(implicit dbSession => select(id))
    else db.readOnly(implicit dbSession => select(id))

  private def select(id: ID)(
      implicit dbSession: DBSession): Option[CrawlerProxy] = {
    val (host, port) = id
    sql"""
         SELECT host, port FROM t_proxy
         WHERE host = $host AND port = $port
         LIMIT 1"""
      .map(rs => CrawlerProxy(rs.string("host"), rs.int("port")))
      .single()
      .apply()
  }
  private def delete(id: ID)(implicit dbSession: DBSession): Unit = {
    val (host, port) = id
    sql"""DELETE FROM t_proxy WHERE host = $host AND port = $port"""
      .update()
      .apply()
  }

  private def insert(proxy: CrawlerProxy)(
      implicit dbSession: DBSession): Unit = {
    val CrawlerProxy(host, port) = proxy
    sql"""INSERT INTO t_proxy(host, port) VALUES($host,$port)"""
      .update()
      .apply()
  }
}
