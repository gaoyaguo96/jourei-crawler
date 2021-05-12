package com.jourei.crawler.verticle

import com.jourei.crawler.exception.ValidationException
import com.jourei.crawler.functional.Constants.AppEither
import com.jourei.crawler.util.interpreter.{
  JsoupHtmlUtilsInterpreter,
  OkHttpHttpUtilsInterpreter
}
import com.jourei.crawler.vertx.FutureUtils
import io.vertx.core.json.JsonObject
import io.vertx.core.{AbstractVerticle, Promise}
import io.vertx.ext.web.Router
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.{ConfigurationSource, Configurator}

import java.io.{BufferedInputStream, FileInputStream}
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Using}

class MainVerticle extends AbstractVerticle {
  override def start(startPromise: Promise[Void]): Unit = {

    val logger =
      Using(
        new BufferedInputStream(
          new FileInputStream(
            "C:\\Users\\44724\\projects\\scala\\jourei-keiko\\src\\main\\resources\\log4j2.xml"
          )
        )
      ) { inputStream =>
        Configurator.initialize(
          this.getClass.getClassLoader,
          new ConfigurationSource(inputStream)
        )
        LogManager.getLogger("RollingRandomAccessFileLogger")
      } match {
        case Failure(e) =>
          e.printStackTrace()
          return
        case Success(value) => value
      }

    val okHttpClient = new OkHttpClient.Builder()
      .connectTimeout(3, TimeUnit.SECONDS)
      .readTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(4, TimeUnit.SECONDS)
      .retryOnConnectionFailure(true)
      .build()

    val httpUtils = new OkHttpHttpUtilsInterpreter(okHttpClient)
    val htmlUtils = new JsoupHtmlUtilsInterpreter

    val router = Router.router(vertx)
    router.get("/get").handler { context =>
      val request = context.request()

      val htmlFuture = FutureUtils.fromEitherAsync {
        Option(request.getParam("url"))
          .toRight(ValidationException.UrlParamMissingException)
          .flatMap(httpUtils.get)
      }
      val selectorsFuture = FutureUtils.fromEitherAsync {
        Option(request.getParam("selectors"))
          .toRight(ValidationException.SelectorsParamMissingException)
          .map(_.split(","))
      }
      htmlFuture
        .thenCombineAsync(
          selectorsFuture,
          (html: String, selectors: Array[String]) => {
            htmlUtils.extract(selectors: _*)(html)
          }
        )
        .thenAcceptAsync { errorOrText: AppEither[String] =>
          errorOrText match {
            case Left(e) =>
              logger.error(e)
              context.json(
                new JsonObject()
                  .put("succeed", false)
                  .put("error", e.getMessage)
              )
            case Right(value) =>
              context.json(
                new JsonObject()
                  .put("text", value)
              )
          }
        }
    }
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888)
      .onSuccess { server =>
        logger.info(s"Listening on ${server.actualPort()}...")
      }
      .onFailure(_ => logger.error("Failed to start this verticle"))
  }
}
