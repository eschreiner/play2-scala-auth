package com.sdc.play.module.plausc.providers.oauth2

import org.codehaus.jackson.JsonNode

import com.sdc.play.module.plausc.user._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class FoursquareAuthInfo(token: String) extends OAuth2AuthInfo(token)

import FoursquareConstants._
import OAuth2Constants._

import scala.collection.JavaConverters._

import com.sdc.play.module.plausc.providers.util.JsonHelpers._

class FoursquareAuthUser(node: JsonNode, info: OAuth2AuthInfo, state: String)
extends BasicOAuth2AuthUser(asText(node,ID),info,state)
with ExtendedIdentity with PicturedIdentity {

	override def getFirstName = asText(node, FIRST_NAME)
	override def getLastName  = asText(node, LAST_NAME)
	val homeCity  = asText(node, HOME_CITY)

	override def getPicture = if (node.has(PHOTO)) {
		val sb = new StringBuilder
		sb.append(node.get(PHOTO).get(PREFIX).asText)
		sb.append(ORIGINAL)
		sb.append(node.get(PHOTO).get(SUFFIX).asText)
		sb.toString
	} else null

	def getGender = asText(node, GENDER)
	val mtype  = asText(node, TYPE)
	val bio    = asText(node, BIO)

	val contactNode = node.get(CONTACT)
	val contact = if (contactNode != null)
		contactNode.getFields.asScala map {e => (e.getKey, e.getValue.asText)} toMap
	else Map[String,String]()

	override def getProvider = PROVIDER_KEY

	/**
	 * It is not guaranteed that an email is present for foursquare
	 */
	override def getEmail = getContactDetail(CONTACT_DETAIL_EMAIL)

	def getContactDetail(key: String) = contact.get(key) orNull

	override def getName: String = {
		val sb = new StringBuilder
		val hasFirstName = getFirstName != null && !getFirstName.isEmpty
		val hasLastName = getLastName != null && !getLastName.isEmpty
		if (hasFirstName) {
			sb.append(getFirstName)
			if (hasLastName) sb.append(" ")
		}
		if (hasLastName) {
			sb.append(getLastName)
		}

		sb.toString
	}
}

import play.api._
import play.libs.WS
import play.libs.WS.Response

import com.sdc.play.module.plausc._

class FoursquareAuthProvider(app: Application)
extends OAuth2AuthProvider[FoursquareAuthUser, FoursquareAuthInfo](app) {

	override protected def buildInfo(r: Response): FoursquareAuthInfo = {

		if (r.getStatus >= 400) {
			throw new AccessTokenException(r.toString)
		} else {
			val result = r.asJson
			Logger.debug(result.asText)
			new FoursquareAuthInfo(result.get(ACCESS_TOKEN).asText)
		}
	}

	override protected def transform(info: FoursquareAuthInfo, state: String): FoursquareAuthUser = {

		val url = configuration.get.getString(USER_INFO_URL_SETTING_KEY).get
		val r = WS.url(url).
				setQueryParameter(OAUTH_TOKEN, info.token).
				setQueryParameter("v", VERSION).
				get.get(PlayAuthenticate.TIMEOUT)

		val result = r.asJson
		if (r.getStatus >= 400) {
			throw new AuthException(result.get("meta").get("errorDetail").asText)
		} else {
			Logger.debug(result.toString)
			new FoursquareAuthUser(result.get("response").get("user"), info, state)
		}
	}

	override def getKey = PROVIDER_KEY

}

object FoursquareConstants {

	val PROVIDER_KEY = "foursquare"

	val USER_INFO_URL_SETTING_KEY = "userInfoUrl"
	val OAUTH_TOKEN               = "oauth_token"
	val VERSION                   = "20120617"

	val CONTACT_DETAIL_EMAIL    = "email"
	val CONTACT_DETAIL_TWITTER  = "contact"
	val CONTACT_DETAIL_FACEBOOK = "contact"

	/**
	 * From:
	 * https://developer.foursquare.com/docs/responses/user
	 */
		val ID         = "id" // "1188384"
		val FIRST_NAME = "firstName" // "Joscha"
		val LAST_NAME  = "lastName" // "Feth"
		val HOME_CITY  = "homeCity" // "Metzingen, Baden-Württemberg"
		val PHOTO      = "photo" // "<prefix>/original/<suffix>"
		val PREFIX     = "prefix" // "https://is0.4sqi.net/img/user/"
		val ORIGINAL   = "original" // "original"
		val SUFFIX     = "suffix" // "/HZGTZQNRLA21ZIAD.jpg"
		val GENDER     = "gender" // "male"
		val TYPE       = "type" // "user"
		val CONTACT    = "contact" // {"email":
								// "joscha@feth.com",
								// "twitter":
								// "joschafeth",
								// "facebook":
								// "616473731"}
		val BIO        = "bio" // "lalala"

}