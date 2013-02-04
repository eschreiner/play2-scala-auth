package com.sdc.play.module.plausc.providers.openid

import play.api._
import play.libs.F.Promise
import play.libs.OpenID
import play.libs.OpenID.UserInfo
import play.api.mvc._

import com.sdc.play.module.plausc.controllers.Authenticate
import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.providers.ext.ExternalAuthProvider
import com.sdc.play.module.plausc.user._

import OpenIdConstants._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class OpenIdAuthUser(id: String, info: UserInfo) extends AuthUser {

    override def getId = id

	val attributes = info.attributes

	def getAttribute(attribute: String) = {
		if (attributes != null)
			attributes.get(attribute)
		else
			null
	}

	override def getProvider = PROVIDER_KEY

}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class OpenIdAuthProvider(app: Application) extends ExternalAuthProvider(app) {

	def getKey() = PROVIDER_KEY

	def authenticate(payload:  Object)(implicit request: Request[_]): Object = {

		if (Logger.isDebugEnabled) {
			Logger.debug("Returned with URL: '" + request.uri + "'")
		}

		val hasOpenID = payload != null && !payload.toString.trim.isEmpty
		var hasInfo: Boolean = false
		var u: UserInfo = null
		try {
			val pu = OpenID.verifiedId
			u = pu.get
			hasInfo = true
		} catch {
		    case t: Throwable =>
			if (t.isInstanceOf[play.api.libs.openid.Errors$BAD_RESPONSE$]) {
				if (!hasOpenID) {
					throw new NoOpenIdAuthException(
							"OpenID endpoint is required");
				} else {
					// ignore, its the start of the OpenID dance
				}
			} else if (t.isInstanceOf[play.api.libs.openid.Errors$BAD_RESPONSE$]) {
				throw new AuthException("Bad response from OpenID provider");
			} else {
				throw new AuthException(t.getMessage());
			}
		}
		if (hasInfo) {

			// TODO: Switch to passing the UserInfo only, when the fix for:
			// https://play.lighthouseapp.com/projects/82401-play-20/tickets/578-202-java-openid-userinfo-id-always-null
			// has been incorporated.
			return new OpenIdAuthUser(Authenticate.getQueryString("openid.claimed_id"), u);

		} else if (hasOpenID) {
			val required = getAttributes(ATTRIBUTES_REQUIRED)
			val optional = getAttributes(ATTRIBUTES_OPTIONAL)

			try {
				val pr = OpenID.redirectURL(
						payload.toString, getRedirectUrl,
						required, optional);

				return pr.get
			} catch {
			    case t: Throwable =>
				if (t.isInstanceOf[java.net.ConnectException]) {
					throw new OpenIdConnectException(t.getMessage)
				} else {
					throw new AuthException(t.getMessage)
				}
			}
		} else {
			// this must never happen
			throw new AuthException
		}
	}

	private def getAttributes(subKey: String): Map[String,String] = {
		val attributes = configuration.get.getConfig(ATTRIBUTES + "." + subKey).get
		if (attributes != null) {
			val keys = attributes.keys.iterator
			keys map {key => (key,attributes.getString(key).get)} toMap
		} else null
	}

}

object OpenIdConstants {

	val PROVIDER_KEY = "openid"

	val ATTRIBUTES          = "attributes"
	val ATTRIBUTES_REQUIRED = "required"
	val ATTRIBUTES_OPTIONAL = "optional"

}