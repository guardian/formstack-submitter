lazy val root = (project in file("."))
  .settings(
    name          := "formstack-submitter",
    organization  := "com.gu",
    scalaVersion  := "2.12.6",
    scalacOptions ++= scalacOpts,
    libraryDependencies ++= Dependencies.circe ++ Dependencies.aws ++ Dependencies.http4s,
    Universal / topLevelDirectory := None,
    Universal / packageName       := normalizedName.value,
    riffRaffPackageType           := (Universal / dist).value,
    riffRaffUploadArtifactBucket  := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket  := Option("riffraff-builds"),
    riffRaffManifestProjectName   := s"Off-platform::${name.value}",
    riffRaffArtifactResources     += (file("cloudformation.yaml"), s"${name.value}-cfn/cfn.yaml")
  )
  .enablePlugins(RiffRaffArtifact, JavaAppPackaging)

val scalacOpts = Seq(
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-language:existentials",
  "-language:higherKinds",
  "-encoding", "utf-8",
  "-deprecation",
  "-feature"
)