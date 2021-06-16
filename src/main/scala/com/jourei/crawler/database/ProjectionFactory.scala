package com.jourei.crawler.database

import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.jdbc.scaladsl.JdbcProjection
import akka.projection.{ Projection, ProjectionId }
import com.jourei.crawler.protocol.ProxyPool
import com.jourei.crawler.repostory.ScalikeJDBCProxyProjectionRepositoryInterpreter
import scalikejdbc.DB

object ProjectionFactory {
  def get(tag: String)(
      implicit
      system: ActorSystem[_]): Projection[EventEnvelope[ProxyPool.Event]] =
    JdbcProjection.exactlyOnce(
      ProjectionId("ProxyPoolProjection", tag),
      buildSourceProvider(tag),
      () => ScalikeJdbcSession(DB.connect()),
      () =>
        new ProxyPoolHandler(ScalikeJDBCProxyProjectionRepositoryInterpreter))(
      system)

  private def buildSourceProvider(tag: String)(
      implicit system: ActorSystem[_]) =
    EventSourcedProvider
      .eventsByTag[ProxyPool.Event](system, JdbcReadJournal.Identifier, tag)
}
