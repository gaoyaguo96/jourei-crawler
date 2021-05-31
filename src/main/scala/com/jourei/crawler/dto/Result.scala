package com.jourei.crawler.dto

final case class Result[A](succeed: Boolean, data: A)
