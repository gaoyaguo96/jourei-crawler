package com.jourei.crawler.util

import com.jourei.crawler.exception.TaskException

trait HttpUtils[F[_]] {
  def get(url: String): F[Either[TaskException, String]]
}
