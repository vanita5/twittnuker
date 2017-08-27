/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.util.AttributeSet
import de.vanita5.twittnuker.constant.IntentConstants.INTENT_ACTION_HIDDEN_SETTINGS_ENTRY

class HiddenSettingEntryPreference(
        context: Context,
        attrs: AttributeSet? = null
) : TintedPreferenceCategory(context, attrs) {

    @SuppressLint("RestrictedApi")
    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?, id: Long) {
        super.onAttachedToHierarchy(preferenceManager, id)
        removeAll()
        val entryIntent = Intent(INTENT_ACTION_HIDDEN_SETTINGS_ENTRY)
        entryIntent.`package` = context.packageName
        context.packageManager.queryIntentActivities(entryIntent, 0).forEach { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
            addPreference(Preference(context).apply {
                title = activityInfo.loadLabel(context.packageManager)
                intent = Intent(INTENT_ACTION_HIDDEN_SETTINGS_ENTRY).setClassName(context, activityInfo.name)
            })
        }
    }
}