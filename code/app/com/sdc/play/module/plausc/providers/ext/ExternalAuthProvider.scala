package com.sdc.play.module.plausc.providers.ext

import play.api.Application
import play.api.mvc._

import com.sdc.play.module.plausc.PlayAuthenticate
import com.sdc.play.module.plausc.providers.AuthProvider

abstract class ExternalAuthProvider(app: Application) extends AuthProvider(app) {

	object SettingKeys {
		val REDIRECT_URI_HOST   = "redirectUri.host"
		val REDIRECT_URI_SECURE = "redirectUri.secure"
	}

	import SettingKeys._

	private def useSecureRedirectUri: Boolean = {
		configuration flatMap { _.getBoolean(REDIRECT_URI_SECURE) } getOrElse false
	}

	protected def getRedirectUrl(implicit request: Request[_]): String = {
		val isHttps = useSecureRedirectUri
		val call = PlayAuthenticate.resolver.auth(getKey)

	    configuration flatMap { _.getString(REDIRECT_URI_HOST) } filter { ! _.trim.isEmpty } map {
	        "http" + (if (isHttps) "s" else "") + "://" + _ + call.url
	    } getOrElse
	    	call.absoluteURL(isHttps)
	}

	def isExternal = true

}
