package com.jourei.crawler.util

import org.jsoup.Jsoup
import org.scalatest.flatspec.AnyFlatSpec

class JsoupTest extends AnyFlatSpec {
  "JSOUP拉取的数据" should "不为空" in {
    val document = Jsoup
      .connect(
        "https://docs.scala-lang.org/scala3/book/taste-objects.html"
      )
      .get()
    val text = document
      .select(
        "#inner-main > section.content > div > div.content-primary.documentation > div > div.toc-context > ul > li:nth-child(2)"
      )
      .text()
    assert(
      "A companion object is an object that has the same name as the class it shares a file with. " +
        "In this situation, that class is also called a companion class." == text
    )
  }
}
