package com.jourei.crawler.protocol
import akka.actor.typed.ActorRef
import com.jourei.crawler.serializable.CborSerializable

trait ProxySource[Summary] {
  sealed trait Command extends CborSerializable
  final case class Add(selector: Selector, url: URL, replyTo: ActorRef[Summary])
      extends Command
  final case class Remove(
      selector: Selector,
      url: URL,
      replyTo: ActorRef[Summary])
      extends Command

  type Selector = String
  type URL = String
}
