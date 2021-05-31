package com.jourei.crawler.util

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Credentials`,
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpResponse,
  StatusCodes
}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Route, StandardRoute}
import io.circe.Encoder

object ResponseUtils {
  import io.circe.syntax.EncoderOps

  def completeWithJsonBody[A](a: A)(implicit
      encoder: Encoder[A]
  ): StandardRoute =
    complete {
      HttpResponse(
        entity = HttpEntity(
          ContentTypes.`application/json`,
          a.asJson.spaces2
        )
      )
    }

  import akka.http.scaladsl.server.Directives._
  def cors(r: Route): Route =
    respondWithHeaders(corsResponseHeaders)(preflightRequestHandler ~ r)

  private val corsResponseHeaders =
    Seq(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Credentials`(false),
      `Access-Control-Allow-Headers`("Authorization", "X-Requested-With")
    )

  private def preflightRequestHandler: Route =
    options {
      complete(
        HttpResponse(StatusCodes.OK).withHeaders(
          `Access-Control-Allow-Methods`(OPTIONS, GET, POST, PUT, PATCH, DELETE)
        )
      )
    }
}
