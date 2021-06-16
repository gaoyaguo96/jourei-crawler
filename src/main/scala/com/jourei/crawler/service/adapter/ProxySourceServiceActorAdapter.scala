package com.jourei.crawler.service.adapter

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.util.Timeout
import com.jourei.crawler.protocol.interceptor.ProxySourceInterceptor
import com.jourei.crawler.service.ProxySourceService

import scala.concurrent.Future

final class ProxySourceServiceActorAdapter(
    proxySource: ActorRef[ProxySourceInterceptor.proxySource.Command])(
    implicit timeout: Timeout,
    scheduler: Scheduler)
    extends ProxySourceService[Future, ProxySourceInterceptor.Summary] {
  import ProxySourceInterceptor.proxySource._
  def add(selector: String)(
      url: String): Future[ProxySourceInterceptor.Summary] =
    proxySource.ask(Add(selector, url, _))

  def remove(selector: Selector)(
      url: URL): Future[ProxySourceInterceptor.Summary] =
    proxySource.ask(Remove(selector, url, _))
}
