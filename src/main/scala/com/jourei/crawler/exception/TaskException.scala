package com.jourei.crawler.exception

sealed trait TaskException extends AppException

object TaskException {
  case object HtmlGettingException extends TaskException
  case object HtmlParsingException extends TaskException
  case object ElementsSelectionException extends TaskException
}
