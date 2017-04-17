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

package de.vanita5.twittnuker.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.vanita5.twittnuker.TwittnukerConstants.SHARED_PREFERENCES_NAME
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.*

class AssistLauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val composeNowAction = prefs.getString(KEY_COMPOSE_NOW_ACTION, VALUE_COMPOSE_NOW_ACTION_COMPOSE)
        val action = when (composeNowAction) {
            VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO -> INTENT_ACTION_COMPOSE_TAKE_PHOTO
            VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE -> INTENT_ACTION_COMPOSE_PICK_IMAGE
            else -> INTENT_ACTION_COMPOSE
        }
        val intent = Intent(action)
        intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        intent.setClass(this, ComposeActivity::class.java)
        startActivity(intent)
        finish()
    }

}