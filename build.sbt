ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val root = (project in file("."))
  .settings(
    name := "twitter",
    libraryDependencies ++= Seq(
      // Functional effects
      "org.typelevel" %% "cats-effect" % "3.3.9",

      // Dynamodb
      "io.github.d2a4u" %% "meteor-awssdk" % "1.0.8",

      // Functional effects testing
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    )
  )
  .configs(IntegrationTest)
