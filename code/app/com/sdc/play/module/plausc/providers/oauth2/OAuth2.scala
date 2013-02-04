package com.sdc.play.module.plausc.providers.oauth2

import com.sdc.play.module.plausc.providers.AuthInfo
import com.sdc.play.module.plausc.user._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
case class OAuth2AuthInfo(token: String, expires: Long) extends AuthInfo {

	def this(token: String) = this(token, AuthUser.NO_EXPIRATION)

}

abstract class BasicOAuth2AuthUser(id: String, info: OAuth2AuthInfo, state: String)
extends OAuth2AuthUser(id,info,state) with BasicIdentity {

	override def toString = AuthUserHelper.toString(this)

}

abstract class OAuth2AuthUser(id: String, info: OAuth2AuthInfo, state: String) extends AuthUser {

    override def getId = id
	override def getExpires = info.expires

}

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.api._
import play.api.mvc._
import play.libs.WS
import play.libs.WS.Response
import play.mvc.Http

import com.sdc.play.module.plausc.PlayAuthenticate
import com.sdc.play.module.plausc.controllers.Authenticate
import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.providers.ext.ExternalAuthProvider
import com.sdc.play.module.plausc.user.AuthUserIdentity

abstract class OAuth2AuthProvider[U <: AuthUserIdentity, I <: OAuth2AuthInfo](app: Application)
extends ExternalAuthProvider(app) {

	object OASettingKeys {
		val AUTHORIZATION_URL = "authorizationUrl"
		val ACCESS_TOKEN_URL  = "accessTokenUrl"
		val SK_CLIENT_ID      = "clientId"
		val SK_CLIENT_SECRET  = "clientSecret"
		val SK_SCOPE          = "scope"
	}

    import OASettingKeys._

	override protected def neededSettingKeys = {
		ACCESS_TOKEN_URL :: AUTHORIZATION_URL ::
		SK_CLIENT_ID :: SK_CLIENT_SECRET ::
		super.neededSettingKeys
	}

	import scala.collection.JavaConversions._
	import OAuth2Constants._

	private def getAccessTokenParams(c: Configuration, code: String)(implicit request: Request[_]) = {
		val params =
			new BasicNameValuePair(CLIENT_SECRET, c.getString(SK_CLIENT_SECRET).get) ::
			new BasicNameValuePair(GRANT_TYPE, AUTHORIZATION_CODE) ::
			new BasicNameValuePair(CODE, code) ::
			getParams(c)

		URLEncodedUtils.format(params, "UTF-8")
	}

	protected def getAccessToken(code: String)(implicit request: Request[_]): I = {
		val c = configuration.get
		val params = getAccessTokenParams(c, code)
		val url = c.getString(ACCESS_TOKEN_URL).get
		val r = WS.url(url)
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.post(params).get(PlayAuthenticate.TIMEOUT)

		buildInfo(r)
	}

	protected def buildInfo(r: Response): I

	protected def getAuthUrl(state: String)(implicit request: Request[_]): String = {

		val c = configuration.get
		val params =
			new BasicNameValuePair(SCOPE, c.getString(SK_SCOPE).get) ::
			new BasicNameValuePair(RESPONSE_TYPE, CODE) ::
			getParams(c)
		if (state != null) {
			params.add(new BasicNameValuePair(STATE, state));
		}

		val m = new HttpGet(c.getString(AUTHORIZATION_URL) + "?" + URLEncodedUtils.format(params, "UTF-8"))

		m.getURI.toString
	}

	private def getParams(c: Configuration)(implicit request: Request[_]) = {
		List(new BasicNameValuePair(CLIENT_ID, c.getString(SK_CLIENT_ID).get),
			new BasicNameValuePair(REDIRECT_URI, getRedirectUrl(request)))
	}

	override def authenticate(payload: Object)(implicit request: Request[_]): Object = {

		if (Logger.isDebugEnabled) {
			Logger.debug("Returned with URL: '" + request.uri + "'")
		}

		val error = Authenticate.getQueryString(ERROR)
		val code =  Authenticate.getQueryString(CODE)

		// Attention: facebook does *not* support state that is non-ASCII - not
		// even encoded.
		val state = Authenticate.getQueryString(STATE)

		if (error != null) {
			if (error.equals(ACCESS_DENIED)) {
				throw new AccessDeniedException(getKey)
			} else if (error.equals(REDIRECT_URI_MISMATCH)) {
				Logger.error("You must set the redirect URI for your provider to whatever you defined in your routes file."
						+ "For this provider it is: '"
						+ getRedirectUrl(request) + "'")
				throw new RedirectUriMismatch
			} else {
				throw new AuthException(error)
			}
		} else if (code != null) {
			// second step in auth process
			val info = getAccessToken(code)

			transform(info, state)
		} else {
			// no auth, yet
			val url = getAuthUrl(state)
			Logger.debug("generated redirect URL for dialog: " + url);
			return url
		}
	}

	/**
	 * This allows custom implementations to enrich an AuthUser object or
	 * provide their own implementation
	 *
	 * @param info
	 * @param state
	 * @return
	 * @throws AuthException
	 */
	protected def transform(info: I, state: String): U
}

	object OAuth2Constants {
		val CLIENT_ID     = "client_id"
		val CLIENT_SECRET = "client_secret"
		val REDIRECT_URI  = "redirect_uri"
		val SCOPE         = "scope"
		val RESPONSE_TYPE = "response_type"
		val STATE         = "state"
		val GRANT_TYPE    = "grant_type"
		val AUTHORIZATION_CODE = "authorization_code"
		val ACCESS_TOKEN  = "access_token"
		val ERROR         = "error"
		val CODE          = "code"
		val TOKEN_TYPE    = "token_type"
		val EXPIRES_IN    = "expires_in"
		val REFRESH_TOKEN = "refresh_token"
		val ACCESS_DENIED = "access_denied"
		val REDIRECT_URI_MISMATCH = "redirect_uri_mismatch"
	}

object OAuth2Helper {

}