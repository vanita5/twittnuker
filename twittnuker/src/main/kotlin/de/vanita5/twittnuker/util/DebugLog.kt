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

import android.util.Log
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG

object DebugLog {

    @JvmStatic
    fun v(tag: String = LOGTAG, msg: String, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        if (tr != null) {
            return Log.v(tag, msg, tr)
        } else {
            return Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String = LOGTAG, msg: String, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        if (tr != null) {
            return Log.d(tag, msg, tr)
        } else {
            return Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String = LOGTAG, msg: String? = null, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        if (msg != null && tr != null) {
            return Log.w(tag, msg, tr)
        } else if (msg != null) {
            return Log.w(tag, msg)
        } else {
            return Log.w(tag, tr)
        }
    }
}