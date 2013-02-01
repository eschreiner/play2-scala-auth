import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-auth"
    val appVersion      = "0.1"

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",

      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),

      libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.2.1",
      libraryDependencies += "com.feth" %% "play-easymail" % "0.1-SNAPSHOT",
      libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m",
      libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
    )

}
