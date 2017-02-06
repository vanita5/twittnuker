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

package de.vanita5.twittnuker.model.analyzer

import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.ContentType
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.util.Analyzer
import de.vanita5.twittnuker.util.LinkCreator

data class Share(
        val id: String,
        @ContentType val type: String,
        @AccountType override val accountType: String?,
        override val accountHost: String? = null
) : Analyzer.Event {
    companion object {
        fun status(accountType: String?, status: ParcelableStatus): Share {
            val uri = LinkCreator.getStatusWebLink(status).toString()
            return Share(uri, ContentType.STATUS, accountType, status.account_key.host)
        }
    }
}