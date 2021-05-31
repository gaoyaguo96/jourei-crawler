package com.jourei.crawler.api

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.jourei.crawler.util.interpreter.StringHelper
import com.jourei.crawler.util.interpreter.StringHelper.Split

import scala.concurrent.Future

final class StringUtils(stringHelper: ActorRef[StringHelper.Command])(implicit
    timeout: Timeout,
    scheduler: Scheduler
) {
  def split(splitter: String)(string: String): Future[Seq[String]] =
    stringHelper.ask(Split(splitter, string, _))
}
