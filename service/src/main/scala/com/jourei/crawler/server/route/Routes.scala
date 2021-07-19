package com.jourei.crawler.server.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jourei.crawler.server.route.dto.AddProxySourceDTO
import com.jourei.crawler.server.util.ResponseUtils.completeWithJsonBody
import com.jourei.crawler.server.marshaller.CirceJSONSupport.unmarshaller
import com.jourei.crawler.server.marshaller.CirceJSONSupport.marshaller
import io.circe.generic.decoding.DerivedDecoder.deriveDecoder

import scala.concurrent.Future

object Routes {
  val appRoutes =
    path("add-proxy-source") {
      post {
        entity(as[AddProxySourceDTO]) {
          
        }
      }
    }
}
