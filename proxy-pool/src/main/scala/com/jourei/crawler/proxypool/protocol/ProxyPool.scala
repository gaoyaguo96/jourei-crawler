package com.jourei.crawler.proxypool.protocol

import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{
  Effect,
  EventSourcedBehavior,
  RetentionCriteria
}
import com.jourei.crawler.serializable.CborSerializable
import monocle.Monocle.toAppliedFocusOps

import scala.concurrent.duration.DurationInt

/** 代理池
 *
 *  @author
 *    Jourei
 *  @since
 *    0.0.1
 */
object ProxyPool {
  sealed trait Command
  final case class Get(replyTo: ActorRef[Summary]) extends Command
  final case class Add(host: Host, port: Port, replyTo: ActorRef[Summary])
      extends Command
  final case class AddInBatches(
      proxies: Set[(Host, Port)],
      replyTo: ActorRef[Summary])
      extends Command
  final case class Remove(host: Host, port: Port, replyTo: ActorRef[Summary])
      extends Command

  sealed trait Event extends CborSerializable
  final case class Added(host: Host, port: Port) extends Event
  final case class AddedAll(proxies: Set[(Host, Port)]) extends Event
  final case class Removed(host: Host, port: Port) extends Event

  final case class State(proxies: Set[(Host, Port)]) {
    def toSummary: Summary = Summary(proxies)
  }

  final case class Summary(proxies: Set[(Host, Port)])

  private type Host = String
  private type Port = Int

  def apply(projectionTag: String): Behavior[Command] =
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId("ProxyPool", "proxy-pool-1"),
        emptyState = State.empty,
        commandHandler = { (state, command) =>
          command match {
            case Get(replyTo) => Effect.reply(replyTo)(state.toSummary)
            case Add(host, port, replyTo) =>
              Effect.persist(Added(host, port)).thenReply(replyTo)(_.toSummary)
            case AddInBatches(proxies, replyTo) =>
              Effect.persist(AddedAll(proxies)).thenReply(replyTo)(_.toSummary)
            case Remove(host, port, replyTo) =>
              Effect
                .persist(Removed(host, port))
                .thenReply(replyTo)(_.toSummary)
          }
        },
        eventHandler = { (state, event) =>
          event match {
            case Added(host, port)   => State.add(host, port)(state)
            case AddedAll(proxies)   => State.addAll(proxies)(state)
            case Removed(host, port) => State.remove(host, port)(state)
          }
        })
      .withRetention(RetentionCriteria.snapshotEvery(100, 3))
      .withTagger(_ => Set(projectionTag))
      .onPersistFailure(SupervisorStrategy
        .restartWithBackoff(200.millis, 5.seconds, 0.1))

  private object State {
    def empty: State = State(Set.empty)
    def add(host: Host, port: Port)(state: State): State =
      state.focus(_.proxies).modify(_ + (host -> port))

    def addAll(proxies: Set[(Host, Port)])(state: State): State =
      state.focus(_.proxies).modify(_ | proxies)

    def remove(host: Host, port: Port)(state: State): State =
      state.focus(_.proxies).modify(_ - (host -> port))
  }
}
