package com.sdc.play.module.plausc.service

import play._

import com.sdc.play.module.plausc.{PlayAuthenticate => pa}
import com.sdc.play.module.plausc.user.AuthUser

abstract class UserServicePlugin(application: Application) extends Plugin with UserService {

	override def onStart = {
		if (pa.hasUserService) {
			Logger.warn("A user service was already registered - replacing the old one, " +
					"however this might hint to a configuration problem if this is a production environment.");
		}
		pa.userService = this
	}

	override def update(knownUser: AuthUser): AuthUser = {
		// Default: just do nothing when user logs in again
		knownUser
	}
}