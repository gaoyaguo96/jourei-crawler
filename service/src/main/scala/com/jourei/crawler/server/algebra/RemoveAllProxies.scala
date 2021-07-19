package com.jourei.crawler.server.algebra

trait RemoveAllProxies[F[_]] {
  def removeAllProxies(): F[Deleted]

  type Deleted = Iterable[(Host, Port)]
  type Host = String
  type Port = Int
}
