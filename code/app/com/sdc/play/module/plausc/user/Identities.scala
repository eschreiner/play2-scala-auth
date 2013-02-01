package com.sdc.play.module.plausc.user

import java.util.Locale

trait BasicIdentity extends EmailIdentity with NameIdentity

trait EmailIdentity extends AuthUserIdentity {
	def getEmail: String
}
trait NameIdentity {
    def getName: String
}
trait AuthUserIdentity {
    def getId: String
	def getProvider: String
}
trait ProfiledIdentity {
	def getProfileLink: String
}
trait ExtendedIdentity extends BasicIdentity with FirstLastNameIdentity {
	def getGender: String
}
trait FirstLastNameIdentity extends NameIdentity {
	def getFirstName: String
	def getLastName: String
}

trait LocaleIdentity {
    val locale: String
	def getLocale: Locale = AuthUserHelper.getLocaleFromString(locale);
}
trait PicturedIdentity {
	def getPicture: String
}
