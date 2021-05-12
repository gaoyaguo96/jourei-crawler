package com.jourei.crawler.util

import com.jourei.crawler.exception.AppException

trait HtmlUtils[F[_]] {
  def extract(selectors: String*)(html: String): F[Either[AppException, String]]
}
