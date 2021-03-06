package com.sdc.play.module.plausc.controllers

import java.io.File

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.i18n.Messages

import play.api.test._
import play.api.test.Helpers._
import org.specs2.mutable._

import com.sdc.play.module.plausc.providers.password.UsernamePasswordConstants
import com.sdc.play.module.plausc.providers.oauth1.LinkedinConstants
import com.sdc.play.module.plausc.PlayAuthenticate

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class AuthenticationSpec extends Specification {

	val linkedinPlugin = List("com.sdc.play.module.plausc.providers.oauth1.LinkedinAuthProvider")

	def app0 = FakeApplication(additionalConfiguration=Map("smtp.mock"->"true"))
	val linkedin = Map(("play2-auth.linkedin.consumerKey" -> "vfoplwmwdece"),
	    		("play2-auth.linkedin.consumerSecret" -> "SFKb6U0PHtpqT3ys"))

	import com.sdc.play.module.plausc.DummyResolver

    "The Authenticate helper" should {
    	"reads the configuration as I want" in {
    		running(app0) {
    		    app0.configuration.getBoolean("smtp.mock") === Some(true)
    		}
        }
    	"give 404 when no provider is configured" in {
    		running(app0) {
    			val request = FakeRequest()
    			val result = Authenticate.authenticate(UsernamePasswordConstants.PROVIDER_KEY)(request)
    			status(result) mustEqual status(NotFound)
//    			println("content: "+ contentAsString(result))
    		}
        }
    	"redirects to Linkedin" in {
    		running(FakeApplication(additionalPlugins=linkedinPlugin,additionalConfiguration=linkedin)) {
    		    PlayAuthenticate.resolver = new DummyResolver
    			val request = FakeRequest()
    			val result = Authenticate.authenticate(LinkedinConstants.PROVIDER_KEY)(request)
    			println(result)
    			status(result) mustEqual status(SeeOther(""))
    		}
    	}
    }

}