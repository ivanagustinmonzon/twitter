ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "twitter",
    libraryDependencies ++= Seq(
      // Functional effects
      "org.typelevel" %% "cats-effect" % "3.3.9",

      // Dynamodb
      "io.github.d2a4u" %% "meteor-awssdk" % "1.0.8",
      "io.github.d2a4u" %% "meteor-dynosaur" % "1.0.12",
      "org.systemfw" %% "dynosaur-core" % "0.3.0",

      // Functional effects testing
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % "test, it", // (Test, IntegrationTest)
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    )
  )

parallelExecution in IntegrationTest := false
