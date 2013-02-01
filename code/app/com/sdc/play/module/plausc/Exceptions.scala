package com.sdc.play.module.plausc

class AuthException(message: String) extends Exception(message) {
    def this() = this(null)
}

case class AccessDeniedException(providerKey: String) extends AuthException
case class AccessTokenException(message: String) extends AuthException(message) {
    def this() = this(null)
}
case class RedirectUriMismatch() extends AuthException

case class NoOpenIdAuthException(message: String) extends AuthException(message)
case class OpenIdConnectException(message: String) extends AuthException(message)
