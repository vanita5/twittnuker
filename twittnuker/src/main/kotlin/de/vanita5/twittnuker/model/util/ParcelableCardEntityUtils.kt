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

package de.vanita5.twittnuker.model.util


import android.support.v4.util.ArrayMap
import org.apache.commons.lang3.math.NumberUtils
import de.vanita5.twittnuker.library.twitter.model.CardEntity
import de.vanita5.twittnuker.library.twitter.util.ThreadLocalSimpleDateFormat
import de.vanita5.twittnuker.model.ParcelableCardEntity
import de.vanita5.twittnuker.model.UserKey
import java.text.DateFormat
import java.text.ParseException
import java.util.*

object ParcelableCardEntityUtils {

    internal val sISOFormat: DateFormat = ThreadLocalSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.ENGLISH)

    init {
        sISOFormat.isLenient = true
        sISOFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun fromCardEntity(card: CardEntity?, accountKey: UserKey?, accountType: String?): ParcelableCardEntity? {
        if (card == null) return null
        val obj = ParcelableCardEntity()
        obj.name = card.name
        obj.url = card.url
        obj.users = ParcelableUserUtils.fromUsers(card.users, accountKey, accountType)
        obj.account_key = accountKey
        obj.values = from(card.bindingValues)
        return obj
    }

    fun from(bindingValues: Map<String, CardEntity.BindingValue>?): Map<String, ParcelableCardEntity.ParcelableBindingValue>? {
        if (bindingValues == null) return null
        val map = ArrayMap<String, ParcelableCardEntity.ParcelableBindingValue>()
        for ((key, value) in bindingValues) {
            map.put(key, ParcelableCardEntity.ParcelableBindingValue(value))
        }
        return map
    }

    fun getAsBoolean(obj: ParcelableCardEntity, key: String, def: Boolean): Boolean {
        val value = obj.getValue(key) ?: return def
        return java.lang.Boolean.parseBoolean(value.value)
    }

    fun getAsString(obj: ParcelableCardEntity, key: String, def: String?): String? {
        return obj.getValue(key)?.value ?: return def
    }

    fun getString(obj: ParcelableCardEntity, key: String): String? {
        val value = obj.getValue(key)
        if (value == null || CardEntity.BindingValue.TYPE_STRING != value.type) return null
        return getAsString(obj, key, null)
    }

    fun getAsInteger(obj: ParcelableCardEntity, key: String, def: Int): Int {
        val value = obj.getValue(key) ?: return def
        return NumberUtils.toInt(value.value, def)
    }

    fun getAsLong(obj: ParcelableCardEntity, key: String, def: Long): Long {
        val value = obj.getValue(key) ?: return def
        return NumberUtils.toLong(value.value, def)
    }

    fun getAsDate(obj: ParcelableCardEntity, key: String, def: Date): Date {
        val value = obj.getValue(key) ?: return def
        try {
            return sISOFormat.parse(value.value)
        } catch (e: ParseException) {
            return def
        }

    }

}