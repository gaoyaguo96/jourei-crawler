package com.jourei.crawler.server.marshaller

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ ContentTypeRange, HttpEntity }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.circe.{ Decoder, Encoder }

import scala.concurrent.Future

object CirceJSONSupport {

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap { implicit ec => mat => json =>
        Future {
          decode[A](json) match {
            case Left(e)      => throw e
            case Right(value) => value
          }
        }
      }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, a.asJson.noSpaces)
    }

  private def jsonContentTypes: List[ContentTypeRange] =
    List(`application/json`)
}
