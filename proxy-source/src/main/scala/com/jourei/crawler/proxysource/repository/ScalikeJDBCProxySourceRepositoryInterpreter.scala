package com.jourei.crawler.proxysource.repository

import com.jourei.crawler.db.ScalikeJdbcSession
import com.jourei.crawler.proxysource.entity.ProxySourceEntity
import com.jourei.crawler.repository.JDBCProjectionRepository
import scalikejdbc.{DBSession, scalikejdbcSQLInterpolationImplicitDef}

object ScalikeJDBCProxySourceRepositoryInterpreter
    extends JDBCProjectionRepository[
      (String, String),
      ProxySourceEntity,
      ScalikeJdbcSession] {
  def save(entity: ProxySourceEntity)(session: ScalikeJdbcSession): Unit =
    session.getDB.withinTx { implicit dbSession =>
      if (select(entity.url, entity.selector).isEmpty) insert(entity)
    }

  def get(id: ID)(session: ScalikeJdbcSession): Option[ProxySourceEntity] = {
    val db = session.getDB
    if (db.isTxAlreadyStarted)
      db.withinTx(select(id)(_))
    else db.readOnly(select(id)(_))
  }

  def remove(id: ID)(session: ScalikeJdbcSession): Option[ProxySourceEntity] =
    session.getDB.withinTx { implicit dbSession =>
      for {
        proxySource <- select(id)
        _ = delete(id)
      } yield proxySource
    }

  private def select(id: ID)(
      implicit dbSession: DBSession): Option[ProxySourceEntity] = {
    val (url, selector) = id
    sql"""
         SELECT url, selector FROM t_proxy_source 
         WHERE url = $url AND selector = $selector
       """
      .map(rs => ProxySourceEntity(rs.string("url"), rs.string("selector")))
      .single()
      .apply()
  }
  private def delete(id: ID)(implicit dbSession: DBSession): Unit = {
    val (url, selector) = id
    sql"""
         DELETE FROM t_proxy_source WHERE url = $url AND selector = $selector
       """.update().apply()
  }
  private def insert(proxySourceEntity: ProxySourceEntity)(
      implicit dbSession: DBSession): Unit = {
    val url = proxySourceEntity.url
    val selector = proxySourceEntity.selector
    sql"""
         INSERT INTO t_proxy_source(url, selector) VALUES ($url, $selector)
       """.update().apply()
  }

  type Selector = String
  type URL = String
  type ID = (URL, Selector)
}
