package com.jourei.crawler.server.natural

import cats.arrow.FunctionK

object Naturals {
  implicit object SetToSeq extends FunctionK[Set, Seq] {
    def apply[A](fa: Set[A]): Seq[A] = fa.toSeq
  }
}
object naturalOps {
  implicit final class SetOps(set: Set[_]) {
    def toSeq: Seq[_] = set.toSeq
  }
}
