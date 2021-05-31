lazy val root = project
  .in(file("."))
  .settings(
    name := "jourei-keiko",
    version := "0.1",
    scalaVersion := "3.0.0",
    crossScalaVersions := Seq("3.0.0", "2.13.6"),
      libraryDependencies ++= Seq (
        "com.typesafe.akka" % "akka-actor-typed_2.13",
        "com.typesafe.akka" % "akka-stream_2.13"
      ).map(_ % "2.6.14" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-http_2.13",
      "com.typesafe.akka" % "akka-http-spray-json_2.13"
    ).map(_ % "10.2.4" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.14.1" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.6.1" withSources () withJavadoc (),
      "com.chuusai" % "shapeless_2.13" % "2.3.7" withSources () withJavadoc (),
      "ch.qos.logback" % "logback-classic" % "1.2.3" withSources () withJavadoc (),
      "com.squareup.okhttp3" % "okhttp" % "4.9.1" withSources () withJavadoc (),
      "org.jsoup" % "jsoup" % "1.13.1" withSources () withJavadoc (),
      "com.typesafe.akka" % "akka-actor-testkit-typed_2.13" % "2.6.14" % Test withSources () withJavadoc (),
      "org.scalatest" %% "scalatest" % "3.2.9" % Test withSources ()
    )
  )
