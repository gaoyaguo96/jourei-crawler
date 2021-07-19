package com.jourei.crawler.server.program

import cats.effect.IO
import cats.{ Monad, Traverse }
import com.jourei.crawler.server.algebra.{
  AddProxySource,
  RefreshProxies,
  RemoveProxySource
}

import scala.concurrent.Future
import scala.language.implicitConversions

trait Programs {
  def addProxySource(selector: Selector)(url: URL)(
      addProxySource: AddProxySource[Future, Seq[(Selector, URL)]])
      : IO[Seq[(Selector, URL)]] =
    addProxySource.addProxySource(selector)(url)

  def removeProxySource(selector: Selector)(url: URL)(
      proxySourceAlgebra: RemoveProxySource[Future, Seq[(Selector, URL)]])
      : IO[Seq[(Selector, URL)]] =
    proxySourceAlgebra.removeProxySource(selector)(url)

  def refreshProxies()(refreshProxies: RefreshProxies[Future, ProxiesSummary])(
      implicit M: Monad[Future],
      T: Traverse[Set]): IO[ProxiesSummary] =
    refreshProxies.refreshProxies()

  implicit private def toIO[A](future: Future[A]): IO[A] =
    IO.fromFuture(IO(future))

  type ProxiesSummary = Set[(Host, Port)]
  type Host = String
  type Port = String
  type Selector = String
  type URL = String
}
