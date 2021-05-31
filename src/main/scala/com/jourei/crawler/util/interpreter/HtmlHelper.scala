package com.jourei.crawler.util.interpreter

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.jsoup.Jsoup

object HtmlHelper {
  sealed trait Command
  final case class Extract(
      selector: String,
      html: String,
      replyTo: ActorRef[Text]
  ) extends Command

  final case class ExtractFirstRespectively(
      selectors: Seq[String],
      html: String,
      replyTo: ActorRef[Texts]
  ) extends Command

  final case class ExtractAll(
      selectors: Seq[String],
      html: String,
      replyTo: ActorRef[Texts]
  ) extends Command

  final case class Text(value: String)
  final case class Texts(value: Seq[String])

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Extract(selector, html, replyTo) =>
        val document = Jsoup parse html
        val text = document.select(selector).first().text()
        replyTo ! Text(text)
        Behaviors.same
      case ExtractFirstRespectively(selectors, html, replyTo) =>
        val document = Jsoup parse html
        val texts =
          selectors map (document.select(_).first().text())
        replyTo ! Texts(texts)
        Behaviors.same
      case ExtractAll(selectors, html, replyTo) =>
        val document = Jsoup parse html
        val filtered = selectors.filterNot(_.matches("""^ *$"""))
        val texts = filtered.map(document.select(_).text())
        if (texts.isEmpty) {
          val texts = Seq(document.text())
          replyTo ! Texts(texts)
        } else replyTo ! Texts(texts)
        Behaviors.same
    }
}
