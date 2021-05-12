package com.jourei.crawler.functional

import com.jourei.crawler.exception.AppException

object Constants {
  type AppEither[A] = Either[AppException, A]
}
