package com.sdc.play.module.plausc.controllers

import play.api.mvc._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
object ControllerHelpers {

    import play.api.http.HeaderNames._

	def ActionWithContext[U](f: Context[AnyContent,U] => PlainResult) = {
        Action { request =>
            val cookie = request.session.get("session")
//            val account = Account.recoverFrom(cookie,request.remoteAddress)
            val result = f(Context(request,None))
            result.withHeaders(CACHE_CONTROL -> "no-cache")
        }
    }

}

case class Context[A,U](request: Request[A],
        user: Option[U])
extends WrappedRequest(request) with AuthContext[A,U]

trait AuthContext[A,UserInfo] extends Request[A] {

  def request: Request[A]

  def user: Option[UserInfo]

  def isAuth = user.isDefined

  def is(user: UserInfo) = this.user == Some(user)
}
