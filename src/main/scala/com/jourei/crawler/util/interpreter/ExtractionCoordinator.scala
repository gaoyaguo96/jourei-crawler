package com.jourei.crawler.util.interpreter

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.pattern.StatusReply
import akka.util.Timeout
import com.jourei.crawler.util.interpreter.HtmlHelper.{
  ExtractAll,
  ExtractFirstRespectively
}
import com.jourei.crawler.util.interpreter.HttpHelper.GetHtml
import okhttp3.OkHttpClient

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object ExtractionCoordinator {
  sealed trait Command
  final case class GetInBatches(
      selectors: Seq[String],
      url: String,
      replyTo: ActorRef[StatusReply[Seq[String]]]
  ) extends Command
  final case class GetRespectivelyInBatches(
      selectors: Seq[String],
      url: String,
      replyTo: ActorRef[StatusReply[Seq[String]]]
  ) extends Command
  final case class GetRespectivelyInBatchesUsingRawSelectors(
      selectors: String,
      url: String,
      replyTo: ActorRef[StatusReply[Seq[String]]]
  ) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val okHttpClient: OkHttpClient = new OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

      implicit val httpHelper: ActorRef[HttpHelper.Command] = context.spawn(
        Behaviors
          .supervise(HttpHelper.apply)
          .onFailure[Throwable](SupervisorStrategy.restart),
        "http-utils"
      )
      implicit val htmlHelper: ActorRef[HtmlHelper.Command] = context.spawn(
        Behaviors
          .supervise(HtmlHelper())
          .onFailure[Throwable](SupervisorStrategy.restart),
        "html-utils"
      )

      Behaviors.receiveMessage {
        case GetInBatches(selectors, url, replyTo) =>
          context.spawnAnonymous(
            Behaviors
              .supervise(
                TextFetcher(extractAll = false)(selectors)(url)(replyTo)
              )
              .onFailure[Throwable](SupervisorStrategy.stop)
          )
          Behaviors.same
        case GetRespectivelyInBatches(selectors, url, replyTo) =>
          context.spawnAnonymous(
            Behaviors
              .supervise(
                TextFetcher(extractAll = true)(selectors)(url)(replyTo)
              )
              .onFailure[Throwable](SupervisorStrategy.stop)
          )
          Behaviors.same
        case GetRespectivelyInBatchesUsingRawSelectors(
              selectors,
              url,
              replyTo
            ) =>
          context.self ! GetRespectivelyInBatches(
            selectors.split('!'),
            url,
            replyTo
          )
          Behaviors.same
      }
    }
}

private object TextFetcher {
  private sealed trait Event
  private case class WrappedHtml(html: String) extends Event
  private case class WrappedTexts(texts: Seq[String]) extends Event
  private case object InvalidURL extends Event
  private case class HttpHelperError(e: Throwable) extends Event
  private case class HtmlHelperError(e: Throwable) extends Event

  def apply(extractAll: Boolean)(
      selectors: Seq[String]
  )(url: String)(replyTo: ActorRef[StatusReply[Seq[String]]])(implicit
      httpHelper: ActorRef[HttpHelper.Command],
      htmlHelper: ActorRef[HtmlHelper.Command]
  ): Behavior[NotUsed] =
    Behaviors
      .setup[Event] { context =>
        implicit val timeout: Timeout = Timeout(2.seconds)
        context.askWithStatus(httpHelper, GetHtml(url, _)) {
          case Failure(StatusReply.ErrorMessage(_)) => InvalidURL
          case Success(html) => WrappedHtml(html.value)
          case Failure(e) => HttpHelperError(e)
        }

        Behaviors.receiveMessage {
          case WrappedHtml(html) if extractAll =>
            context.ask(
              htmlHelper,
              ExtractAll(selectors, html, _)
            ) {
              case Failure(e) => HtmlHelperError(e)
              case Success(texts) => WrappedTexts(texts.value)
            }
            Behaviors.unhandled
          case WrappedHtml(html) =>
            context.ask(
              htmlHelper,
              ExtractFirstRespectively(selectors, html, _)
            ) {
              case Failure(e) => HtmlHelperError(e)
              case Success(tests) => WrappedTexts(tests.value)
            }
            Behaviors.unhandled
          case WrappedTexts(text) =>
            replyTo ! StatusReply.success(text)
            Behaviors.stopped
          case InvalidURL =>
            replyTo ! StatusReply.Error("invalid url")
            Behaviors.stopped
          case HttpHelperError(msg) =>
            context.log.error("http helper error", msg)
            Behaviors.stopped
          case HtmlHelperError(msg) =>
            context.log.error("html helper error", msg)
            Behaviors.stopped
        }
      }
      .asInstanceOf[Behavior[NotUsed]]
}
