package com.jourei.crawler

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, SupervisorStrategy}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.jourei.crawler.config.AppThreadPool
import com.jourei.crawler.dto.{CrawledData, Result}
import com.jourei.crawler.util.ResponseUtils.{completeWithJsonBody, cors}
import com.jourei.crawler.util.interpreter.ExtractionCoordinator
import com.jourei.crawler.util.interpreter.ExtractionCoordinator.{
  GetInBatches,
  GetRespectivelyInBatchesUsingRawSelectors
}
import io.circe.generic.auto.exportEncoder

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val extractionCoordinator = context.spawn(
        Behaviors
          .supervise(ExtractionCoordinator())
          .onFailure[Throwable](SupervisorStrategy.restart),
        "extraction-coordinator"
      )

      implicit val system: ActorSystem[Nothing] = context.system
      implicit val timeout: Timeout = Timeout(3.seconds)

      def getText(selector: String)(url: String): Future[String] = {
        val future: Future[Seq[String]] = extractionCoordinator
          .askWithStatus(GetInBatches(Seq(selector), url, _))
        future
          .map(_.head)(AppThreadPool.asyncExecutorContext)
      }
      def getFirstTextRespectively(selectors: Seq[String])(
          url: String
      ): Future[Seq[String]] =
        extractionCoordinator.askWithStatus(GetInBatches(selectors, url, _))
      def getAllText(selectors: String)(
          url: String
      ): Future[Seq[String]] =
        extractionCoordinator.askWithStatus(
          GetRespectivelyInBatchesUsingRawSelectors(selectors, url, _)
        )

      val routes =
        cors {
          concat(
            path("get") {
              get {
                parameters("url", "selector") { (url, selector) =>
                  onSuccess(getText(selector)(url)) { html =>
                    complete(html)
                  }
                }
              }
            },
            path("get-all-in-batches") {
              get {
                parameters("url", "selectors") { (url, selectors) =>
                  onSuccess(
                    getAllText(selectors)(url).map(texts =>
                      Result(succeed = true, CrawledData(texts))
                    )(AppThreadPool.asyncExecutorContext)
                  )(completeWithJsonBody)
                }
              }
            }
          )
        }
      val bindingFuture: Future[Http.ServerBinding] =
        Http().newServerAt("localhost", 30000).bind(routes)

      implicit val executorContext: ExecutionContext = system.executionContext
      bindingFuture.onComplete {
        case Failure(exception) =>
          system.log.error(
            "Failed to bind HTTP endpoint, terminating system",
            exception
          )
          system.terminate()
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info(
            "Listening on http://{}:{}",
            address.getHostString,
            address.getPort
          )
      }

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "my-system")
  }
}
