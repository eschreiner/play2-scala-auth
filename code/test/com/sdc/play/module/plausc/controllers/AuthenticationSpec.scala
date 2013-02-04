package com.sdc.play.module.plausc.controllers

import java.io.File

import play.api.mvc.Results._

import play.test.Helpers.fakeRequest
import play.api._
import play.api.i18n.Messages

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mutable._

import com.sdc.play.module.plausc.providers.password.UsernamePasswordConstants

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class AuthenticationSpec extends Specification {

    def app0 = FakeApplication(additionalConfiguration=Map("smtp.mock"->"true"))

    "The Authenticate helper" should {
    	"reads the configuration as I want" in {
    		running(app0) {
    		    app0.configuration.getBoolean("smtp.mock") === Some(true)
    		}
        }
    	"give 404 when no provider is configured" in {
    		running(app0) {
    			implicit val request = fakeRequest.getWrappedRequest
    			val result = Authenticate.authenticate(UsernamePasswordConstants.PROVIDER_KEY)(request)
    			status(result) mustEqual status(NotFound)
//    			println("content: "+ contentAsString(result))
    		}
        }
    }

}