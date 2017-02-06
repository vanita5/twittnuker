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

package de.vanita5.twittnuker.util.linkhandler

import android.content.UriMatcher
import android.net.Uri
import de.vanita5.twittnuker.Constants.*

object TwidereLinkMatcher {

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY_STATUS, null, LINK_ID_STATUS)
        addURI(AUTHORITY_USER, null, LINK_ID_USER)
        addURI(AUTHORITY_USER_TIMELINE, null, LINK_ID_USER_TIMELINE)
        addURI(AUTHORITY_USER_MEDIA_TIMELINE, null, LINK_ID_USER_MEDIA_TIMELINE)
        addURI(AUTHORITY_USER_FOLLOWERS, null, LINK_ID_USER_FOLLOWERS)
        addURI(AUTHORITY_USER_FRIENDS, null, LINK_ID_USER_FRIENDS)
        addURI(AUTHORITY_USER_FAVORITES, null, LINK_ID_USER_FAVORITES)
        addURI(AUTHORITY_USER_BLOCKS, null, LINK_ID_USER_BLOCKS)
        addURI(AUTHORITY_DIRECT_MESSAGES_CONVERSATION, null,
                LINK_ID_DIRECT_MESSAGES_CONVERSATION)
        addURI(AUTHORITY_DIRECT_MESSAGES, null, LINK_ID_DIRECT_MESSAGES)
        addURI(AUTHORITY_INTERACTIONS, null, LINK_ID_INTERACTIONS)
        addURI(AUTHORITY_PUBLIC_TIMELINE, null, LINK_ID_PUBLIC_TIMELINE)
        addURI(AUTHORITY_USER_LIST, null, LINK_ID_USER_LIST)
        addURI(AUTHORITY_GROUP, null, LINK_ID_GROUP)
        addURI(AUTHORITY_USER_LIST_TIMELINE, null, LINK_ID_USER_LIST_TIMELINE)
        addURI(AUTHORITY_USER_LIST_MEMBERS, null, LINK_ID_USER_LIST_MEMBERS)
        addURI(AUTHORITY_USER_LIST_SUBSCRIBERS, null, LINK_ID_USER_LIST_SUBSCRIBERS)
        addURI(AUTHORITY_USER_LIST_MEMBERSHIPS, null, LINK_ID_USER_LIST_MEMBERSHIPS)
        addURI(AUTHORITY_USER_LISTS, null, LINK_ID_USER_LISTS)
        addURI(AUTHORITY_USER_GROUPS, null, LINK_ID_USER_GROUPS)
        addURI(AUTHORITY_SAVED_SEARCHES, null, LINK_ID_SAVED_SEARCHES)
        addURI(AUTHORITY_USER_MENTIONS, null, LINK_ID_USER_MENTIONS)
        addURI(AUTHORITY_INCOMING_FRIENDSHIPS, null, LINK_ID_INCOMING_FRIENDSHIPS)
        addURI(AUTHORITY_ITEMS, null, LINK_ID_ITEMS)
        addURI(AUTHORITY_STATUS_RETWEETERS, null, LINK_ID_STATUS_RETWEETERS)
        addURI(AUTHORITY_STATUS_FAVORITERS, null, LINK_ID_STATUS_FAVORITERS)
        addURI(AUTHORITY_SEARCH, null, LINK_ID_SEARCH)
        addURI(AUTHORITY_MUTES_USERS, null, LINK_ID_MUTES_USERS)
        addURI(AUTHORITY_MAP, null, LINK_ID_MAP)
        addURI(AUTHORITY_SCHEDULED_STATUSES, null, LINK_ID_SCHEDULED_STATUSES)

        addURI(AUTHORITY_ACCOUNTS, null, LINK_ID_ACCOUNTS)
        addURI(AUTHORITY_DRAFTS, null, LINK_ID_DRAFTS)
        addURI(AUTHORITY_FILTERS, null, LINK_ID_FILTERS)
        addURI(AUTHORITY_FILTERS, PATH_FILTERS_IMPORT_BLOCKS, LINK_ID_FILTERS_IMPORT_BLOCKS)
        addURI(AUTHORITY_FILTERS, PATH_FILTERS_IMPORT_MUTES, LINK_ID_FILTERS_IMPORT_MUTES)
        addURI(AUTHORITY_FILTERS, PATH_FILTERS_SUBSCRIPTIONS, LINK_ID_FILTERS_SUBSCRIPTIONS)
        addURI(AUTHORITY_FILTERS, PATH_FILTERS_SUBSCRIPTIONS_ADD, LINK_ID_FILTERS_SUBSCRIPTIONS_ADD)
        addURI(AUTHORITY_PROFILE_EDITOR, null, LINK_ID_PROFILE_EDITOR)
    }

    fun match(uri: Uri): Int {
        return matcher.match(uri)
    }
}