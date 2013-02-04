package com.sdc.play.module.plausc.providers.oauth2

import java.util.Map

import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.user._

import FacebookConstants._
import OAuth2Constants._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class FacebookAuthInfo(m: Map[String,String])
extends OAuth2AuthInfo(m.get(ACCESS_TOKEN),
        	java.lang.System.currentTimeMillis() + m.get(EXPIRES).toLong * 1000) {

}

import java.util.Locale

import org.codehaus.jackson.JsonNode
import com.sdc.play.module.plausc.providers.util.JsonHelpers._

class FacebookAuthUser(node: JsonNode, info: FacebookAuthInfo, state: String)
extends BasicOAuth2AuthUser(asText(node, ID), info, state)
with ExtendedIdentity with PicturedIdentity with ProfiledIdentity with LocaleIdentity {

	override def getName        = asText(node, NAME)
	override def getFirstName   = asText(node, FIRST_NAME)
	override def getLastName    = asText(node, LAST_NAME)
	override def getProfileLink = asText(node, LINK)
	val username   = asText(node, USERNAME)
	override def getGender      = asText(node, GENDER)
	override def getEmail       = asText(node, EMAIL)
	val verified   = asBool(node, VERIFIED)
	val timezone   = asInt(node,TIME_ZONE)
	val locale     = asText(node, LOCALE)
	val updateTime = asText(node, UPDATE_TIME)

	override def getProvider    = PROVIDER_KEY

	override def getPicture = {
		// According to
		// https://developers.facebook.com/docs/reference/api/#pictures
		String.format("https://graph.facebook.com/%s/picture", username)
	}
}

import java.net.URI
import java.util._

import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils

import play.api._
import play.libs.WS
import play.libs.WS.Response

import scala.collection.JavaConversions._

import com.sdc.play.module.plausc.PlayAuthenticate;

class FacebookAuthProvider(app: Application)
extends OAuth2AuthProvider[FacebookAuthUser, FacebookAuthInfo](app) {

	private val MESSAGE = "message"

	private val USER_INFO_URL_SETTING_KEY = "userInfoUrl"

	@Override
	protected def transform(info: FacebookAuthInfo, state: String): FacebookAuthUser = {

		val url = configuration.get.getString(USER_INFO_URL_SETTING_KEY).get
		val r = WS
				.url(url)
				.setQueryParameter(ACCESS_TOKEN,info.token).get()
				.get(PlayAuthenticate.TIMEOUT)

		val result = r.asJson
		if (result.get(ERROR) != null) {
			throw new AuthException(result.get(MESSAGE).asText)
		} else {
			Logger.debug(result.toString)
			return new FacebookAuthUser(result, info, state)
		}
	}

	def getKey() = PROVIDER_KEY

	protected def buildInfo(r: Response): FacebookAuthInfo = {
		if (r.getStatus >= 400) {
			throw new AccessTokenException(r.asJson().get(MESSAGE).asText)
		} else {
			val query = r.getBody
			Logger.debug(query)
			val pairs = URLEncodedUtils.parse(URI.create("/?" + query), "utf-8")
			if (pairs.size < 2) {
				throw new AccessTokenException()
			}
			val m = new HashMap[String, String](pairs.size)
			for (nameValuePair <- pairs) {
				m.put(nameValuePair.getName(), nameValuePair.getValue())
			}

			new FacebookAuthInfo(m)
		}
	}

}

object FacebookConstants {
		val PROVIDER_KEY = "facebook";
		val EXPIRES = "expires";

		val ID = "id"; // "616473731"
		val NAME = "name"; // "Joscha Feth"
		val FIRST_NAME = "first_name";// "Joscha"
		val LAST_NAME = "last_name"; // "Feth"
		val LINK = "link"; // "http://www.facebook.com/joscha.feth"
		val USERNAME = "username";// "joscha.feth"
		val GENDER = "gender";// "male"
		val EMAIL = "email";// "joscha@feth.com"
		val TIME_ZONE = "timezone";// 2
		val LOCALE = "locale";// "de_DE"
		val VERIFIED = "verified";// true
		val UPDATE_TIME = "updated_time"; // "2012-04-26T20:22:52+0000"}
	}
