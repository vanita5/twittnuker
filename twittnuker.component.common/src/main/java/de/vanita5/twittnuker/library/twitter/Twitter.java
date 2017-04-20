/*
 *          Twittnuker - Twitter client for Android
 *
 *  Copyright 2013-2017 vanita5 <mail@vanit.as>
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 *  Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.vanita5.twittnuker.library.twitter;

import de.vanita5.twittnuker.library.twitter.api.DirectMessagesResources;
import de.vanita5.twittnuker.library.twitter.api.FavoritesResources;
import de.vanita5.twittnuker.library.twitter.api.FriendsFollowersResources;
import de.vanita5.twittnuker.library.twitter.api.HelpResources;
import de.vanita5.twittnuker.library.twitter.api.ListResources;
import de.vanita5.twittnuker.library.twitter.api.MutesResources;
import de.vanita5.twittnuker.library.twitter.api.PlacesGeoResources;
import de.vanita5.twittnuker.library.twitter.api.SavedSearchesResources;
import de.vanita5.twittnuker.library.twitter.api.SearchResources;
import de.vanita5.twittnuker.library.twitter.api.SpamReportingResources;
import de.vanita5.twittnuker.library.twitter.api.TimelineResources;
import de.vanita5.twittnuker.library.twitter.api.TrendsResources;
import de.vanita5.twittnuker.library.twitter.api.TweetResources;
import de.vanita5.twittnuker.library.twitter.api.UsersResources;

public interface Twitter extends SearchResources, TimelineResources, TweetResources, UsersResources,
        ListResources, DirectMessagesResources, FriendsFollowersResources, FavoritesResources,
        SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, MutesResources, TwitterPrivate {
}