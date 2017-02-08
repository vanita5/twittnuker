/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import org.mariotaku.ktextension.toLong
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants.*
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.PreviewStyle
import de.vanita5.twittnuker.extension.getNonEmptyString
import de.vanita5.twittnuker.model.CustomAPIConfig
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.sync.SyncProviderInfo
import de.vanita5.twittnuker.preference.ThemeBackgroundPreference
import de.vanita5.twittnuker.util.sync.SyncProviderInfoFactory
import de.vanita5.twittnuker.view.ProfileImageView


val textSizeKey = KIntKey(KEY_TEXT_SIZE, 15)
val nameFirstKey = KBooleanKey(KEY_NAME_FIRST, true)
val displayProfileImageKey = KBooleanKey(KEY_DISPLAY_PROFILE_IMAGE, true)
val mediaPreviewKey = KBooleanKey(KEY_MEDIA_PREVIEW, true)
val bandwidthSavingModeKey = KBooleanKey(KEY_BANDWIDTH_SAVING_MODE, false)
val displaySensitiveContentsKey = KBooleanKey(KEY_DISPLAY_SENSITIVE_CONTENTS, true)
val hideCardActionsKey = KBooleanKey(KEY_HIDE_CARD_ACTIONS, false)
val iWantMyStarsBackKey = KBooleanKey(KEY_I_WANT_MY_STARS_BACK, false)
val showAbsoluteTimeKey = KBooleanKey(KEY_SHOW_ABSOLUTE_TIME, false)
val statusShortenerKey = KNullableStringKey(KEY_STATUS_SHORTENER, null)
val mediaUploaderKey = KNullableStringKey(KEY_MEDIA_UPLOADER, null)
val newDocumentApiKey = KBooleanKey(KEY_NEW_DOCUMENT_API, Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
val rememberPositionKey = KBooleanKey(KEY_REMEMBER_POSITION, true)
val attachLocationKey = KBooleanKey(KEY_ATTACH_LOCATION, false)
val attachPreciseLocationKey = KBooleanKey(KEY_ATTACH_PRECISE_LOCATION, false)
val noCloseAfterTweetSentKey = KBooleanKey(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
val loadItemLimitKey = KIntKey(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT)
val defaultFeatureLastUpdated = KLongKey("default_feature_last_updated", -1)
val drawerTutorialCompleted = KBooleanKey(KEY_SETTINGS_WIZARD_COMPLETED, false)
val stopAutoRefreshWhenBatteryLowKey = KBooleanKey(KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW, true)
val apiLastChangeKey = KLongKey(KEY_API_LAST_CHANGE, -1)
val bugReportsKey = KBooleanKey(KEY_BUG_REPORTS, BuildConfig.DEBUG)
val readFromBottomKey = KBooleanKey(KEY_READ_FROM_BOTTOM, true)
val randomizeAccountNameKey = KBooleanKey(KEY_RANDOMIZE_ACCOUNT_NAME, false)
val defaultAutoRefreshKey = KBooleanKey(KEY_DEFAULT_AUTO_REFRESH, false)
val defaultAutoRefreshAskedKey = KBooleanKey("default_auto_refresh_asked", true)
val unreadCountKey = KBooleanKey(KEY_UNREAD_COUNT, true)
val drawerToggleKey = KBooleanKey(KEY_DRAWER_TOGGLE, false)
val fabVisibleKey = KBooleanKey(KEY_FAB_VISIBLE, true)
val themeKey = KStringKey(KEY_THEME, VALUE_THEME_NAME_AUTO)
val themeColorKey = KIntKey(KEY_THEME_COLOR, 0)
val filterUnavailableQuoteStatusesKey = KBooleanKey("filter_unavailable_quote_statuses", false)
val filterPossibilitySensitiveStatusesKey = KBooleanKey("filter_possibility_sensitive_statuses", false)
val chromeCustomTabKey = KBooleanKey("chrome_custom_tab", true)
val lightFontKey = KBooleanKey("light_font", true)
val extraFeaturesNoticeVersionKey = KIntKey("extra_features_notice_version", 0)
val mediaPreloadKey = KBooleanKey(KEY_MEDIA_PRELOAD, true)
val mediaPreloadOnWifiOnlyKey = KBooleanKey(KEY_PRELOAD_WIFI_ONLY, false)
val autoRefreshCompatibilityModeKey = KBooleanKey("auto_refresh_compatibility_mode", Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
val floatingDetailedContentsKey = KBooleanKey("floating_detailed_contents", true)

object themeBackgroundAlphaKey : KSimpleKey<Int>(KEY_THEME_BACKGROUND_ALPHA, 0xFF) {
    override fun read(preferences: SharedPreferences): Int {
        return preferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA)
                .coerceIn(ThemeBackgroundPreference.MIN_ALPHA, ThemeBackgroundPreference.MAX_ALPHA)
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putInt(key, value.coerceIn(ThemeBackgroundPreference.MIN_ALPHA,
                ThemeBackgroundPreference.MAX_ALPHA))
        return true
    }
}

object profileImageStyleKey : KSimpleKey<Int>(KEY_PROFILE_IMAGE_STYLE, ProfileImageView.SHAPE_RECTANGLE) {
    override fun read(preferences: SharedPreferences): Int {
        if (preferences.getString(key, null) == VALUE_PROFILE_IMAGE_STYLE_ROUND) return ProfileImageView.SHAPE_CIRCLE
        return ProfileImageView.SHAPE_RECTANGLE
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, if (value == ProfileImageView.SHAPE_CIRCLE) VALUE_PROFILE_IMAGE_STYLE_ROUND else VALUE_PROFILE_IMAGE_STYLE_SQUARE)
        return true
    }

}

object mediaPreviewStyleKey : KSimpleKey<Int>(KEY_MEDIA_PREVIEW_STYLE, PreviewStyle.CROP) {
    override fun read(preferences: SharedPreferences): Int {
        when (preferences.getString(key, null)) {
            VALUE_MEDIA_PREVIEW_STYLE_SCALE -> return PreviewStyle.SCALE
            VALUE_MEDIA_PREVIEW_STYLE_REAL_SIZE -> return PreviewStyle.REAL_SIZE
            else -> return PreviewStyle.CROP
        }
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, when (value) {
            PreviewStyle.SCALE -> VALUE_MEDIA_PREVIEW_STYLE_SCALE
            PreviewStyle.REAL_SIZE -> VALUE_MEDIA_PREVIEW_STYLE_REAL_SIZE
            else -> VALUE_MEDIA_PREVIEW_STYLE_CROP
        })
        return true
    }

}

