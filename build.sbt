ThisBuild / scalaVersion     := "3.3.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "demo",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.13",
      "dev.zio" %% "zio-config" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC16",
      "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
      "dev.zio" %% "zio-test" % "2.0.13" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
