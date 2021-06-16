package com.jourei.crawler.service.adapter

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.util.Timeout
import com.jourei.crawler.protocol.TextFetchHelper
import com.jourei.crawler.protocol.TextFetchHelper.GetAll
import com.jourei.crawler.service.TextFetchService

import scala.concurrent.Future

class TextFetchServiceActorAdapter(
    fetchHelper: ActorRef[TextFetchHelper.Command])(
    implicit timeout: Timeout,
    scheduler: Scheduler)
    extends TextFetchService {
  def fetchAll(selectors: Seq[String])(url: String): Future[Seq[Seq[String]]] =
    fetchHelper.askWithStatus(GetAll(selectors, url, _))

  def fetch(selector: String)(url: String): Future[Seq[String]] =
    fetchHelper.askWithStatus(TextFetchHelper.Get(selector, url, _))
}
