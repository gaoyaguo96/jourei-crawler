package com.jourei.crawler.server.algebra

import cats.{ Applicative, Traverse }
import cats.implicits.toTraverseOps
import cats.syntax.applicative._

trait PurifyProxies[F[_]] {
  final def purifyProxies(rawProxies: Seq[String])(
      implicit t: Traverse[Set],
      ap: Applicative[F]): F[Option[Set[(Host, Port)]]] =
    rawProxies.grouped(2).map(purifySingle).toSet.sequence.pure

  private def purifySingle(rawSingle: Seq[String]): Option[(Host, Port)] =
    rawSingle match {
      case Seq(host, port) if port.toIntOption.isDefined =>
        Some((host, port.toInt))
      case _ => Option.empty
    }

  type Host = String
  type Port = Int
}
