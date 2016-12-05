/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.constant

import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import org.mariotaku.kpreferences.*
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.AuthTypeInt
import de.vanita5.twittnuker.extension.getNonEmptyString
import de.vanita5.twittnuker.model.CustomAPIConfig
import de.vanita5.twittnuker.model.ParcelableCredentials

val mediaPreviewStyleKey = KStringKey(KEY_MEDIA_PREVIEW_STYLE, VALUE_MEDIA_PREVIEW_STYLE_CROP)
val profileImageStyleKey = KStringKey(KEY_PROFILE_IMAGE_STYLE, VALUE_PROFILE_IMAGE_STYLE_SQUARE)
val textSizeKey = KIntKey(KEY_TEXT_SIZE, 15)
val nameFirstKey = KBooleanKey(KEY_NAME_FIRST, true)
val displayProfileImageKey = KBooleanKey(KEY_DISPLAY_PROFILE_IMAGE, true)
val mediaPreviewKey = KBooleanKey(KEY_MEDIA_PREVIEW, true)
val bandwidthSavingModeKey = KBooleanKey(KEY_BANDWIDTH_SAVING_MODE, false)
val displaySensitiveContentsKey = KBooleanKey(KEY_DISPLAY_SENSITIVE_CONTENTS, true)
val hideCardActionsKey = KBooleanKey(KEY_HIDE_CARD_ACTIONS, false)
val iWantMyStarsBackKey = KBooleanKey(KEY_I_WANT_MY_STARS_BACK, false)
val showAbsoluteTimeKey = KBooleanKey(KEY_SHOW_ABSOLUTE_TIME, false)
val linkHighlightOptionKey = KStringKey(KEY_LINK_HIGHLIGHT_OPTION, VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT)
val statusShortenerKey = KNullableStringKey(KEY_STATUS_SHORTENER, null)
val mediaUploaderKey = KNullableStringKey(KEY_MEDIA_UPLOADER, null)
val newDocumentApiKey = KBooleanKey(KEY_NEW_DOCUMENT_API, Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
val loadItemLimitKey: KIntKey = KIntKey(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT)
val defaultFeatureLastUpdated: KLongKey = KLongKey("default_feature_last_updated", -1)

object defaultAPIConfigKey : KPreferenceKey<CustomAPIConfig> {
    override fun contains(preferences: SharedPreferences): Boolean {
        if (preferences.getString(KEY_API_URL_FORMAT, null) == null) return false
        return true
    }

    override fun read(preferences: SharedPreferences): CustomAPIConfig {
        val apiUrlFormat = preferences.getNonEmptyString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getInt(KEY_AUTH_TYPE, AuthTypeInt.OAUTH)
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = preferences.getNonEmptyString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY).trim()
        val consumerSecret = preferences.getNonEmptyString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET).trim()
        return CustomAPIConfig("Default", apiUrlFormat, authType, sameOAuthSigningUrl,
                noVersionSuffix, consumerKey, consumerSecret)
    }

    override fun write(editor: SharedPreferences.Editor, value: CustomAPIConfig): Boolean {
        if (!TextUtils.isEmpty(value.consumerKey) && !TextUtils.isEmpty(value.consumerSecret)) {
            editor.putString(KEY_CONSUMER_KEY, value.consumerKey)
            editor.putString(KEY_CONSUMER_SECRET, value.consumerSecret)
        } else {
            editor.remove(KEY_CONSUMER_KEY)
            editor.remove(KEY_CONSUMER_SECRET)
        }
        editor.putString(KEY_API_URL_FORMAT, value.apiUrlFormat)
        editor.putInt(KEY_AUTH_TYPE, value.authType)
        editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, value.isSameOAuthUrl)
        editor.putBoolean(KEY_NO_VERSION_SUFFIX, value.isNoVersionSuffix)
        return true
    }

}