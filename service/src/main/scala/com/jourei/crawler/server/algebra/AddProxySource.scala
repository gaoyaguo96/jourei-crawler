package com.jourei.crawler.server.algebra

trait AddProxySource[F[_], Summary] {
  def addProxySource(selector: String)(url: String): F[Summary]
}
