package com.jourei.crawler.service

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.jourei.crawler.protocol.StringHelper
import com.jourei.crawler.protocol.StringHelper.Split

import scala.concurrent.Future

final class StringService(stringHelper: ActorRef[StringHelper.Command])(
    implicit timeout: Timeout,
    scheduler: Scheduler) {
  def split(splitter: String)(string: String): Future[Seq[String]] =
    stringHelper.ask(Split(splitter, string, _))
}
