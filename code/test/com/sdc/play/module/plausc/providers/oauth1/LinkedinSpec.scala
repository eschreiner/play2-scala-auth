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

	import java.io.File

	import play.api.test._
	import play.api.test.Helpers._

	val linkedinPlugin = List("com.sdc.play.module.plausc.providers.oauth1.LinkedinAuthProvider")
	val partialConfiguration = Map(
	    		"play2-auth.linkedin.redirectUri.secure" -> "false",
	    		"play2-auth.linkedin.accessTokenUrl" -> "https://api.linkedin.com/uas/oauth/accessToken",
	    		"play2-auth.linkedin.authorizationUrl" -> "https://api.linkedin.com/uas/oauth/authenticate",
	    		"play2-auth.linkedin.requestTokenUrl" -> "https://api.linkedin.com/uas/oauth/requestToken?scope=r_fullprofile+r_emailaddress")
	val fullConfiguration = partialConfiguration +
				("play2-auth.linkedin.consumerKey" -> "vfoplwmwdece") +
	    		("play2-auth.linkedin.consumerSecret" -> "SFKb6U0PHtpqT3ys")
	val extendedConfiguration = fullConfiguration +
				("play2-auth.linkedin.userInfoUrl" -> "http://api.linkedin.com/v1/people/~:(id,picture-url,first-name,last-name,industry,positions,educations)?format=json") +
				("play2-auth.linkedin.userEmailUrl" -> "http://api.linkedin.com/v1/people/~/email-address?format=json")

	"The Linkedin provider" should {
		"complain about completely missing configuration" in {
			running(FakeApplication(additionalPlugins=linkedinPlugin)) {
			} must throwA[RuntimeException](message = "No settings for provider 'linkedin' available at all!")
		}
		"read default configuration from file test/conf/application.conf" in {
			running(FakeApplication(new File("test"),additionalPlugins=linkedinPlugin)) {
			} must throwA[RuntimeException](message = "Provider 'linkedin' is missing needed setting\\(s\\): consumerKey, consumerSecret")
		}
		"complain about partially missing configuration" in {
			running(FakeApplication(additionalPlugins=linkedinPlugin,additionalConfiguration=partialConfiguration)) {
			} must throwA[RuntimeException](message = "Provider 'linkedin' is missing needed setting\\(s\\): consumerKey, consumerSecret")
		}
		"check its configuration" in {
			running(FakeApplication(additionalPlugins=linkedinPlugin,additionalConfiguration=fullConfiguration)) {
			}
		}
	}

}