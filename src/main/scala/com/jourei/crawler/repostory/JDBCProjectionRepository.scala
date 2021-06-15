package com.jourei.crawler.repostory

import akka.projection.jdbc.JdbcSession

trait JDBCProjectionRepository[ID, Entity, Session <: JdbcSession] {
  def save(entity: Entity)(session: Session): Unit
  def get(id: ID)(session: Session): Option[Entity]
  def remove(id: ID)(session: Session): Option[Entity]
}
