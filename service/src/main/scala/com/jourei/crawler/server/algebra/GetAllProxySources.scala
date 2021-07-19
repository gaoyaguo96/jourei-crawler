package com.jourei.crawler.server.algebra

trait GetAllProxySources[F[_]] {
  def getAllProxySources: F[ProxySources]

  type ProxySources = Set[(Selector, URL)]
  type Selector = String
  type URL = String
}
