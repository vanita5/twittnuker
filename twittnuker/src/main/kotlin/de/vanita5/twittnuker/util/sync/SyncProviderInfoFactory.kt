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

package de.vanita5.twittnuker.util.sync

import android.content.Context
import android.content.SharedPreferences
import de.vanita5.twittnuker.model.sync.SyncProviderEntry
import de.vanita5.twittnuker.model.sync.SyncProviderInfo
import java.util.*

abstract class SyncProviderInfoFactory {
    abstract fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo?

    abstract fun getSupportedProviders(context: Context): List<SyncProviderEntry>

    companion object {
        fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo? {
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                val info = factory.getInfoForType(type, preferences)
                if (info != null) return info
            }
            return null
        }

        fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
            val result = ArrayList<SyncProviderEntry>()
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                result.addAll(factory.getSupportedProviders(context))
            }
            return result
        }

        fun getProviderEntry(context: Context, type: String): SyncProviderEntry? {
            ServiceLoader.load(SyncProviderInfoFactory::class.java).forEach { factory ->
                factory.getSupportedProviders(context).forEach { entry ->
                    if (entry.type == type) return entry
                }
            }
            return null
        }
    }
}