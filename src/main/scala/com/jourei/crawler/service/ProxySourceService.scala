package com.jourei.crawler.service

trait ProxySourceService[F[_], Summary] {
  def add(selector: String)(url: String): F[Summary]
  def remove(selector: Selector)(URL: URL): F[Summary]

  type Selector = String
  type URL = String
}
