package com.sdc.play.module.plausc.user

import java.io.Serializable
import java.util.Locale

import org.apache.commons.lang.LocaleUtils

trait AuthUser extends AuthUserIdentity with Serializable {

    def getExpires = AuthUser.NO_EXPIRATION

	override def hashCode: Int = {
		val prime = 31
		var result = 1
		result = prime * result + (if (getId == null) 0 else getId.hashCode)
		result = prime * result + (if (getProvider == null) 0 else getProvider.hashCode)
		result
	}

	override def equals(obj: Object): Boolean = {
		if (this == obj) return true
		if (obj == null) return false
		if (getClass != obj.getClass) return false

		val other = obj.asInstanceOf[AuthUserIdentity]

		if (getId == null) {
			if (other.getId != null) return false
		} else if (getId != other.getId)
			return false
		if (getProvider == null) {
			if (other.getProvider != null) return false
		} else if (getProvider != other.getProvider)
			return false
		return true
	}

	override def toString: String = getId + "@" + getProvider

}

object AuthUser {
	val NO_EXPIRATION = -1L
}

object AuthUserHelper {

	def getLocaleFromString(locale: String): Locale = {
		if (locale != null && !locale.isEmpty()) {
			try {
				LocaleUtils.toLocale(locale);
			} catch {
			    case iae: java.lang.IllegalArgumentException =>
				try {
					LocaleUtils.toLocale(locale.replace('-', '_'))
				} catch {
				    case e: Exception => null
				}
			}
		} else {
			null
		}
	}

	@Override def toString[T <: AuthUserIdentity with NameIdentity](identity: T): String = {
		val sb = new StringBuilder
		if (identity.getName != null) {
			sb.append(identity.getName)
			sb.append(" ");
		}
		if (identity.isInstanceOf[EmailIdentity]) {
			val i2 = identity.asInstanceOf[EmailIdentity]
			if (i2.getEmail != null) {
				sb.append("(")
				sb.append(i2.getEmail)
				sb.append(") ")
			}
		}
		if (sb.length == 0) {
			sb.append(identity.getId)
		}
		sb.append(" @ ")
		sb.append(identity.getProvider)

		sb.toString
	}
}
