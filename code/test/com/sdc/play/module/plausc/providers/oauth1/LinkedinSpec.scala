package com.sdc.play.module.plausc.providers.oauth1

import org.specs2.mutable._

/**
 * @author  Dr. Erich W. Schreiner - 8SPE - Rohde&amp;Schwarz
 * @version 5.6.1.0
 * @since   5.6.1.0
 * @history
 *          5.6.1.0 # Feb 4, 2013 # created
 */
class LinkedinSpec extends Specification {

	import play.api.test._
	import play.api.test.Helpers._

	def app = FakeApplication(
	    additionalPlugins=List("com.sdc.play.module.plausc.providers.oauth1.LinkedinAuthProvider"),
	    additionalConfiguration=Map(
	    		"play-authenticate.linkedin.redirectUri.secure" -> "false",
	        "play-authenticate.linkedin.accessTokenUrl" -> "hello",
	        "play-authenticate.linkedin.authorizationUrl" -> "hello",
	        "play-authenticate.linkedin.requestTokenUrl" -> "hello",
	        "play-authenticate.linkedin.consumerKey" -> "vfoplwmwdece",
	        "play-authenticate.linkedin.consumerSecret" -> "SFKb6U0PHtpqT3ys"))

	"The Linkedin provider" should {
		"parse JSon correctly" in {
			running(app) {
				val data = """ ... """
			}
		}
	}

}