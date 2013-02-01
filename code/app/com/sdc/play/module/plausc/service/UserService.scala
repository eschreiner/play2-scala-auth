package com.sdc.play.module.plausc.service

import com.sdc.play.module.plausc.user._

trait UserService {

	/**
	 * Saves auth provider/id combination to a local user
	 * @param authUser
	 * @return The local identifying object or null if the user existed
	 */
	def save(authUser: AuthUser): Object

	/**
	 * Returns the local identifying object if the auth provider/id combination has been linked to a local user account already
	 * or null if not.
	 * This gets called on any login to check whether the session user still has a valid corresponding local user
	 *
	 * @param identity
	 * @return
	 */
	def getLocalIdentity(identity: AuthUserIdentity): Object

	/**
	 * Merges two user accounts after a login with an auth provider/id that is linked to a different account than the login from before
	 * Returns the user to generate the session information from
	 *
	 * @param newUser
	 * @param oldUser
	 * @return
	 */
	def merge(newUser: AuthUser, oldUser: AuthUser): AuthUser

	/**
	 * Links a new account to an exsting local user.
	 * Returns the auth user to log in with
	 *
	 * @param oldUser
	 * @param newUser
	 */
	def link(oldUser: AuthUser, newUser: AuthUser): AuthUser

	/**
	 * Gets called when a user logs in - you might make profile updates here with data coming from the login provider
	 * or bump a last-logged-in date
	 *
	 * @param knownUser
	 * @return
	 */
	def update(knownUser: AuthUser): AuthUser
}
