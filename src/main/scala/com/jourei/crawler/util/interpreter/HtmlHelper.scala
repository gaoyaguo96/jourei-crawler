package com.jourei.crawler.util.interpreter

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import org.jsoup.Jsoup

import scala.jdk.CollectionConverters.CollectionHasAsScala

object HtmlHelper {
  sealed trait Command
  final case class Extract(
      selector: String,
      html: String,
      replyTo: ActorRef[Seq[String]])
      extends Command

  final case class ExtractAll(
      selectors: Seq[String],
      html: String,
      replyTo: ActorRef[Seq[Seq[String]]])
      extends Command

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Extract(selector, html, replyTo) =>
        val document = Jsoup.parse(html)
        val text = document.select(selector).eachText().asScala.toSeq
        replyTo ! text
        Behaviors.same
      case ExtractAll(selectors, html, replyTo) =>
        val document = Jsoup.parse(html)
        val filtered = selectors.filterNot(_.matches("""^ *$"""))
        val texts = filtered.map(document.select(_).eachText().asScala.toSeq)
        replyTo ! texts
        Behaviors.same
    }
}
