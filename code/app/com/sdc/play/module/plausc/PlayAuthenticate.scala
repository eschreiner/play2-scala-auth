package com.sdc.play.module.plausc

import play.api._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.mvc.Results._

import providers.{AuthProvider,Registry}
import service.UserService
import user.AuthUser

import scala.reflect.BeanProperty

object PlayAuthenticate {

	private val SETTING_KEY_PLAY_AUTHENTICATE     = "play2-auth"
	private val SETTING_KEY_AFTER_AUTH_FALLBACK   = "afterAuthFallback"
	private val SETTING_KEY_AFTER_LOGOUT_FALLBACK = "afterLogoutFallback"
	private val SETTING_KEY_ACCOUNT_MERGE_ENABLED = "accountMergeEnabled"
	private val SETTING_KEY_ACCOUNT_AUTO_LINK     = "accountAutoLink"
	private val SETTING_KEY_ACCOUNT_AUTO_MERGE    = "accountAutoMerge"

	var resolver: Resolver = null
	var userService: UserService = null

	def getUserService: UserService = {
		if (userService == null) {
			throw new RuntimeException(Messages("playauthenticate.core.exception.no_user_service"))
		}
		userService
	}

	private val ORIGINAL_URL   = "pa.url.orig"
	private val USER_KEY       = "pa.u.id"
	private val PROVIDER_KEY   = "pa.p.id"
	private val EXPIRES_KEY    = "pa.u.exp"
	private val SESSION_ID_KEY = "pa.s.id"

	def configuration = Play.maybeApplication flatMap { _.configuration.getConfig(SETTING_KEY_PLAY_AUTHENTICATE) }

	val TIMEOUT = 10l * 1000
	private val MERGE_USER_KEY: String = null
	private val LINK_USER_KEY: String = null

	def originalUrl(implicit request: Request[_]): String = request.session(ORIGINAL_URL)

	def storeOriginalUrl(session: Session)(implicit request: Request[_]): (Session,String) = {
		val loginUrl = if (resolver.login != null) {
			resolver.login.url
		} else {
			Logger.warn("You should define a login call in the resolver")
			null
		}

		if (request.method == "GET"
				&& request.path != loginUrl) {
			Logger.debug("Path where we are coming from ("
					+ request.uri
					+ ") is different than the login URL (" + loginUrl + ")")
			(session + (ORIGINAL_URL -> request.uri),request.uri)
		} else {
			Logger.debug("The path we are coming from is the Login URL - delete jumpback")
			(session - ORIGINAL_URL,null)
		}
	}

	def storeUser(authUser: AuthUser)(implicit request: Request[_]): Session = {

		// User logged in once more - wanna make some updates?
		val u = getUserService.update(authUser)

		if (u.getExpires != AuthUser.NO_EXPIRATION) {
			request.session + (EXPIRES_KEY, u.getExpires.toString)
		} else {
			request.session - EXPIRES_KEY
		} + (USER_KEY -> u.getId) + (PROVIDER_KEY -> u.getProvider)
	}

	def isLoggedIn(implicit request: Request[_]): Boolean = {
		var ret = request.session.get(USER_KEY).isDefined &&  // user is set
				  request.session.get(PROVIDER_KEY).isDefined // provider is set
		ret &= Registry.hasProvider(request.session(PROVIDER_KEY)) // this provider is active
		if (request.session.get(EXPIRES_KEY).isDefined) {
			// expiration is set
			val expires = getExpiration
			if (expires != AuthUser.NO_EXPIRATION) {
				ret &= (System.currentTimeMillis < expires) // and the session expires after now
			}
		}
		ret
	}

	def logout(implicit request: Request[_]) = {
		Redirect(getUrl(resolver.afterLogout, SETTING_KEY_AFTER_LOGOUT_FALLBACK)).withSession(
		        request.session - USER_KEY - PROVIDER_KEY - EXPIRES_KEY -
		        // shouldn't be in any more, but just in case lets kill it from the cookie
		        ORIGINAL_URL)
	}

