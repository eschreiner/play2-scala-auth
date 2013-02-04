package com.sdc.play.module.plausc.controllers;

import play.api.mvc._

import com.sdc.play.module.plausc.PlayAuthenticate

object Authenticate extends Controller {

    import ControllerHelpers._
    import com.sdc.play.module.plausc.user.AuthUser

	private val PAYLOAD_KEY = "p"

	def noCache(result: PlainResult) = {
		// http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
		result.withHeaders(
		        CACHE_CONTROL -> "no-cache, no-store, must-revalidate",  // HTTP 1.1
				PRAGMA -> "no-cache",  // HTTP 1.0.
				EXPIRES -> "0");  // Proxies.
	}

	def authenticate(provider: String) = Action { implicit request =>
//		noCache(response());

		val payload = getQueryString(PAYLOAD_KEY)
		PlayAuthenticate.handleAuthentication(provider, payload)
	}

	def logout() = Action { implicit request =>
//		noCache(response());

		PlayAuthenticate.logout
	}

	// TODO remove on Play 2.1
	def getQueryString(key: String)(implicit request: Request[_]) = {
		request.queryString.get(key) map {l => if (!l.isEmpty) l(0) else null } orNull
	}
}
