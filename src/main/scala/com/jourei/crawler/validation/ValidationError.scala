package com.jourei.crawler.validation

sealed trait ValidationError
object ValidationError {
  final case object UrlParamMissing extends ValidationError
  final case object SelectorsParamMissing extends ValidationError
  final case object SelectorsEmpty extends ValidationError
}
