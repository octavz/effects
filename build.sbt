Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "demo",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.13",
      "dev.zio" %% "zio-config" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC16",
      "dev.zio" %% "zio-http" % "3.0.0-RC1",
      "dev.zio" %% "zio-json" % "0.5.0",
      "dev.zio" %% "zio-metrics-connectors" % "2.0.8",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.13",
      "org.slf4j" % "slf4j-simple" % "1.7.36",
      "org.postgresql" % "postgresql" % "42.5.4",
      "com.zaxxer" % "HikariCP" % "5.0.1",
      "dev.zio" %% "zio-test" % "2.0.13" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
