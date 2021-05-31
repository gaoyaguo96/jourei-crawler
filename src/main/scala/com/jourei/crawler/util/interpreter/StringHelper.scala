package com.jourei.crawler.util.interpreter

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object StringHelper {
  sealed trait Command
  final case class Split(
      splitter: String,
      string: String,
      replyTo: ActorRef[Seq[String]]
  ) extends Command

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage { case Split(splitter, string, replyTo) =>
      replyTo ! string.split(splitter)
      Behaviors.same
    }
}
