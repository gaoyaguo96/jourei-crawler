package com.jourei.crawler.util

import org.jsoup.Jsoup
import org.scalatest.flatspec.AnyFlatSpec

import scala.jdk.CollectionConverters.CollectionHasAsScala

class JsoupTest extends AnyFlatSpec {
  "JSOUP拉取的数据" should "不为空" in {
    val document = Jsoup
      .connect(
        "http://www.66ip.cn/index.html"
      )
      .get()
    val text = document
      .select(
        "#main > div.containerbox.boxindex > div.layui-row.layui-col-space15 > div:nth-child(1) > table > tbody > tr:nth-child(2)~tr > td:nth-child(1)+td"
      )
      .eachText()
      .asScala
    assert(Set("45.71.115.211", "999") == Set.from(text))
  }
}
