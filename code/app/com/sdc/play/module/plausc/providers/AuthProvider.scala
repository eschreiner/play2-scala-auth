package com.sdc.play.module.plausc.providers

import play.api._
import play.api.mvc._
import play.mvc.Http

import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.user._

import java.io.Serializable

trait AuthInfo extends Serializable

abstract class AuthProvider(application: Application) extends Plugin {

	override def onStart = {

		val c = configuration
		if (c.isEmpty) throw new RuntimeException("No settings for provider '"+ getKey +"' available at all!")

		for (key <- neededSettingKeys) {
			val setting = c.get.getString(key)
			if (setting.isEmpty || "".equals(setting.get)) {
				throw new RuntimeException("Provider '" + getKey
						+ "' missing needed setting '" + key + "'")
			}
		}

		Registry.register(getKey, this)
	}

	override def onStop = Registry.unregister(getKey)

	def getUrl = PlayAuthenticate.resolver.auth(getKey).url

	protected def getAbsoluteUrl(implicit request: Request[_]): String = {
		PlayAuthenticate.resolver.auth(getKey).absoluteURL(false)
	}

	def getKey: String

	protected def configuration = PlayAuthenticate.configuration.getConfig(getKey)

	/**
	 * Returns either an AuthUser object or a String (URL)
	 *
	 * @param context
	 * @param payload
	 *            Some arbitrary payload that shall get passed into the
	 *            authentication process
	 * @return
	 * @throws AuthException
	 */
	def authenticate(payload: Object)(implicit request: Request[_]): Object

	protected def neededSettingKeys: List[String] = Nil

	def getSessionAuthUser(id: String, expires: Long): AuthUser = new SessionAuthUser(getKey, id, expires)

	def isExternal: Boolean

}


object Registry {

	private val providers = scala.collection.mutable.Map[String, AuthProvider]()

	def register(provider: String, p: AuthProvider): Unit = {
		val previous = providers.put(provider, p)
		if (previous != null) {
			Logger.warn("There are multiple AuthProviders registered for key '"+ provider +"'")
		}
	}
	def unregister(provider: String): Unit = providers.remove(provider)
	def get(provider: String): Option[AuthProvider] = providers.get(provider)
	def getProviders: Iterable[AuthProvider] = providers.values
	def hasProvider(provider: String): Boolean = providers.contains(provider)
}

