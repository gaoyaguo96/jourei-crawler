package com.jourei.crawler.util.adapter
import akka.actor.typed.scaladsl.AskPattern.{
  schedulerFromActorSystem,
  Askable
}
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.util.Timeout
import com.jourei.crawler.util.interpreter.ProxyFetchHelper
import com.jourei.crawler.util.interpreter.ProxyFetchHelper.FetchProxies

import scala.concurrent.Future

final class ProxyFetchServiceActorAdapter(
    proxyHelper: ActorRef[ProxyFetchHelper.Command])(
    implicit timeout: Timeout,
    system: ActorSystem[_])
    extends ProxyFetchService {
  def fetchProxies(selector: String, url: String): Future[Set[(Host, Port)]] =
    proxyHelper.askWithStatus(FetchProxies(selector, url, _))
}
