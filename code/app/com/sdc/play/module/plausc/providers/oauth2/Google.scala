package com.sdc.play.module.plausc.providers.oauth2

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.sdc.play.module.plausc.providers.util.JsonHelpers._

import OAuth2Constants._
import GoogleConstants._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class GoogleAuthInfo(node: JsonNode)
extends OAuth2AuthInfo(asText(node,ACCESS_TOKEN),
        java.lang.System.currentTimeMillis() + asLong(node, EXPIRES_IN) * 1000) {

	val bearer  = asText(node, TOKEN_TYPE)
	val idToken = asText(node, ID_TOKEN)

}

import java.util.Locale

import com.sdc.play.module.plausc.user._

class GoogleAuthUser(node: JsonNode, info: GoogleAuthInfo, state: String)
extends BasicOAuth2AuthUser(asText(node,ID),info,state)
with ExtendedIdentity with PicturedIdentity with ProfiledIdentity with LocaleIdentity {

	override def getEmail       = asText(node, EMAIL)
	val emailIsVerified = asBool(node, EMAIL_IS_VERIFIED)
	override def getName        = asText(node, NAME)
	override def getFirstName   = asText(node, FIRST_NAME)
	override def getLastName    = asText(node, LAST_NAME)
	override def getPicture     = asText(node, PICTURE)
	override def getGender      = asText(node, GENDER)
	val locale    = asText(node, LOCALE)
	override def getProfileLink = asText(node, LINK)

	override def getProvider    = PROVIDER_KEY

}

import play.api._
import play.libs.WS
import play.libs.WS.Response

import com.sdc.play.module.plausc._

class GoogleAuthProvider(app: Application)
extends OAuth2AuthProvider[GoogleAuthUser, GoogleAuthInfo](app) {

	def getKey() = PROVIDER_KEY

	protected def transform(info: GoogleAuthInfo, state: String): GoogleAuthUser = {

		val url = configuration.get.getString(USER_INFO_URL_SETTING_KEY).get
		val r = WS.url(url).
				setQueryParameter(ACCESS_TOKEN, info.token).
				get.get(PlayAuthenticate.TIMEOUT)

		val result = r.asJson
		if (result.get(ERROR) != null) {
			throw new AuthException(result.get(ERROR).asText)
		} else {
			Logger.debug(result.toString)
			new GoogleAuthUser(result, info, state)
		}
	}

	@Override
	protected def buildInfo(r: Response): GoogleAuthInfo = {
		val n = r.asJson
		Logger.debug(n.toString)

		if (n.get(ERROR) != null) {
			throw new AccessTokenException(n.get(ERROR).asText)
		} else {
			new GoogleAuthInfo(n)
		}
	}

}

object GoogleConstants {

	val PROVIDER_KEY = "google"

	val USER_INFO_URL_SETTING_KEY = "userInfoUrl"

    val ID_TOKEN = "id_token"

	/**
	 * From https://developers.google.com/accounts/docs/OAuth2Login#userinfocall
	 */
   	val ID                = "id"; // "00000000000000",
	val EMAIL             = "email"; // "fred.example@gmail.com",
	val EMAIL_IS_VERIFIED = "verified_email"; // true,
	val NAME              = "name"; // "Fred Example",
	val FIRST_NAME        = "given_name"; // "Fred",
	val LAST_NAME         = "family_name"; // "Example",
	val PICTURE           = "picture"; // "https://lh5.googleusercontent.com/-2Sv-4bBMLLA/AAAAAAAAAAI/AAAAAAAAABo/bEG4kI2mG0I/photo.jpg",
	val GENDER            = "gender"; // "male",
	val LOCALE            = "locale"; // "en-US"
	val LINK              = "link"; // "https://plus.google.com/107424373956322297554"

}
