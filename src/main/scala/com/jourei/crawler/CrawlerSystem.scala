package com.jourei.crawler

import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, SupervisorStrategy }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{
  as,
  concat,
  entity,
  onSuccess,
  pathEnd,
  pathPrefix,
  post
}
import akka.projection.ProjectionBehavior
import akka.util.Timeout
import com.jourei.crawler.database.{ ProjectionFactory, ScalikeJDBCSetup }
import com.jourei.crawler.protocol.interceptor.ProxySourceInterceptor
import com.jourei.crawler.protocol.{
  ProxyFetchHelper,
  ProxyPool,
  TextFetchHelper
}
import com.jourei.crawler.route.Routes
import com.jourei.crawler.service.adapter.{
  ProxyFetchServiceActorAdapter,
  ProxySourceServiceActorAdapter,
  TextFetchServiceActorAdapter
}
import com.jourei.crawler.service.valueobject.ProxySourceDTO
import com.jourei.crawler.util.ResponseUtils.{ completeWithJsonBody, cors }
import io.circe.generic.auto.exportEncoder
import io.circe.generic.decoding.DerivedDecoder.deriveDecoder

import java.util.concurrent.TimeUnit
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object CrawlerSystem {
  def rootBehavior: Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val fetchHelper =
        context.spawn(superviseWithRestart(TextFetchHelper()), "fetch-helper")
      val proxyFetchHelper = context.spawn(
        superviseWithRestart(ProxyFetchHelper(fetchHelper)),
        "proxy-fetch-helper")

      implicit val system: ActorSystem[Nothing] = context.system
      implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)

      ScalikeJDBCSetup.init(system.settings.config.getConfig("jdbc-connection"))

      val proxyPoolProjectionTag = "proxy-pool-0"
      val proxyPoolActor =
        context.spawn(
          superviseWithRestart(ProxyPool(proxyPoolProjectionTag)),
          "proxy-pool")
      val proxyPoolProjection = ProjectionFactory.get(proxyPoolProjectionTag)
      val proxyPoolProjectionActor =
        context.spawn(
          superviseWithRestart(ProjectionBehavior(proxyPoolProjection)),
          "proxy-pool-projection")

      val proxySourceProjectionTag = "proxy-source-0"
      val proxySourceActor =
        context.spawn(
          superviseWithRestart(
            ProxySourceInterceptor(proxySourceProjectionTag)),
          "proxy-source")

      val proxySourceService =
        new ProxySourceServiceActorAdapter(proxySourceActor)

      val proxyFetchService =
        new ProxyFetchServiceActorAdapter(proxyFetchHelper)

      val textFetchService =
        new TextFetchServiceActorAdapter(fetchHelper)

      import com.jourei.crawler.marshaller.CirceJSONSupport._
      val routes =
        cors {
          pathPrefix("proxy-source") {
            concat(
              Routes.getTextFetchPath(textFetchService),
              pathEnd {
                post {
                  entity(as[ProxySourceDTO]) { proxySource =>
                    val eventualSummary = proxySourceService.add(
                      proxySource.selector)(proxySource.url)
                    onSuccess(eventualSummary) { summary =>
                      completeWithJsonBody(summary)
                    }
                  }
                }
              })
          }
        }
      val bindingFuture: Future[Http.ServerBinding] =
        Http().newServerAt("localhost", 30000).bind(routes)

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
