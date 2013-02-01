package com.sdc.play.module.plausc.providers.password

import org.mindrot.jbcrypt.BCrypt

import com.sdc.play.module.plausc.user._

import UsernamePasswordConstants._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
trait UsernamePassword {

	def getEmail: String
	def getPassword: String
}

abstract class UsernamePasswordAuthUser(@transient password: String, email: String)
extends AuthUser with EmailIdentity {

    override def getEmail = email

	/**
	 * Should return null if the clearString given is null.
	 *
	 * @return
	 */
	//protected abstract String createPassword(final String clearString);

	/**
	 * Should return false if either the candidate or stored password is null.
	 *
	 * @param candidate
	 * @return
	 */
	//public abstract boolean checkPassword(final String candidate);

	override def getId = getHashedPassword

	override def getProvider = PROVIDER_KEY

	def getHashedPassword = createPassword(password)

	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	protected def createPassword(clearString: String): String = {
		BCrypt.hashpw(clearString, BCrypt.gensalt)
	}

	/**
	 * You *SHOULD* provide your own implementation of this which implements your own security.
	 */
	def checkPassword(hashed: String, candidate: String): Boolean =
		hashed != null && candidate != null && BCrypt.checkpw(candidate, hashed)

}

case class DefaultUsernamePasswordAuthUser(password: String, email: String)
extends UsernamePasswordAuthUser(password,email) {

	override def getId = email

	/**
	 * This MUST be overwritten by an extending class.
	 * The default implementation stores a clear string, which is NOT recommended.
	 *
	 * Should return null if the clearString given is null.
	 *
	 * @return
	 */
	override def createPassword(clearString: String) = clearString

}

class SessionUsernamePasswordAuthUser(password: String, email: String, expires: Long)
extends DefaultUsernamePasswordAuthUser(password,email) {

    override def getId = email

}

import play.Application
import play.api.data.Form
import play.api.mvc._
import play.mvc.Http
import play.mvc.Http.Context

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.sdc.play.module.plausc._
import com.sdc.play.module.plausc.providers.AuthProvider

abstract class UsernamePasswordAuthProvider[R,
    UL <: UsernamePasswordAuthUser, US <: UsernamePasswordAuthUser,
    L <: UsernamePassword, S <: UsernamePassword](app: Application)
