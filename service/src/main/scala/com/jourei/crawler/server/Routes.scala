package com.jourei.crawler.server
import akka.http.scaladsl.server.Directives._

object Routes {
  val appRoutes = path("/add-proxy-source"){
    post{
      entity(as[])
    }
  }
}