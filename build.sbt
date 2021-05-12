lazy val root = project
  .in(file("."))
  .settings(
    name := "jourei-keiko",
    version := "0.1",
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.6.0" withSources () withJavadoc (),
      "io.vertx" % "vertx-web" % "4.0.3" withSources () withJavadoc (),
      "org.apache.logging.log4j" % "log4j-core" % "2.14.1" withSources () withJavadoc (),
      "com.squareup.okhttp3" % "okhttp" % "4.9.1" withSources () withJavadoc (),
      "org.jsoup" % "jsoup" % "1.13.1" withSources () withJavadoc (),
      "com.squareup.okio" % "okio" % "2.10.0" withSources () withJavadoc (),
      "io.circe" %% "circe-parser" % "0.13.0" withSources () withJavadoc ()
    )
  )
