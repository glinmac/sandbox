name := "cmusphinx"

version := "1.0"

scalaVersion := "2.11.11"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.6.0"

libraryDependencies += "edu.cmu.sphinx" % "sphinx4-core" % "5prealpha-SNAPSHOT"
libraryDependencies += "edu.cmu.sphinx" % "sphinx4-data" % "5prealpha-SNAPSHOT"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}