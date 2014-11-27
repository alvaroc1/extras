name := "extras"

organization := "com.gravitydev"

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.4"

offline := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.4"
)

resolvers ++= Seq(
  "devstack" at "https://devstack.io/repo/gravitydev/public",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

publishTo := Some("gravitydev" at "https://devstack.io/repo/gravitydev/public")

