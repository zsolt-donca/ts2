name := "ts2"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"

libraryDependencies += "org.atnos" %% "eff" % "2.3.0"
libraryDependencies += "org.atnos" %% "eff-monix" % "2.3.0"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.1"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1"

libraryDependencies += "com.github.melrief" %% "pureconfig" % "0.5.1"

libraryDependencies += "com.github.fommil" %% "spray-json-shapeless" % "1.3.0"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"


libraryDependencies += "com.github.pathikrit" %% "better-files" % "2.17.1"
libraryDependencies += "com.github.pathikrit" %% "better-files-akka" % "2.17.1"
libraryDependencies += "commons-io" % "commons-io" % "2.5"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"


addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

scalacOptions += "-Ypartial-unification"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
