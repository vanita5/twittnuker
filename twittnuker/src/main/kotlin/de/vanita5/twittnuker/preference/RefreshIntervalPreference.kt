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

import android.app.job.JobInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.support.v7.preference.PreferenceManager
import android.util.AttributeSet
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toLong
import de.vanita5.twittnuker.constant.autoRefreshCompatibilityModeKey
import java.util.concurrent.TimeUnit

class RefreshIntervalPreference(
        context: Context, attrs: AttributeSet? = null
) : EntrySummaryListPreference(context, attrs) {

    private val entriesBackup = entries
    private val valuesBackup = entryValues

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == autoRefreshCompatibilityModeKey.key) {
            updateEntries()
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        sharedPreferences.registerOnSharedPreferenceChangeListener(changeListener)
        updateEntries()
    }

    override fun onDetached() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)
        super.onDetached()
    }

    private fun updateEntries() {
        var index: Int = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !sharedPreferences[autoRefreshCompatibilityModeKey]) {
            index = valuesBackup.indexOfFirst {
                val intervalMinutes = it.toString().toLong(-1L)
                if (intervalMinutes < 0) return@indexOfFirst false
                return@indexOfFirst TimeUnit.MINUTES.toMillis(intervalMinutes) >= JobInfo.getMinPeriodMillis()
            }

        }

        if (index >= 0) {
            entryValues = valuesBackup.sliceArray(index..valuesBackup.lastIndex)
            entries = entriesBackup.sliceArray(index..entriesBackup.lastIndex)
        } else {
            entryValues = valuesBackup
            entries = entriesBackup
        }
        val valueMinutes = value.toLong(-1)
        val minValue = entryValues.firstOrNull()?.toString().toLong(-1)
        if (valueMinutes > 0 && valueMinutes < minValue) {
            value = minValue.toString()
        }
    }
}