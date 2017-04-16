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
import android.support.annotation.DrawableRes
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.model.UserKey


class ErrorInfoStore(application: Context) {

    private val preferences = application.getSharedPreferences("error_info", Context.MODE_PRIVATE)

    operator fun get(key: String): Int {
        return preferences.getInt(key, 0)
    }

    operator fun get(key: String, extraId: String): Int {
        return get(key + "_" + extraId)
    }

    operator fun get(key: String, extraId: UserKey): Int {
        val host = extraId.host
        if (host == null) {
            return get(key, extraId.id)
        } else {
            return get(key + "_" + extraId.id + "_" + host)
        }
    }

    operator fun set(key: String, code: Int) {
        preferences.edit().putInt(key, code).apply()
    }

    operator fun set(key: String, extraId: String, code: Int) {
        set(key + "_" + extraId, code)
    }

    operator fun set(key: String, extraId: UserKey, code: Int) {
        val host = extraId.host
        if (host == null) {
            set(key, extraId.id, code)
        } else {
            set(key + "_" + extraId.id + "_" + host, code)
        }
    }

    fun remove(key: String, extraId: String) {
        remove(key + "_" + extraId)
    }

    fun remove(key: String, extraId: UserKey) {
        val host = extraId.host
        if (host == null) {
            remove(key, extraId.id)
        } else {
            remove(key + "_" + extraId.id + "_" + host)
        }
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    class DisplayErrorInfo(code: Int, @DrawableRes icon: Int, message: String) {
        var code: Int = 0
            internal set
        var icon: Int = 0
            internal set
        var message: String
            internal set

        init {
            this.code = code
            this.icon = icon
            this.message = message
        }
    }

    companion object {

        val KEY_DIRECT_MESSAGES = "direct_messages"
        val KEY_INTERACTIONS = "interactions"
        val KEY_HOME_TIMELINE = "home_timeline"
        val KEY_ACTIVITIES_BY_FRIENDS = "activities_by_friends"

        val CODE_NO_DM_PERMISSION = 1
        val CODE_NO_ACCESS_FOR_CREDENTIALS = 2
        val CODE_NETWORK_ERROR = 3
        val CODE_TIMESTAMP_ERROR = 4

        fun getErrorInfo(context: Context, code: Int): DisplayErrorInfo? {
            when (code) {
                CODE_NO_DM_PERMISSION -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_no_dm_permission))
                }
                CODE_NO_ACCESS_FOR_CREDENTIALS -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_no_access_for_credentials))
                }
                CODE_NETWORK_ERROR -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.message_toast_network_error))
                }
                CODE_TIMESTAMP_ERROR -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_info_oauth_timestamp_error))
                }
            }
            return null
        }
    }
}