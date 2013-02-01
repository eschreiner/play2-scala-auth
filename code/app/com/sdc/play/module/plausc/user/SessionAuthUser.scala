package com.sdc.play.module.plausc.user

case class SessionAuthUser(provider: String, id: String, expires: Long) extends AuthUser {

    override def getId = id

    override def getProvider = provider
    override def getExpires = expires

}
