package com.jourei.crawler

import akka.actor.typed.ActorSystem
import com.jourei.crawler.CrawlerSystem.rootBehavior
import org.slf4j.LoggerFactory

object Main {
  val logger = LoggerFactory.getLogger("com.jourei.crawler.Main")
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Nothing] =
      ActorSystem[Nothing](rootBehavior, "crawler-system")
  }
}
