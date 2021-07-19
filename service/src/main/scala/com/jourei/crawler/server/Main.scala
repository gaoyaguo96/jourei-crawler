package com.jourei.crawler.server

import akka.actor.typed.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Nothing] =
      ActorSystem[Nothing](CrawlerSystem.systemBehavior, "crawler-system")
  }
}
