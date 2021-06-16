lazy val root = project
  .in(file("."))
  .settings(
    scalacOptions ++= Seq("-target:11", "-Xsource:3"),
    name := "jourei-keiko",
    version := "0.1",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed",
      "com.typesafe.akka" %% "akka-persistence-typed",
      "com.typesafe.akka" %% "akka-stream",
      "com.typesafe.akka" %% "akka-serialization-jackson")
      .map(_ % "2.6.15" withSources () withJavadoc ()),
    libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-http")
      .map(_ % "10.2.4" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-projection-jdbc",
      "com.lightbend.akka" %% "akka-projection-eventsourced")
      .map(_ % "1.2.1" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser")
      .map(_ % "0.14.1" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "dev.optics" %% "monocle-core",
      "dev.optics" %% "monocle-macro")
      .map(_ % "3.0.0-RC2" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "org.scalikejdbc" %% "scalikejdbc-config",
      "org.scalikejdbc" %% "scalikejdbc")
      .map(_ % "3.5.0" withSources () withJavadoc ()),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.6.1" withJavadoc (),
      "dev.optics" %% "monocle-core" % "3.0.0-RC2" withJavadoc (),
      "com.chuusai" %% "shapeless" % "2.3.7" withJavadoc (),
      "org.postgresql" % "postgresql" % "42.2.20" withJavadoc (),
      "ch.qos.logback" % "logback-classic" % "1.2.3" withJavadoc (),
      "com.squareup.okhttp3" % "okhttp" % "4.9.1" withJavadoc (),
      "org.jsoup" % "jsoup" % "1.13.1" withJavadoc (),
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.15" % Test withJavadoc (),
      "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.0.1" withJavadoc (),
      "org.scalatest" %% "scalatest" % "3.2.9" % Test).map(_ withSources ()))
