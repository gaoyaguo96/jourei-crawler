package com.jourei.crawler.database

import akka.projection.eventsourced.EventEnvelope
import akka.projection.jdbc.scaladsl.JdbcHandler
import com.jourei.crawler.protocol.ProxyPool
import com.jourei.crawler.protocol.ProxyPool.{ Added, Removed }
import com.jourei.crawler.repostory.ScalikeJDBCProxyProjectionRepositoryInterpreter
import com.jourei.crawler.repostory.entity.CrawlerProxy

final class ProxyPoolHandler(
    repo: ScalikeJDBCProxyProjectionRepositoryInterpreter.type)
    extends JdbcHandler[EventEnvelope[ProxyPool.Event], ScalikeJdbcSession] {
  def process(
      session: ScalikeJdbcSession,
      envelope: EventEnvelope[ProxyPool.Event]): Unit =
    envelope.event match {
      case Added(host, port) =>
        repo.save(CrawlerProxy(host, port))(session)
      case Removed(host, port) =>
        repo.remove(host -> port)(session)
    }
}