	def peekOriginalUrl(implicit request: Request[_]): String = request.session(ORIGINAL_URL)

	def hasUserService = userService != null

	def getExpiration(implicit request: Request[_]): Long = {
	    Option(request.session(EXPIRES_KEY)).map { exp =>
			try {
				exp.toLong
			} catch {
			    case nfe: NumberFormatException => AuthUser.NO_EXPIRATION
			}
		} getOrElse AuthUser.NO_EXPIRATION
	}

	def getUser(implicit request: Request[_]): Option[AuthUser] = {
	    for {
	        provider <- request.session.get(PROVIDER_KEY)
	        id <- request.session.get(USER_KEY)
	    } yield {
	    	val expires = getExpiration
			getProvider(provider) map { _.getSessionAuthUser(id, expires) } orNull
		}
	}

	def isAccountAutoMerge:    Boolean = configuration.get.getBoolean(SETTING_KEY_ACCOUNT_AUTO_MERGE).get
	def isAccountAutoLink:     Boolean = configuration.get.getBoolean(SETTING_KEY_ACCOUNT_AUTO_LINK).get
	def isAccountMergeEnabled: Boolean = configuration.get.getBoolean(SETTING_KEY_ACCOUNT_MERGE_ENABLED).get

	private def getPlayAuthSessionId(implicit request: Request[_]): (Session,String) = {
		// Generate a unique id
		request.session.get(SESSION_ID_KEY) map { id =>
		    (request.session,id)
		} getOrElse {
			val uuid = java.util.UUID.randomUUID.toString
			(request.session + (SESSION_ID_KEY, uuid),uuid)
		}
	}

	private def storeUserInCache(key: String, identity: AuthUser)(implicit request: Request[_]) = {
		storeInCache(key, identity)
	}

	def storeInCache(key: String, o: Object)(implicit request: Request[_]) = {
		play.cache.Cache.set(getCacheKey(key), o)
	}

	def removeFromCache(key: String)(implicit request: Request[_]): Object = {
		val o = getFromCache(key)

		val k = getCacheKey(key)
		// TODO change on Play 2.1
		play.cache.Cache.set(k, null, 0)

		// POST-2.0/
		// play.cache.Cache.remove(k)
		o
	}

	private def getCacheKey(key: String)(implicit request: Request[_]) = {
		val id = getPlayAuthSessionId
		id + "_" + key
	}

	def getFromCache(key: String)(implicit request: Request[_]) = {
		play.cache.Cache.get(getCacheKey(key))
	}

	private def getUserFromCache(key: String)(implicit request: Request[_]): AuthUser = {
		val o = getFromCache(key)
		if (o != null && o.isInstanceOf[AuthUser]) {
			o.asInstanceOf[AuthUser]
		} else {
			null
		}
	}

	def storeMergeUser(identity: AuthUser)(implicit request: Request[_]) = {
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
		storeUserInCache(MERGE_USER_KEY, identity)
	}

	def getMergeUser(implicit request: Request[_]): AuthUser = {
		getUserFromCache(MERGE_USER_KEY)
	}

	def removeMergeUser(implicit request: Request[_]) = {
		removeFromCache(MERGE_USER_KEY)
	}

	def storeLinkUser(identity: AuthUser)(implicit request: Request[_]) = {
		// TODO the cache is not ideal for this, because it might get cleared
		// any time
		storeUserInCache(LINK_USER_KEY, identity)
	}

	def getLinkUser(implicit request: Request[_]): AuthUser = {
		getUserFromCache(LINK_USER_KEY)
	}

	def removeLinkUser(implicit request: Request[_]) = {
		removeFromCache(LINK_USER_KEY)
	}

	private def getJumpUrl(implicit request: Request[_]): String = {
		val originalUrl2 = originalUrl
		if (originalUrl2 != null) {
			originalUrl2
		} else {
			getUrl(resolver.afterAuth, SETTING_KEY_AFTER_AUTH_FALLBACK)
		}
	}