extends AuthProvider(app) {

	override protected def neededSettingKeys = {
		SETTING_KEY_MAIL +"."+ SETTING_KEY_MAIL_DELAY ::
		SETTING_KEY_MAIL +"."+ SETTING_KEY_MAIL_FROM +"."+ SETTING_KEY_MAIL_FROM_EMAIL ::
		Nil
	}

	protected var mailer: Mailer = null

	override def onStart = {
		super.onStart
		mailer = Mailer.getCustomMailer(configuration.getConfig(SETTING_KEY_MAIL));
	}

	def getKey = PROVIDER_KEY

	import Action._
	import SignupResult._
	import LoginResult._

	def authenticate(context: Context, payload: Object)(implicit request: Request[_]): Object = {

		if (payload == SIGNUP) {
			val signup = getSignup(context)
			val authUser = buildSignupAuthUser(signup, context)
			val r = signupUser(authUser)

			r match {
			case USER_EXISTS =>
				// The user exists already
				userExists(authUser).url
			case USER_EXISTS_UNVERIFIED =>
				// User got created as unverified
				// Send validation email
				sendVerifyEmailMailing(context, authUser)
				userUnverified(authUser).url
			case USER_CREATED_UNVERIFIED =>
				// User got created as unverified
				// Send validation email
				sendVerifyEmailMailing(context, authUser)
				userUnverified(authUser).url
			case USER_CREATED =>
				// continue to login...
				authUser
			case _ =>
				throw new AuthException("Something in signup went wrong")
			}
		} else if (payload == SIGNIN) {
			val login = getLogin(context)
			val authUser = buildLoginAuthUser(login, context)
			val r = loginUser(authUser)
			r match {
			case USER_UNVERIFIED =>
				// The email of the user is not verified, yet - we won't allow
				// him to log in
				userUnverified(authUser).url
			case USER_LOGGED_IN =>
				// The user exists and the given password was correct
				authUser
			case WRONG_PASSWORD =>
				// don't expose this - it might harm users privacy if anyone
				// knows they signed up for our service
				onLoginUserNotFound(context)
			case NOT_FOUND =>
				// forward to login page
				onLoginUserNotFound(context)
			case _ =>
				throw new AuthException("Something in login went wrong");
			}
		} else {
			PlayAuthenticate.resolver.login.url
		}
	}

	protected def onLoginUserNotFound(context: Context) = PlayAuthenticate.resolver.login.url

	override def getSessionAuthUser(id: String, expires: Long): AuthUser = {
		new SessionUsernamePasswordAuthUser(getKey, id, expires)
	}

	private def getSignup(context: Context)(implicit request: Request[_]): S = {
		// TODO change to getSignupForm().bindFromRequest(request) after 2.1
		Http.Context.current.set(context)
		getSignupForm.bindFromRequest.get
	}

	private def getLogin(context: Context)(implicit request: Request[_]): L = {
		// TODO change to getSignupForm().bindFromRequest(request) after 2.1
		Http.Context.current.set(context)
		getLoginForm.bindFromRequest.get
	}

	/**
	 * You might overwrite this to provide your own recipient format
	 * implementation,
	 * however the default should be fine for most cases
	 *
	 * @param user
	 * @return
	 */
	protected def getEmailName(user: US): String = {
		val name = if (user.isInstanceOf[NameIdentity]) {
			user.asInstanceOf[NameIdentity].getName
		} else null

		return getEmailName(user.getEmail, name);
	}

	protected def getEmailName(email: String, name: String) = Mailer.getEmailName(email, name)

	protected def generateVerificationRecord(user: US): R

	private def sendVerifyEmailMailing(context: Context, user: US) = {
		val subject = getVerifyEmailMailingSubject(user, context)
		val record = generateVerificationRecord(user);
		val body = getVerifyEmailMailingBody(record, user, context)
//		val verifyMail = new Mail(subject, body, new String[] { getEmailName(user) })
//		mailer.sendMail(verifyMail)
	}

	override def isExternal = false

	protected def getVerifyEmailMailingSubject(user: US, context: Context): String
	protected def getVerifyEmailMailingBody(verificationRecord: R, user: US, context: Context): Body

	protected def buildLoginAuthUser(login: L, context: Context): UL
	protected def buildSignupAuthUser(signup: S, context: Context): US

	protected def loginUser(authUser: UL): LoginResult.LoginResult
	protected def signupUser(user: US): SignupResult.SignupResult

	protected def getSignupForm: Form[S]
	protected def getLoginForm: Form[L]

	protected def userExists(authUser: UsernamePasswordAuthUser): Call
	protected def userUnverified(authUser: UsernamePasswordAuthUser): Call

}

object Action extends Enumeration {
    type Action = Value
    val SIGNIN, SIGNUP = Value
}

object SignupResult extends Enumeration {
    type SignupResult = Value
	val USER_EXISTS, USER_CREATED_UNVERIFIED, USER_CREATED, USER_EXISTS_UNVERIFIED = Value
}

object LoginResult extends Enumeration {
    type LoginResult = Value
	val USER_UNVERIFIED, USER_LOGGED_IN, NOT_FOUND, WRONG_PASSWORD = Value
}

object UsernamePassword {

    import Action._

	def handleLogin(implicit request: Request[_]): Result = handleAuthentication(SIGNIN)
	def handleSignup(implicit request: Request[_]): Result = handleAuthentication(SIGNUP)

	private def handleAuthentication(action: Action)(implicit request: Request[_]): Result = {
		PlayAuthenticate.handleAuthentication(PROVIDER_KEY, action)
	}

}

object UsernamePasswordConstants {

	val PROVIDER_KEY = "password"

	val SETTING_KEY_MAIL = "mail"
	val SETTING_KEY_MAIL_FROM_EMAIL = Mailer.SettingKeys.FROM_EMAIL
	val SETTING_KEY_MAIL_DELAY = Mailer.SettingKeys.DELAY
	val SETTING_KEY_MAIL_FROM = Mailer.SettingKeys.FROM

}
