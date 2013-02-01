package com.sdc.play.module.plausc.providers.oauth1

import com.sdc.play.module.plausc.providers.AuthInfo
import com.sdc.play.module.plausc.user._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class OAuth1AuthInfo(token: String, secret: String) extends AuthInfo

abstract class OAuth1AuthUser(id: String, info: OAuth1AuthInfo, state: String) extends AuthUser {
    override def getId = id
}

abstract class BasicOAuth1AuthUser(id: String, info: OAuth1AuthInfo, state: String)
extends OAuth1AuthUser(id,info,state) with NameIdentity with AuthUserIdentity {

	override def toString = AuthUserHelper.toString(this)

}

import oauth.signpost.exception.OAuthException
import play._
import play.api.mvc._
import play.api.libs.oauth._
import play.mvc.Http

import com.sdc.play.module.plausc.PlayAuthenticate
import com.sdc.play.module.plausc.controllers.Authenticate
import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.providers.ext.ExternalAuthProvider

abstract class OAuth1AuthProvider[U <: AuthUserIdentity, I <: OAuth1AuthInfo](app: Application)
extends ExternalAuthProvider(app) {

	private val CACHE_TOKEN = "pa.oauth1.rtoken"

	protected def buildInfo(rtoken: RequestToken): I

	override protected def neededSettingKeys = {
		OASettingKeys.ACCESS_TOKEN_URL :: OASettingKeys.AUTHORIZATION_URL ::
		OASettingKeys.REQUEST_TOKEN_URL ::
		OASettingKeys.CONSUMER_KEY :: OASettingKeys.CONSUMER_SECRET ::
		super.neededSettingKeys
	}

	object OASettingKeys {
		val REQUEST_TOKEN_URL = "requestTokenUrl"
		val AUTHORIZATION_URL = "authorizationUrl"
		val ACCESS_TOKEN_URL  = "accessTokenUrl"
		val CONSUMER_KEY      = "consumerKey"
		val CONSUMER_SECRET   = "consumerSecret"
	}

	object Constants {
		val OAUTH_TOKEN_SECRET = "oauth_token_secret"
		val OAUTH_TOKEN        = "oauth_token"
		val OAUTH_VERIFIER     = "oauth_verifier"
	}

	def authenticate(payload: Object)(implicit request: Request[_]): Object = {

		val uri = request.uri

		if (Logger.isDebugEnabled()) {
			Logger.debug("Returned with URL: '" + uri + "'")
		}

		val c = configuration
		import OASettingKeys._

		val key = new ConsumerKey(
				c.getString(CONSUMER_KEY),
				c.getString(CONSUMER_SECRET))
		val requestTokenURL  = c.getString(REQUEST_TOKEN_URL)
		val accessTokenURL   = c.getString(ACCESS_TOKEN_URL)
		val authorizationURL = c.getString(AUTHORIZATION_URL);
		val info = new ServiceInfo(requestTokenURL, accessTokenURL, authorizationURL, key)
		val service = new OAuth(info, true)

		if (uri.contains(Constants.OAUTH_VERIFIER)) {

			val rtoken = PlayAuthenticate.removeFromCache(CACHE_TOKEN).asInstanceOf[RequestToken]
			val verifier = Authenticate.getQueryString(Constants.OAUTH_VERIFIER)
			val retrieveAccessToken = service.retrieveAccessToken(rtoken, verifier)

			retrieveAccessToken.fold(
				e => throw new AuthException(e.getLocalizedMessage()),
				s => {
					val i = buildInfo(s)
					transform(i)
				}
			)

		} else {

			val callbackURL = getRedirectUrl(request)

			val reponse = service.retrieveRequestToken(callbackURL)

			reponse.fold(
				// Exception happened
				e => throw new AuthException(e.getLocalizedMessage()),
				s => {
				// All good, we have the request token
					val token = s.token
					val redirectUrl = service.redirectUrl(token)

					PlayAuthenticate.storeInCache(CACHE_TOKEN, s)
					redirectUrl
				}
			)
		}

	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementation
	 *
	 * @param i
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected def transform(identity: I): U
}

object OAuth1Helper {

	import play.api.libs.concurrent.Promise
	import play.api.libs.json.JsValue
	import play.api.libs.oauth._
	import play.api.libs.ws.WS
	import play.libs.Json

	def promiseFor(url: String, op: OAuthCalculator) = WS.url(url).sign(op).get
	def toJson(promise: Promise[play.api.libs.ws.Response]) = Json.parse(promise.value.get.json.toString())

}