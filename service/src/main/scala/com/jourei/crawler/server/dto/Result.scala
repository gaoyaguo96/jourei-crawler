package com.jourei.crawler.server.dto

final case class Result[A](succeed: Boolean, data: A)
