package com.jourei.crawler.server.config

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object AppThreadPool {
  lazy val asyncExecutorContext: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
}
