package com.jourei.crawler.server.route.dto

final case class CommonReply[A](success: Boolean, data: A)
