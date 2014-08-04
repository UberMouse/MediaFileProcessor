name := """show-parser"""

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "com.omertron"  % "thetvdbapi"                   % "1.5",
  "org.scalatest" %% "scalatest"                   % "2.2.1" % "test",
  "org.scaldi"    %% "scaldi-akka"                 % "0.4",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.1.1" % "test"
)