	private def getUrl(c: Call, settingFallback: String): String = {
		// this can be null if the user did not correctly define the
		// resolver
		if (c != null) {
			c.url
		} else {
			// go to root instead, but log this
			Logger.warn("Resolver did not contain information about where to go - redirecting to /")
			val afterAuthFallback = configuration.get.getString(settingFallback).get
			if (afterAuthFallback != null && afterAuthFallback != "") {
				afterAuthFallback
			} else {
				// Not even the config setting was there or valid...meh
				Logger.error("Config setting '" + settingFallback
						+ "' was not present!")
				"/"
			}
		}
	}

	def link(link: Boolean)(implicit request: Request[_]): Result = {
		val linkUser = getLinkUser

		if (linkUser == null) {
			return Forbidden("")
		}

		var loginUser: AuthUser = null
		if (link) {
			// User accepted link - add account to existing local user
			loginUser = getUserService.link(getUser.get, linkUser)
		} else {
			// User declined link - create new user
			try {
				loginUser = signupUser(linkUser)
			} catch {
			    case e: AuthException => InternalServerError(e.getMessage)
			}
		}
		removeLinkUser
		loginAndRedirect(loginUser)
	}

	def loginAndRedirect(loginUser: AuthUser)(implicit request: Request[_]): Result = {
		storeUser(loginUser)
		Redirect(getJumpUrl)
	}

	def merge(merge: Boolean)(implicit request: Request[_]): Result = {
		val mergeUser = getMergeUser

		if (mergeUser == null) {
			Forbidden("")
		} else {
			var loginUser = if (merge) {
				// User accepted merge, so do it
				getUserService.merge(mergeUser, getUser.get)
			} else {
				// User declined merge, so log out the old user, and log out with
				// the new one
				mergeUser
			}
			removeMergeUser
			loginAndRedirect(loginUser)
		}
	}

	private def signupUser(u: AuthUser): AuthUser = {
		val id = getUserService.save(u)
		if (id == null) {
			throw new AuthException(Messages("playauthenticate.core.exception.signupuser_failed"))
		}
		u
	}

