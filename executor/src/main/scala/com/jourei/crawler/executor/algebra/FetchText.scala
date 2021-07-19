package com.jourei.crawler.executor.algebra

trait FetchText[F[_]] {
  def fetchText(selector: String)(url: String): F[Seq[String]]
}
