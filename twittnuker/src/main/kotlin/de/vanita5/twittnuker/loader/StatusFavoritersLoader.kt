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

package de.vanita5.twittnuker.loader

import android.content.Context
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.TwitterWeb
import de.vanita5.twittnuker.library.twitter.model.IDs
import de.vanita5.twittnuker.library.twitter.model.IDsAccessor
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import java.text.ParseException

class StatusFavoritersLoader(
        context: Context,
        accountKey: UserKey,
        private val statusId: String,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : CursorSupportUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getIDs(twitter: MicroBlog, details: AccountDetails, paging: Paging): IDs {
        if (details.isOfficial(context)) {
            return twitter.getStatusActivitySummary(statusId).favoriters
        } else if (details.type == AccountType.TWITTER) {
            val web = details.newMicroBlogInstance(context, TwitterWeb::class.java)
            val htmlUsers = web.getFavoritedPopup(statusId).htmlUsers
            return IDs().also {
                IDsAccessor.setIds(it, parseUserIds(htmlUsers))
            }
        }
        throw MicroBlogException("Not supported")
    }

    override fun useIDs(details: AccountDetails): Boolean {
        return true
    }

    @Throws(MicroBlogException::class)
    private fun parseUserIds(html: String): Array<String> {
        val parser = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())
        val userIds = ArrayList<String>()
        val handler = object : AbstractSimpleMarkupHandler() {
            override fun handleOpenElement(elementName: String, attributes: Map<String, String>?,
                    line: Int, col: Int) {
                if (elementName == "div" && attributes != null) {
                    if (attributes["class"]?.split(" ")?.contains("account") ?: false) {
                        attributes["data-user-id"]?.let { userIds.add(it) }
                    }
                }
            }
        }
        try {
            parser.parse(html, handler)
        } catch (e: ParseException) {
            throw MicroBlogException(e)
        }
        if (userIds.isEmpty()) {
            throw MicroBlogException("Invalid response")
        }
        return userIds.distinct().toTypedArray()
    }

}