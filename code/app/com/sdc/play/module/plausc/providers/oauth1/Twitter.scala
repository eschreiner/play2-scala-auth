package com.sdc.play.module.plausc.providers.oauth1

import play.api._
import play.api.libs.concurrent.Promise
import play.api.libs.json.JsValue
import play.api.libs.oauth._
import play.api.libs.ws.WS
import play.libs.Json

import com.sdc.play.module.plausc._

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
case class TwitterAuthInfo(token: String, secret: String) extends OAuth1AuthInfo(token,secret)

class TwitterAuthProvider(app: Application)
extends OAuth1AuthProvider[TwitterAuthUser, TwitterAuthInfo](app) {

	private def USER_INFO_URL_SETTING_KEY = "userInfoUrl"

	override def getKey = TwitterConstants.PROVIDER_KEY

	import OAuth1Helper._
	import OASettingKeys._

	override protected def transform(info: TwitterAuthInfo): TwitterAuthUser = {

	    val c = configuration.get

		val url = c.getString(USER_INFO_URL_SETTING_KEY).get

		val token = new RequestToken(info.token, info.secret)
		val cK = new ConsumerKey(c.getString(CONSUMER_KEY).get, c.getString(CONSUMER_SECRET).get)

		val op = new OAuthCalculator(cK, token)

		val promise = promiseFor(url,op)

		val json = toJson(promise)
		new TwitterAuthUser(json, info)
	}

	override protected def buildInfo(rtoken: RequestToken): TwitterAuthInfo =
		new TwitterAuthInfo(rtoken.token, rtoken.secret)

}

import java.util.Locale

import org.codehaus.jackson.JsonNode

import com.sdc.play.module.plausc.user._
import com.sdc.play.module.plausc.providers.util.JsonHelpers._

class TwitterAuthUser(nodeInfo: JsonNode, info: OAuth1AuthInfo)
extends BasicOAuth1AuthUser(nodeInfo.get(TwitterConstants.ID).asText, info, null)
with PicturedIdentity with LocaleIdentity {

    import TwitterConstants._

	override def getName       = asText(nodeInfo, NAME)
	val locale     = asText(nodeInfo, LOCALE)
	val screenName = asText(nodeInfo, SCREEN_NAME)
	val verified   = asBool(nodeInfo, VERIFIED)
	override def getPicture    = asText(nodeInfo, PROFILE_IMAGE_URL)

	override def getProvider = PROVIDER_KEY

}

	object TwitterConstants {

		val PROVIDER_KEY = "twitter"

		// {
		val ID = "id";
		// "id":15484335,
		// "listed_count":5,
		val PROFILE_IMAGE_URL = "profile_image_url";
		// "profile_image_url":"http://a0.twimg.com/profile_images/57096786/j_48x48_normal.png",
		// "following":false,
		// "followers_count":118,
		// "location":"Sydney, Australia",
		// "contributors_enabled":false,
		// "profile_background_color":"C0DEED",
		// "time_zone":"Berlin",
		// "geo_enabled":true,
		// "utc_offset":3600,
		// "is_translator":false,
		val NAME = "name";
		// "name":"Joscha Feth",
		// "profile_background_image_url":"http://a0.twimg.com/images/themes/theme1/bg.png",
		// "show_all_inline_media":false,
		val SCREEN_NAME = "screen_name";
		// "screen_name":"joschafeth",
		// "protected":false,
		// "profile_link_color":"0084B4",
		// "default_profile_image":false,
		// "follow_request_sent":false,
		// "profile_background_image_url_https":"https://si0.twimg.com/images/themes/theme1/bg.png",
		// "favourites_count":3,
		// "notifications":false,
		val VERIFIED = "verified";
		// "verified":false,
		// "profile_use_background_image":true,
		// "profile_text_color":"333333",
		// "description":"",
		// "id_str":"15484335",
		val LOCALE = "lang";
		// "lang":"en",
		// "profile_sidebar_border_color":"C0DEED",
		// "profile_image_url_https":"https://si0.twimg.com/profile_images/57096786/j_48x48_normal.png",
		// "default_profile":true,
		// "url":null,
		// "statuses_count":378,
		// "status":{
		// "in_reply_to_user_id":11111,
		// "truncated":false,
		// "created_at":"Mon Jul 23 13:22:31 +0000 2012",
		// "coordinates":null,
		// "geo":null,
		// "favorited":false,
		// "in_reply_to_screen_name":"XXX",
		// "contributors":null,
		// "in_reply_to_status_id_str":"111111",
		// "place":null,
		// "source":"<a href=\"http://itunes.apple.com/us/app/twitter/id409789998?mt=12\" rel=\"nofollow\">Twitter for Mac</a>",
		// "in_reply_to_user_id_str":"11111",
		// "id":111111,
		// "id_str":"111111",
		// "retweeted":false,
		// "retweet_count":0,
		// "in_reply_to_status_id":11111,
		// "text":"some text to up to 140chars here"
		// },
		// "profile_background_tile":false,
		// "friends_count":120,
		// "created_at":"Fri Jul 18 18:17:46 +0000 2008",
		// "profile_sidebar_fill_color":"DDEEF6"

	}

