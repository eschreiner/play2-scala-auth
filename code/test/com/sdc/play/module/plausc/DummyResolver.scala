package com.sdc.play.module.plausc

import controllers.routes.Authenticate

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
class DummyResolver extends Resolver {

    import play.api.mvc._

    def login(implicit request: Request[_]) = null

    def afterAuth(implicit request: Request[_]) = null

    def auth(provider: String)(implicit request: Request[_]) = Authenticate.authenticate(provider)

    def askMerge(implicit request: Request[_]) = null

    def askLink(implicit request: Request[_]) = null

    def afterLogout(implicit request: Request[_]) = null

}