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

package de.vanita5.twittnuker.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.support.v4.util.ArrayMap
import android.support.v4.util.LruCache
import de.vanita5.microblog.library.twitter.model.User
import de.vanita5.twittnuker.TwittnukerConstants.USER_COLOR_PREFERENCES_NAME
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.UserKeyUtils

class UserColorNameManager(context: Context) {

    val colorPreferences: SharedPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val colorCache = LruCache<String, Int>(512)

    private val colorChangedListeners = ArrayMap<UserColorChangedListener, OnSharedPreferenceChangeListener>()

    fun clearUserColor(userKey: UserKey) {
        val editor = colorPreferences.edit()
        val userKeyString = userKey.toString()
        colorCache.remove(userKeyString)
        editor.remove(userKeyString)
        editor.apply()
    }

    fun setUserColor(userKey: UserKey, color: Int) {
        val editor = colorPreferences.edit()
        val userKeyString = userKey.toString()
        colorCache.put(userKeyString, color)
        editor.putInt(userKeyString, color)
        editor.apply()
    }

    fun getDisplayName(user: ParcelableUser, nameFirst: Boolean): String {
        return getDisplayName(user.key, user.name, user.screen_name, nameFirst)
    }

    fun getDisplayName(user: User, nameFirst: Boolean): String {
        return getDisplayName(UserKeyUtils.fromUser(user), user.name, user.screenName, nameFirst)
    }

    fun getDisplayName(user: ParcelableUserList, nameFirst: Boolean): String {
        return getDisplayName(user.user_key, user.user_name, user.user_screen_name, nameFirst)
    }

    fun getDisplayName(status: ParcelableStatus, nameFirst: Boolean): String {
        return getDisplayName(status.user_key, status.user_name, status.user_screen_name, nameFirst)
    }

    fun getDisplayName(userKey: UserKey, name: String, screenName: String, nameFirst: Boolean): String {
        return getDisplayName(userKey.toString(), name, screenName, nameFirst)
    }

    fun getDisplayName(key: String, name: String, screenName: String, nameFirst: Boolean): String {
        return decideDisplayName(name, screenName, nameFirst)
    }

    fun getUserColor(userKey: UserKey): Int {
        return getUserColor(userKey.toString())
    }

    fun getUserColor(userId: String): Int {
        val cached = colorCache.get(userId)
        if (cached != null) return cached
        val color = colorPreferences.getInt(userId, Color.TRANSPARENT)
        colorCache.put(userId, color)
        return color
    }

    fun registerColorChangedListener(listener: UserColorChangedListener) {
        val preferenceChangeListener = OnColorPreferenceChangeListener(listener)
        colorPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        colorChangedListeners[listener] = preferenceChangeListener
    }

    fun unregisterColorChangedListener(listener: UserColorChangedListener) {
        val preferenceChangeListener = colorChangedListeners.remove(listener) ?: return
        colorPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    interface UserColorChangedListener {
        fun onUserColorChanged(userKey: UserKey, color: Int)
    }

    private class OnColorPreferenceChangeListener internal constructor(private val mListener: UserColorChangedListener?) : OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
            val userKey = UserKey.valueOf(key)
            mListener?.onUserColorChanged(userKey, preferences.getInt(key, 0))
        }
    }

    companion object {

        fun decideDisplayName(name: String, screenName: String,
                nameFirst: Boolean) = if (nameFirst) name else "@$screenName"

    }
}