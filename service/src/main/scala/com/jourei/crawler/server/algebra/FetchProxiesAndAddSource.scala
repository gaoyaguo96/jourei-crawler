package com.jourei.crawler.server.algebra

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ Monad, Traverse }

trait FetchProxiesAndAddSource[F[_], Summary]
    extends FetchProxies[F]
    with AddProxySource[F, Summary] {
  def fetchProxiesWithSavingProxySource(selector: String)(
      url: String)(implicit m: Monad[F], t: Traverse[Set]): F[Option[Proxies]] =
    for {
      proxies <- fetchProxies(selector)(url)
      _ <- addProxySource(selector)(url)
    } yield proxies
}
