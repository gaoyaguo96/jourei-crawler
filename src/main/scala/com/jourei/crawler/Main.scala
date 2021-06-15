package com.jourei.crawler

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, SupervisorStrategy}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.jourei.crawler.CrawlerSystem.rootBehavior
import com.jourei.crawler.config.AppThreadPool
import com.jourei.crawler.dto.{CrawledData, Result}
import com.jourei.crawler.util.ResponseUtils.{completeWithJsonBody, cors}
import com.jourei.crawler.util.interpreter.ExtractionCoordinator
import com.jourei.crawler.util.interpreter.ExtractionCoordinator.GetSingleInBatches

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Nothing] =
      ActorSystem[Nothing](rootBehavior, "crawler-system")
  }
}
