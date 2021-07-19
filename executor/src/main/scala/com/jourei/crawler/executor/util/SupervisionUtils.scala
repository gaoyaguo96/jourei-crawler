package com.jourei.crawler.executor.util

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ Behavior, SupervisorStrategy }

object SupervisionUtils {
  def withRestart[A](behavior: Behavior[A]): Behavior[A] =
    Behaviors
      .supervise(behavior)
      .onFailure[Throwable](SupervisorStrategy.restart)

  def withStop[A](behavior: Behavior[A]): Behavior[A] =
    Behaviors.supervise(behavior).onFailure[Throwable](SupervisorStrategy.stop)
}
