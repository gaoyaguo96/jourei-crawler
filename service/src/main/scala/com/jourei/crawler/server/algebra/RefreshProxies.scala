package com.jourei.crawler.server.algebra
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.{ Monad, Traverse }

trait RefreshProxies[F[_], ProxiesSummary]
    extends GetAllProxySources[F]
    with FetchProxies[F]
    with RemoveAllProxies[F]
    with AddProxy[F, ProxiesSummary] {
  def refreshProxies()(
      implicit M: Monad[F],
      T: Traverse[Set]): F[ProxiesSummary] =
    for {
      proxySources <- getAllProxySources
      fetchedProxies <- fetchProxies(proxySources)
      _ <- removeAllProxies()
      proxiesSummary <- addProxies(fetchedProxies.toSeq)
    } yield proxiesSummary
}
