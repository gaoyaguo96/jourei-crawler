package com.jourei.crawler.route

import akka.http.scaladsl.server.Directives.{ get, onSuccess, parameters, path }
import akka.http.scaladsl.server.Route
import com.jourei.crawler.service.TextFetchService
import com.jourei.crawler.util.ResponseUtils.completeWithJsonBody

object Routes {
  def getTextFetchPath(textFetchService: TextFetchService): Route =
    path("fetch") {
      get {
        parameters("url", "selector") { (url, selector) =>
          onSuccess(textFetchService.fetch(selector)(url)) { html =>
            completeWithJsonBody(html)
          }
        }
      }
    }
}
