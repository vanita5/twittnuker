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

package de.vanita5.twittnuker.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        CustomTabType.HOME_TIMELINE,
        CustomTabType.NOTIFICATIONS_TIMELINE,
        CustomTabType.TRENDS_SUGGESTIONS,
        CustomTabType.DIRECT_MESSAGES,
        CustomTabType.FAVORITES,
        CustomTabType.USER_TIMELINE,
        CustomTabType.SEARCH_STATUSES,
        CustomTabType.LIST_TIMELINE,
})
@Retention(RetentionPolicy.SOURCE)
public @interface CustomTabType {
    String HOME_TIMELINE = "home_timeline";
    String NOTIFICATIONS_TIMELINE = "notifications_timeline";
    String TRENDS_SUGGESTIONS = "trends_suggestions";
    String DIRECT_MESSAGES = "direct_messages";
    String DIRECT_MESSAGES_NEXT = "direct_messages_next";
    String FAVORITES = "favorites";
    String USER_TIMELINE = "user_timeline";
    String SEARCH_STATUSES = "search_statuses";
    String LIST_TIMELINE = "list_timeline";
}