package com.jourei.crawler.service

import scala.concurrent.Future

trait TextFetchService {
  def fetchAll(selectors: Seq[String])(url: String): Future[Seq[Seq[String]]]
  def fetch(selector: String)(url: String): Future[Seq[String]]
}
