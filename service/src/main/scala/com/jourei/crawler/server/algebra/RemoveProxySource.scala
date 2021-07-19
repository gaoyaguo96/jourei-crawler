package com.jourei.crawler.server.algebra

trait RemoveProxySource[F[_], Summary] {
  def removeProxySource(selector: String)(url: String): F[Summary]
}
