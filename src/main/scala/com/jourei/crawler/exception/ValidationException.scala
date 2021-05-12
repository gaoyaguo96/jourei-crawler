package com.jourei.crawler.exception

sealed trait ValidationException extends AppException

object ValidationException {
  case object UrlParamMissingException extends ValidationException
  case object SelectorsParamMissingException extends ValidationException
  case object SelectorsEmptyException extends ValidationException
}
