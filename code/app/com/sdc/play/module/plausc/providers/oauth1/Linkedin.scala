package com.sdc.play.module.plausc.providers.oauth1

import org.apache.commons.lang3.StringUtils
import org.codehaus.jackson.JsonNode

import com.sdc.play.module.plausc.user._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
case class LinkedinAuthInfo(token: String, secret: String) extends OAuth1AuthInfo(token,secret)

import com.sdc.play.module.plausc.providers.util.JsonHelpers._

class LinkedinAuthUser(nodeInfo: JsonNode, email: String, info: OAuth1AuthInfo)
extends BasicOAuth1AuthUser(asText(nodeInfo, LinkedinConstants.ID), info, null)
with BasicIdentity with FirstLastNameIdentity with PicturedIdentity
with EmploymentsIdentity with EducationsIdentity {

    override def getEmail = email

	import LinkedinConstants._
	import scala.collection.JavaConverters._

	override def getFirstName = asText(nodeInfo, FIRST_NAME)
	override def getLastName  = asText(nodeInfo, LAST_NAME)
	override def getPicture   = asText(nodeInfo, PROFILE_IMAGE_URL)
	val industry  = asText(nodeInfo, INDUSTRY)

	override def getEmployments = nodeList(POSITIONS) map {_ map {EmploymentInfo(_)}} getOrElse Nil
	override def getEducations  = nodeList(EDUCATIONS) map {_ map {EducationInfo(_)}} getOrElse Nil

	private def nodeList(expr: String) = find(nodeInfo, expr) map(_.getElements.asScala.toList)

	override def getName = getFirstName + " " + getLastName

	override def getProvider = PROVIDER_KEY

}

object LinkedinConstants {

	val PROVIDER_KEY = "linkedin"

	val ID         = "id"
	val PROFILE_IMAGE_URL = "pictureUrl"
	val FIRST_NAME = "firstName"
	val LAST_NAME  = "lastName"
	val INDUSTRY   = "industry"
	val POSITIONS  = "positions/values"
	val EDUCATIONS = "educations/values"

}

import play.api._

import com.sdc.play.module.plausc._

class LinkedinAuthProvider(app: Application)
extends OAuth1AuthProvider[LinkedinAuthUser, LinkedinAuthInfo](app) {

	private val USER_INFO_URL_SETTING_KEY  = "userInfoUrl"
	private val USER_EMAIL_URL_SETTING_KEY = "userEmailUrl"

	def getKey = LinkedinConstants.PROVIDER_KEY

	import play.api.libs.concurrent.Promise
	import play.api.libs.json.JsValue
	import play.api.libs.oauth._
	import play.api.libs.ws.WS
	import play.libs.Json

	import OAuth1Helper._
	import OASettingKeys._

	protected def transform(info: LinkedinAuthInfo): LinkedinAuthUser = {
		val c = configuration.get
		val url      = c.getString(USER_INFO_URL_SETTING_KEY).get
		val urlEmail = c.getString(USER_EMAIL_URL_SETTING_KEY).get

		val token = new RequestToken(info.token, info.secret)
		val cK = new ConsumerKey(c.getString(CONSUMER_KEY).get, c.getString(CONSUMER_SECRET).get)

		val op = new OAuthCalculator(cK, token)

		val promise      = promiseFor(url,op)
		val promiseEmail = promiseFor(urlEmail,op)

		val json      = toJson(promise)
		val jsonEmail = toJson(promiseEmail)

		new LinkedinAuthUser(json, jsonEmail.asText, info)
	}

	@Override
	protected def buildInfo(rtoken: RequestToken) = new LinkedinAuthInfo(rtoken.token, rtoken.secret)

}
