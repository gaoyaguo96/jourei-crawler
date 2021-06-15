package com.jourei.crawler.util.interpreter

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.pattern.StatusReply
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop._
import org.scalatest.propspec.AnyPropSpec
class HtmlHelperTest
    extends AnyPropSpec
    with TableDrivenPropertyChecks
    with BeforeAndAfterAll
    with Matchers {
  private val testKit = ActorTestKit()
  private val examples =
    Table("set", testKit)

  property("an empty Set should have size 0") {
    forAll(examples) { testKit =>
      val extractionCoordinator =
        testKit.spawn(ExtractionCoordinator(), "extraction-coordinator")
      val probe = testKit.createTestProbe[StatusReply[Seq[Seq[String]]]]()
      extractionCoordinator ! ExtractionCoordinator
        .GetAllWithRawSelectors(
          "#s_xmancard_news_new > div > div.s-news-rank-wrapper.s-news-special-rank-wrapper.c-container-r > div > div > ul > li:nth-child(1) > a > span.title-content-title",
          "https://www.baidu.com",
          probe.ref)
      probe.expectMessage(StatusReply.success(Seq(Seq("go"))))
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
