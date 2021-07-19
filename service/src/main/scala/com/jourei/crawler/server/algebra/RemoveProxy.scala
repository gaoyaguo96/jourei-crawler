package com.jourei.crawler.server.algebra

trait RemoveProxy[F[_], Summary] {
  def removeProxy(host: String)(port: Int): F[Summary]
}