object linkHighlightOptionKey : KSimpleKey<Int>(KEY_LINK_HIGHLIGHT_OPTION, VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT) {
    override fun read(preferences: SharedPreferences): Int = when (preferences.getString(key, null)) {
        VALUE_LINK_HIGHLIGHT_OPTION_BOTH -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH
        VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE
        VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
        else -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, when (value) {
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH -> VALUE_LINK_HIGHLIGHT_OPTION_BOTH
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE -> VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT -> VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT
            else -> VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT
        })
        return true
    }

}

object refreshIntervalKey : KSimpleKey<Long>(KEY_REFRESH_INTERVAL, 15) {
    override fun read(preferences: SharedPreferences): Long {
        return preferences.getString(key, null).toLong(def)
    }

    override fun write(editor: SharedPreferences.Editor, value: Long): Boolean {
        editor.putString(key, value.toString())
        return true
    }

}

object defaultAPIConfigKey : KPreferenceKey<CustomAPIConfig> {
    override fun contains(preferences: SharedPreferences): Boolean {
        if (preferences.getString(KEY_API_URL_FORMAT, null) == null) return false
        return true
    }

    override fun read(preferences: SharedPreferences): CustomAPIConfig {
        val apiUrlFormat = preferences.getNonEmptyString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getString(KEY_CREDENTIALS_TYPE, Credentials.Type.OAUTH)
        val customApiType = preferences.getString(KEY_CUSTOM_API_TYPE, null) ?: AccountType.TWITTER
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = preferences.getNonEmptyString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY).trim()
        val consumerSecret = preferences.getNonEmptyString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET).trim()
        return CustomAPIConfig("Default", customApiType, apiUrlFormat, authType, sameOAuthSigningUrl,
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
        editor.putString(KEY_CUSTOM_API_TYPE, value.type)
        editor.putString(KEY_CREDENTIALS_TYPE, value.credentialsType)
        editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, value.isSameOAuthUrl)
        editor.putBoolean(KEY_NO_VERSION_SUFFIX, value.isNoVersionSuffix)
        return true
    }

}

object dataSyncProviderInfoKey : KPreferenceKey<SyncProviderInfo?> {
    private const val PROVIDER_TYPE_KEY = "sync_provider_type"

    override fun contains(preferences: SharedPreferences): Boolean {
        return read(preferences) != null
    }

    override fun read(preferences: SharedPreferences): SyncProviderInfo? {
        val type = preferences.getString(PROVIDER_TYPE_KEY, null) ?: return null
        return SyncProviderInfoFactory.getInfoForType(type, preferences)
    }

    override fun write(editor: SharedPreferences.Editor, value: SyncProviderInfo?): Boolean {
        if (value == null) {
            editor.remove(PROVIDER_TYPE_KEY)
        } else {
            editor.putString(PROVIDER_TYPE_KEY, value.type)
            value.writeToPreferences(editor)
        }
        return true
    }

}

object composeAccountsKey : KSimpleKey<Array<UserKey>?>(KEY_COMPOSE_ACCOUNTS, null) {

    override fun read(preferences: SharedPreferences): Array<UserKey>? {
        val string = preferences.getString(key, null) ?: return null
        return UserKey.arrayOf(string)
    }

    override fun write(editor: SharedPreferences.Editor, value: Array<UserKey>?): Boolean {
        editor.putString(key, value?.joinToString(","))
        return true
    }

}