	def handleAuthentication(provider: String, payload: Object)(implicit request: Request[_]): Result = {
		val ap = getProvider(provider)
		if (ap.isEmpty) {
			// Provider wasn't found and/or user was fooling with our stuff -
			// tell him off:
			return NotFound(Messages("play2_auth.error.provider_not_found", provider))
		}
		try {
			val o = ap.get.authenticate(payload)
			if (o.isInstanceOf[String]) {
				Redirect(o.asInstanceOf[String])
			} else if (o.isInstanceOf[AuthUser]) {

				val newUser = o.asInstanceOf[AuthUser]
				val session = request.session

				// We might want to do merging here:
				// Adapted from:
				// http://stackoverflow.com/questions/6666267/architecture-for-merging-multiple-user-accounts-together
				// 1. The account is linked to a local account and no session
				// cookie is present --> Login
				// 2. The account is linked to a local account and a session
				// cookie is present --> Merge
				// 3. The account is not linked to a local account and no
				// session cookie is present --> Signup
				// 4. The account is not linked to a local account and a session
				// cookie is present --> Linking Additional account

				// get the user with which we are logged in - is null if we
				// are
				// not logged in (does NOT check expiration)

				var oldUser = getUser.orNull

				// checks if the user is logged in (also checks the expiration!)
				var isLoggedIN = isLoggedIn

				var oldIdentity: Object = null

				// check if local user still exists - it might have been
				// deactivated/deleted,
				// so this is a signup, not a link
				if (isLoggedIN) {
					oldIdentity = getUserService.getLocalIdentity(oldUser);
					isLoggedIN &= oldIdentity != null;
					if (!isLoggedIN) {
						// if isLoggedIn is false here, then the local user has
						// been deleted/deactivated
						// so kill the session
						logout
						oldUser = null
					}
				}

				val loginIdentity = getUserService.getLocalIdentity(newUser)
				val isLinked = loginIdentity != null

				var loginUser: AuthUser = null
				if (isLinked && !isLoggedIN) {
					// 1. -> Login
					loginUser = newUser;

				} else if (isLinked && isLoggedIN) {
					// 2. -> Merge

					// merge the two identities and return the AuthUser we want
					// to use for the log in
					if (isAccountMergeEnabled
							&& !loginIdentity.equals(oldIdentity)) {
						// account merge is enabled
						// and
						// The currently logged in user and the one to log in
						// are not the same, so shall we merge?

						if (isAccountAutoMerge) {
							// Account auto merging is enabled
							loginUser = getUserService.merge(newUser, oldUser)
						} else {
							// Account auto merging is disabled - forward user
							// to merge request page
							val c = resolver.askMerge
							if (c == null) {
								throw new RuntimeException(
										Messages("playauthenticate.core.exception.merge.controller_undefined",
												SETTING_KEY_ACCOUNT_AUTO_MERGE))
							}
							storeMergeUser(newUser)
							return Redirect(c)
						}
					} else {
						// the currently logged in user and the new login belong
						// to the same local user,
						// or Account merge is disabled, so just change the log
						// in to the new user
						loginUser = newUser
					}

				} else if (!isLinked && !isLoggedIN) {
					// 3. -> Signup
					loginUser = signupUser(newUser)
				} else {
					// !isLinked && isLoggedIn:

					// 4. -> Link additional
					if (isAccountAutoLink) {
						// Account auto linking is enabled

						loginUser = getUserService.link(oldUser, newUser)
					} else {
						// Account auto linking is disabled - forward user to
						// link suggestion page
						val c = resolver.askLink
						if (c == null) {
							throw new RuntimeException(
									Messages("playauthenticate.core.exception.link.controller_undefined",
											SETTING_KEY_ACCOUNT_AUTO_LINK))
						}
						storeLinkUser(newUser)
						return Redirect(c)
					}

				}

				loginAndRedirect(loginUser)
			} else {
				InternalServerError(Messages("playauthenticate.core.exception.general"))
			}
		} catch {
		    case e: AuthException => {
		    	val c = resolver.onException(e)
		    	if (c != null) {
		    		Redirect(c);
		    	} else {
		    		val message = e.getMessage
		    		if (message != null) {
		    			InternalServerError(message);
		    		} else {
		    			InternalServerError("");
		    		}
		    	}
		    }
		}
	}

	def getProvider(providerKey: String): Option[AuthProvider] = Registry.get(providerKey)

}

trait Resolver {

	/**
	 * This is the route to your login page
	 *
	 * @return
	 */
	def login(implicit request: Request[_]): Call

	/**
	 * Route to redirect to after authentication has been finished.
	 * Only used if no original URL was stored.
	 * If you return null here, the user will get redirected to the URL of
	 * the setting
	 * afterAuthFallback
	 * You can use this to redirect to an external URL for example.
	 *
	 * @return
	 */
	def afterAuth(implicit request: Request[_]): Call

	/**
	 * This should usually point to the route where you registered
	 * com.feth.play.module.pa.controllers.AuthenticateController.
	 * authenticate(String)
	 * however you might provide your own authentication implementation if
	 * you want to
	 * and point it there
	 *
	 * @param provider
	 *            The provider ID matching one of your registered providers
	 *            in play.plugins
	 *
	 * @return a Call to follow
	 */
	def auth(provider: String)(implicit request: Request[_]): Call

	/**
	 * If you set the accountAutoMerge setting to true, you might return
	 * null for this.
	 *
	 * @return
	 */
	def askMerge(implicit request: Request[_]): Call

	/**
	 * If you set the accountAutoLink setting to true, you might return null
	 * for this
	 *
	 * @return
	 */
	def askLink(implicit request: Request[_]): Call

	/**
	 * Route to redirect to after logout has been finished.
	 * If you return null here, the user will get redirected to the URL of
	 * the setting
	 * afterLogoutFallback
	 * You can use this to redirect to an external URL for example.
	 *
	 * @return
	 */
	def afterLogout(implicit request: Request[_]): Call

	def onException(e: AuthException): Call = null
}

