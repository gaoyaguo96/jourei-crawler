package com.jourei.crawler

import akka.actor.typed.scaladsl.AskPattern.{
  schedulerFromActorSystem,
  Askable
}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, SupervisorStrategy }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{
  concat,
  get,
  onSuccess,
  parameters,
  path
}
import akka.projection.ProjectionBehavior
import akka.util.Timeout
import com.jourei.crawler.config.AppThreadPool
import com.jourei.crawler.database.{ ProjectionFactory, ScalikeJDBCSetup }
import com.jourei.crawler.dto.{ CrawledData, Result }
import com.jourei.crawler.util.ResponseUtils.{ completeWithJsonBody, cors }
import com.jourei.crawler.util.adapter.ProxyFetchServiceActorAdapter
import com.jourei.crawler.util.interpreter.ExtractionCoordinator.GetAll
import com.jourei.crawler.util.interpreter.{
  ExtractionCoordinator,
  ProxyFetchHelper,
  ProxyPool
}
import io.circe.generic.auto.exportEncoder

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object CrawlerSystem {
  def rootBehavior: Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val extractionCoordinator = context.spawn(
        superviseWithRestart(ExtractionCoordinator()),
        "extraction-coordinator")
      val proxyFetchHelper = context.spawn(
        superviseWithRestart(ProxyFetchHelper(extractionCoordinator)),
        "proxy-fetch-helper")

      implicit val system: ActorSystem[Nothing] = context.system
      implicit val timeout: Timeout = Timeout(3.seconds)

      ScalikeJDBCSetup.init(system.settings.config)

      val projectionTag = "proxy-pool-0"
      val proxyPoolActor =
        context.spawn(
          superviseWithRestart(ProxyPool(projectionTag)),
          "proxy-pool")
      val proxyPoolProjection = ProjectionFactory.get(projectionTag)
      val proxyPoolProjectionActor =
        context.spawn(
          superviseWithRestart(ProjectionBehavior(proxyPoolProjection)),
          "proxy-pool-projection")

      val proxyFetchService =
        new ProxyFetchServiceActorAdapter(proxyFetchHelper)

      def getText(selector: String)(url: String): Future[Seq[String]] =
        extractionCoordinator
          .askWithStatus(GetAll(Seq(selector), url, _))
          .map(_.head)(AppThreadPool.asyncExecutorContext)

      def getAllText(selectors: Seq[String])(
          url: String): Future[Seq[Seq[String]]] =
        extractionCoordinator.askWithStatus(GetAll(selectors, url, _))

      val routes =
        cors {
          concat(
            path("get") {
              get {
                parameters("url", "selector") { (url, selector) =>
                  onSuccess(getText(selector)(url)) { html =>
                    completeWithJsonBody(html)
                  }
                }
              }
            },
            path("get-all-in-batches") {
              get {
                parameters("url", "selectors") { (url, selectors) =>
                  onSuccess((getAllText(selectors)(url) map (texts =>
                    Result(succeed = true, CrawledData(texts))))(
                    AppThreadPool.asyncExecutorContext))(
                    completeWithJsonBody(_))
                }
              }
            })
        }
      val bindingFuture: Future[Http.ServerBinding] =
        Http() newServerAt ("localhost", 30000) bind routes

      implicit val executorContext: ExecutionContext = system.executionContext
      bindingFuture.onComplete {
        case Failure(exception) =>
          system.log.error(
            "Failed to bind HTTP endpoint, terminating system",
            exception)
          system.terminate()
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info(
            "Listening on http://{}:{}",
            address.getHostString,
            address.getPort)
      }

      Behaviors.empty
    }

  private def superviseWithRestart[A](behavior: Behavior[A]): Behavior[A] =
    Behaviors
      .supervise(behavior)
      .onFailure[Throwable](SupervisorStrategy.restart)
}
