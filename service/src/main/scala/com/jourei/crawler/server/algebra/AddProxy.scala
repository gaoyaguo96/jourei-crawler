package com.jourei.crawler.server.algebra

trait AddProxy[F[_], Summary] {
  def addProxies(proxies: Seq[(Host, Port)]): F[Summary]

  final def addProxy(host: Host)(port: Port): F[Summary] =
    addProxies(Seq((host, port)))

  type Host = String
  type Port = Int
}
