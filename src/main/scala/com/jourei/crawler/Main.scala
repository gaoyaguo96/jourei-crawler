package com.jourei.crawler

import com.jourei.crawler.verticle.MainVerticle
import io.vertx.core.Vertx

object Main {
  def main(args: Array[String]): Unit = {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(new MainVerticle())
  }
}
