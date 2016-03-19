name := "xyztr"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  //  "org.bitcoinj" % "bitcoinj-core" % "0.13.3",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.twitter" %% "finagle-http" % "6.34.0"
)
