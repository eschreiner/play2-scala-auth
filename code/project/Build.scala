import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-auth"
    val appVersion      = "0.1"

    val appDependencies = Seq(
	  "org.scalatest" %% "scalatest" % "1.8" % "test",
	  "org.specs2" %% "specs2" % "1.12.3" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(

		organization := "com.sdc",

      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",

//      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/")),
//      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/")),

      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2.1",
//      libraryDependencies += "com.feth" %% "play-easymail" % "0.1-SNAPSHOT",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
    )

}
