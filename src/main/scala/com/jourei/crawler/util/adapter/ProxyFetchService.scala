package com.jourei.crawler.util.adapter

import scala.concurrent.Future

trait ProxyFetchService {
  def fetchProxies(selector: String, url: String): Future[Set[(Host, Port)]]

  type Host = String
  type Port = Int
}
