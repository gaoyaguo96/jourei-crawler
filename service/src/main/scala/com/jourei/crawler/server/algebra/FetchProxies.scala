package com.jourei.crawler.server.algebra

import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import cats.{ Monad, Traverse }
import com.jourei.crawler.executor.algebra.FetchText

trait FetchProxies[F[_]] extends FetchText[F] with PurifyProxies[F] {
  final def fetchProxies(selector: Selector)(
      url: URL)(implicit m: Monad[F], t: Traverse[Set]): F[Option[Proxies]] =
    for {
      fetched <- fetchText(selector)(url)
      proxies <- purifyProxies(fetched)
    } yield proxies

  final def fetchProxies(proxySources: Set[(Selector, URL)])(
      implicit m: Monad[F],
      t: Traverse[Set]): F[Proxies] =
    proxySources
      .traverse { case (selector, url) => fetchProxies(selector)(url) }
      .fmap(_.flatten.flatten)

  type Proxies = Set[(Host, Port)]
  type Selector = String
  type URL = String
}
