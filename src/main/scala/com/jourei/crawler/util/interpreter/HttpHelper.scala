package com.jourei.crawler.util.interpreter

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.StatusReply
import okhttp3._

import java.io.IOException

object HttpHelper {
  sealed trait Command
  final case class GetHtml(url: String, replyTo: ActorRef[StatusReply[Html]])
      extends Command

  sealed trait Event
  final case class Html(value: String) extends Event

  def apply(implicit okHttpClient: OkHttpClient): Behavior[Command] =
    Behaviors.receiveMessage { case GetHtml(url, replyTo) =>
      if (!url.matches("https?://.+")) {
        replyTo ! StatusReply.Error("invalid URL")
      } else {
        val request = new Request.Builder()
          .url(url)
          .header(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
              " AppleWebKit/537.36 (KHTML, like Gecko)" +
              " Chrome/90.0.4430.212" +
              " Safari/537.36" +
              " Edg/90.0.818.62"
          )
          .build()

        okHttpClient
          .newCall(request)
          .enqueue(new Callback {
            def onFailure(call: Call, e: IOException): Unit = throw e

            def onResponse(call: Call, response: Response): Unit = {
              replyTo ! StatusReply.success(Html(response.body().string()))
            }
          })
      }

      Behaviors.same
    }
}
