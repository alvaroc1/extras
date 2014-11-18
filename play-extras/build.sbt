name := "play-extras"

organization := "com.gravitydev"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

offline := true

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.6",
  "joda-time" % "joda-time" % "2.5"
)

resolvers ++= Seq(
  "devstack" at "https://devstack.io/repo/gravitydev/public",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

publishTo := Some("gravitydev" at "https://devstack.io/repo/gravitydev/public")

