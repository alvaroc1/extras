name := "extras"

organization := "com.gravitydev"

version := "0.0.4-SNAPSHOT"

scalaVersion := "2.11.6"

offline := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

resolvers ++= Seq(
  "devstack" at "https://devstack.io/repo/gravitydev/public",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

publishTo := Some("gravitydev" at "https://devstack.io/repo/gravitydev/public")

