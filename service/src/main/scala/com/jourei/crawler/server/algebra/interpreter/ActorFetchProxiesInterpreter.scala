package com.jourei.crawler.server.algebra.interpreter

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.util.Timeout
import com.jourei.crawler.executor.protocol.TextFetchHelper
import com.jourei.crawler.executor.protocol.TextFetchHelper.Get
import com.jourei.crawler.server.algebra.FetchProxies

import scala.concurrent.Future

final class ActorFetchProxiesInterpreter private (
    textFetchHelper: ActorRef[TextFetchHelper.Command])(
    implicit timeout: Timeout,
    scheduler: Scheduler)
    extends FetchProxies[Future] {
  def fetchText(selector: String)(url: String): Future[Seq[String]] =
    textFetchHelper.askWithStatus(Get(selector, url, _))
}

object ActorFetchProxiesInterpreter {
  def apply(textFetchHelper: ActorRef[TextFetchHelper.Command])(
      timeout: Timeout)(scheduler: Scheduler): ActorFetchProxiesInterpreter =
    new ActorFetchProxiesInterpreter(textFetchHelper)(timeout, scheduler)
}
