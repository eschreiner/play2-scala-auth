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

	val linkedinPlugin = List("com.sdc.play.module.plausc.providers.oauth1.LinkedinAuthProvider")
	val partialConfiguration = Map(
	    		"play2-auth.linkedin.redirectUri.secure" -> "false",
	    		"play2-auth.linkedin.accessTokenUrl" -> "hello",
	    		"play2-auth.linkedin.authorizationUrl" -> "hello",
	    		"play2-auth.linkedin.requestTokenUrl" -> "hello")
	val fullConfiguration = Map(
	    		"play2-auth.linkedin.redirectUri.secure" -> "false",
	    		"play2-auth.linkedin.accessTokenUrl" -> "hello",
	    		"play2-auth.linkedin.authorizationUrl" -> "hello",
	    		"play2-auth.linkedin.requestTokenUrl" -> "hello",
	    		"play2-auth.linkedin.consumerKey" -> "vfoplwmwdece",
	    		"play2-auth.linkedin.consumerSecret" -> "SFKb6U0PHtpqT3ys")

	def app = FakeApplication(
	    additionalPlugins=linkedinPlugin,
	    additionalConfiguration=fullConfiguration)

	"The Linkedin provider" should {
		"complain about completely missing configuration" in {
			running(FakeApplication(additionalPlugins=linkedinPlugin)) {
			} must throwA[RuntimeException](message = "No settings for provider 'linkedin' available at all!")
		}
		"complain about partially missing configuration" in {
			running(FakeApplication(additionalPlugins=linkedinPlugin,additionalConfiguration=partialConfiguration)) {
			} must throwA[RuntimeException](message = "Provider 'linkedin' is missing needed setting\\(s\\): consumerKey, consumerSecret")
		}
		"check its configuration" in {
			running(app) {
			}
		}
	}

}