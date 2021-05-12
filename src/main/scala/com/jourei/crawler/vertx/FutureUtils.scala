package com.jourei.crawler.vertx

import com.jourei.crawler.functional.Constants.AppEither

import java.util.concurrent.CompletableFuture

object FutureUtils {
  def fromEitherAsync[A](
      either: => AppEither[A]
  ): CompletableFuture[A] = {
    CompletableFuture.supplyAsync { () =>
      either match {
        case Left(e) => throw e
        case Right(a) => a
      }
    }
  }
}
