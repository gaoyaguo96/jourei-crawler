package com.jourei.crawler.proxysource.protocol

import akka.actor.typed.{ ActorRef, Behavior }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{
  Effect,
  EventSourcedBehavior,
  RetentionCriteria
}
import com.jourei.crawler.serializable.CborSerializable
import monocle.Monocle.toAppliedFocusOps

object ProxySource {
  sealed trait Command extends CborSerializable
  final case class Add(selector: Selector, url: URL, replyTo: ActorRef[Summary])
      extends Command
  final case class Remove(
      selector: Selector,
      url: URL,
      replyTo: ActorRef[Summary])
      extends Command
  sealed trait Event extends CborSerializable
  final case class Added(selector: Selector, url: URL) extends Event
  final case class Removed(selector: Selector, url: URL) extends Event

  final case class State(sources: Set[(Selector, URL)])
      extends CborSerializable {
    def toSummary: Summary = Summary(sources)
  }
  final case class Summary(sources: Set[(Selector, URL)])
      extends CborSerializable

  type Selector = String
  type URL = String

  def apply(projectionTag: String): Behavior[Command] =
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId("ProxySource", "proxy-source-0"),
        emptyState = State.empty,
        commandHandler = { (_, command) =>
          command match {
            case Add(selector, url, replyTo) =>
              Effect
                .persist(Added(selector, url))
                .thenReply(replyTo)(_.toSummary)
            case Remove(selector, url, replyTo) =>
              Effect
                .persist(Removed(selector, url))
                .thenReply(replyTo)(_.toSummary)
          }
        },
        eventHandler = { (state, event) =>
          event match {
            case Added(selector, url)   => State.add(selector)(url)(state)
            case Removed(selector, url) => State.remove(selector)(url)(state)
          }
        })
      .withRetention(RetentionCriteria.snapshotEvery(100, 3))
      .withTagger(_ => Set(projectionTag))

  object State {
    def add(selector: Selector)(url: URL)(state: State): State =
      state.focus(_.sources).modify(_ + (selector -> url))

    def remove(selector: Selector)(url: URL)(state: State): State =
      state.focus(_.sources).modify(_ - (selector -> url))

    def empty: State = State(Set.empty)
  }
}
