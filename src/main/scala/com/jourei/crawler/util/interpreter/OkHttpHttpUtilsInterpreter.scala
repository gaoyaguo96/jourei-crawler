package com.jourei.crawler.util.interpreter

import cats.Id
import com.jourei.crawler.exception.TaskException
import com.jourei.crawler.util.HttpUtils
import okhttp3.{OkHttpClient, Request}

import scala.util.Try

class OkHttpHttpUtilsInterpreter(private val okHttpClient: OkHttpClient)
    extends HttpUtils[Id] {
  def get(url: String): Either[TaskException, String] =
    Try {
      okHttpClient
        .newCall(new Request.Builder().url(url).build())
        .execute()
        .body()
        .string()
    }.toEither.left.map(_ => TaskException.HtmlGettingException)
}
