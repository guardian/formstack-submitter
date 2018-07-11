lazy val root = (project in file("."))
  .settings(
    name         := "formstack-submitter",
    organization := "com.gu",
    scalaVersion := "2.12.6",
    scalacOptions ++= scalacOpts,
    libraryDependencies ++= Dependencies.circe ++ Dependencies.aws ++ Dependencies.http4s ++ Dependencies.log4j ++ Dependencies.testing,
    Universal / topLevelDirectory := None,
    Universal / packageName       := normalizedName.value,
    riffRaffPackageType           := (Universal / packageBin).value,
    riffRaffUploadArtifactBucket  := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket  := Option("riffraff-builds"),
    riffRaffManifestProjectName   := s"Content Platforms::${name.value}"
  )
  .enablePlugins(RiffRaffArtifact, JavaAppPackaging)

val scalacOpts = Seq(
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-Ywarn-unused:imports",
  "-language:existentials",
  "-language:higherKinds",
  "-encoding",
  "utf-8",
  "-deprecation",
  "-feature"
)
