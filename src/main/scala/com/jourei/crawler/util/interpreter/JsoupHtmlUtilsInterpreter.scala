package com.jourei.crawler.util.interpreter

import cats.Id
import com.jourei.crawler.exception.{
  AppException,
  TaskException,
  ValidationException
}
import com.jourei.crawler.util.HtmlUtils
import org.jsoup.Jsoup

import scala.util.Try

class JsoupHtmlUtilsInterpreter extends HtmlUtils[Id] {
  def extract(selectors: String*)(html: String): Either[AppException, String] =
    for {
      first <- selectors.headOption.toRight(ValidationException.SelectorsEmptyException)
      document = Jsoup.parse(html)
      text <- Try {
        selectors.tail
          .foldLeft(document.select(first))(_.select(_))
          .text()
      }.toEither.left.map(_ => TaskException.ElementsSelectionException)
    } yield text
}
