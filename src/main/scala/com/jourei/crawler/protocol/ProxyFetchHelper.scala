package com.jourei.crawler.protocol

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.pattern.StatusReply
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success, Try }

object ProxyFetchHelper {
  sealed trait Command
  final case class FetchProxies(
      selector: String,
      url: String,
      replyTo: ActorRef[StatusReply[Set[(String, Int)]]])
      extends Command

  def apply(fetch: ActorRef[TextFetchHelper.Get]): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case FetchProxies(selector, url, replyTo) =>
          implicit val timeout: Timeout = Timeout(250.millis)
          context.askWithStatus(
            fetch,
            TextFetchHelper.Get(selector, url, _)) {
            case Failure(exception) => FetchError(exception, replyTo)
            case Success(value)     => SendResult(value, replyTo)
          }
          Behaviors.unhandled
        case SendResult(result, replyTo) =>
          if (0 != result.length / 2)
            replyTo ! StatusReply.error("爬到的代理列表有问题")
          Try {
            result.grouped(2).map(pair => (pair.head, pair(1).toInt)).toSet
          } match {
            case Failure(exception) =>
              context.self ! FetchError(exception, replyTo)
            case Success(value) => replyTo ! StatusReply.success(value)
          }
          Behaviors.same
        case FetchError(StatusReply.ErrorMessage(msg), replyTo) =>
          context.log.error("爬取代理出错", msg)
          replyTo ! StatusReply.error(msg)
          Behaviors.same
        case FetchError(e, replyTo) =>
          context.log.error("未知错误", e)
          replyTo ! StatusReply.error("未知错误")
          Behaviors.same
      }
    }

  private type Host = String
  private type Port = Int
  private type Selector = String
  private type Url = String
  private type Text = String

  final private case class FetchError(
      e: Throwable,
      replyTo: ActorRef[StatusReply[Nothing]])
      extends Command
  final private case class SendResult(
      result: Seq[String],
      replyTo: ActorRef[StatusReply[Set[(String, Int)]]])
      extends Command
}
