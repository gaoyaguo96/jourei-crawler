package com.jourei.crawler.executor.protocol

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import akka.pattern.StatusReply
import akka.util.Timeout
import HtmlHelper.ExtractAll
import HttpHelper.{ GetHtml, Html }
import com.jourei.crawler.executor.util.SupervisionUtils
import okhttp3.OkHttpClient

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

object TextFetchHelper {
  sealed trait Command
  final case class Get(
      selector: String,
      url: String,
      replyTo: ActorRef[StatusReply[Seq[String]]])
      extends Command
  final case class GetAll(
      selectors: Seq[String],
      url: String,
      replyTo: ActorRef[StatusReply[Seq[Seq[String]]]])
      extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val okHttpClient: OkHttpClient =
        new OkHttpClient.Builder()
          .connectTimeout(3, TimeUnit.SECONDS)
          .readTimeout(5, TimeUnit.SECONDS)
          .writeTimeout(4, TimeUnit.SECONDS)
          .retryOnConnectionFailure(true)
          .build()

      implicit val httpHelper: ActorRef[HttpHelper.Command] =
        context.spawn(
          Behaviors
            .supervise(HttpHelper.apply)
            .onFailure[Throwable](SupervisorStrategy.restart),
          "http-helper")

      implicit val htmlHelper: ActorRef[HtmlHelper.Command] =
        context.spawn(
          Behaviors
            .supervise(HtmlHelper())
            .onFailure[Throwable](SupervisorStrategy.restart),
          "html-helper")

      Behaviors.receiveMessage {
        case Get(selector, url, replyTo) =>
          implicit val timeout: Timeout = Timeout(250.millis)
          context.askWithStatus(context.self, GetAll(Seq(selector), url, _)) {
            case Failure(StatusReply.ErrorMessage(msg)) =>
              ValidationError(msg, replyTo)
            case Failure(exception) => InternalError(exception, replyTo)
            case Success(value)     => ToSingle(value, replyTo)
          }
          Behaviors.unhandled
        case GetAll(selectors, url, replyTo) =>
          context.spawnAnonymous(
            SupervisionUtils.withStop(TextFetcher(selectors)(url)(replyTo)))
          Behaviors.same
        case ToSingle(seq, replyTo) =>
          replyTo ! StatusReply.success(seq.head)
          Behaviors.same
        case ValidationError(msg, replyTo) =>
          replyTo ! StatusReply.error(msg)
          Behaviors.same
        case InternalError(e, replyTo) =>
          context.log.error("未知错误", e)
          replyTo ! StatusReply.error("未知错误")
          Behaviors.same
      }
    }
  final private case class ToSingle(
      seq: Seq[Seq[String]],
      replyTo: ActorRef[StatusReply[Seq[String]]])
      extends Command
  final private case class ValidationError(
      msg: String,
      replyTo: ActorRef[StatusReply[Seq[String]]])
      extends Command
  final private case class InternalError[A](
      e: Throwable,
      replyTo: ActorRef[StatusReply[Seq[String]]])
      extends Command
}

private object TextFetcher {
  sealed private trait Event
  final private case class HtmlReceived(html: String) extends Event
  final private case class TextsReceived(texts: Seq[Seq[String]]) extends Event
  final private case object InvalidURL extends Event
  final private case class HttpHelperError(e: Throwable) extends Event
  final private case class HtmlHelperError(e: Throwable) extends Event

  def apply(selectors: Seq[String])(url: String)(
      replyTo: ActorRef[StatusReply[Seq[Seq[String]]]])(
      implicit httpHelper: ActorRef[HttpHelper.Command],
      htmlHelper: ActorRef[HtmlHelper.Command]): Behavior[NotUsed] =
    Behaviors
      .setup[Event] { context =>
        implicit val timeout: Timeout = Timeout(2.seconds)
        context.askWithStatus(httpHelper, GetHtml(url, _)) {
          case Failure(StatusReply.ErrorMessage(_)) => InvalidURL
          case Failure(e)                           => HttpHelperError(e)
          case Success(Html(value))                 => HtmlReceived(value)
        }

        Behaviors.receiveMessage {
          case HtmlReceived(html) =>
            context.ask(htmlHelper, ExtractAll(selectors, html, _)) {
              case Failure(e)     => HtmlHelperError(e)
              case Success(value) => TextsReceived(value)
            }
            Behaviors.unhandled
          case TextsReceived(text) =>
            replyTo ! StatusReply.success(text)
            Behaviors.stopped
          case InvalidURL =>
            replyTo ! StatusReply.Error("invalid URL")
            Behaviors.stopped
          case HttpHelperError(e) =>
            replyTo ! StatusReply.Error(e)
            Behaviors.stopped
          case HtmlHelperError(e) =>
            replyTo ! StatusReply.Error(e)
            Behaviors.stopped
        }
      }
      .asInstanceOf[Behavior[NotUsed]]
}
