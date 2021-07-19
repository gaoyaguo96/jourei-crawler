package com.jourei.crawler.server.algebra

trait GetAllProxies[F[_]] {
  def getAllProxies: F[Set[(Host, Port)]]

  type Host = String
  type Port = Int
}
