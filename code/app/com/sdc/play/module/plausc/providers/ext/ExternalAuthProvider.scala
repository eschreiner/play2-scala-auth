package com.sdc.play.module.plausc.providers.ext

import play.Application
import play.api.mvc._

import com.sdc.play.module.plausc.PlayAuthenticate
import com.sdc.play.module.plausc.providers.AuthProvider

abstract class ExternalAuthProvider(app: Application) extends AuthProvider(app) {

	object SettingKeys {
		@Deprecated
		val SECURE_REDIRECT_URI = "secureRedirectUri"
		val REDIRECT_URI_HOST   = "redirectUri.host"
		val REDIRECT_URI_SECURE = "redirectUri.secure"
	}

	import SettingKeys._

	private def useSecureRedirectUri: Boolean = {
		val secure = configuration.getBoolean(REDIRECT_URI_SECURE)
		if (secure == null)
			configuration.getBoolean(SECURE_REDIRECT_URI)
		else secure
	}

	protected def getRedirectUrl(implicit request: Request[_]): String = {
		val overrideHost = configuration.getString(REDIRECT_URI_HOST)
		val isHttps = useSecureRedirectUri
		val c = PlayAuthenticate.resolver.auth(getKey)
		if (overrideHost != null && !overrideHost.trim.isEmpty)
			"http" + (if (isHttps) "s" else "") + "://" + overrideHost + c.url
		else
			c.absoluteURL(isHttps)
	}

	def isExternal = true

}
