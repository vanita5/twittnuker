/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vanita5.twittnuker.api.twitter;

import de.vanita5.twittnuker.api.twitter.api.DirectMessagesResources;
import de.vanita5.twittnuker.api.twitter.api.FavoritesResources;
import de.vanita5.twittnuker.api.twitter.api.FriendsFollowersResources;
import de.vanita5.twittnuker.api.twitter.api.HelpResources;
import de.vanita5.twittnuker.api.twitter.api.ListsResources;
import de.vanita5.twittnuker.api.twitter.api.PlacesGeoResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateActivityResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateDirectMessagesResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateFriendsFollowersResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateTimelinesResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateTweetResources;
import de.vanita5.twittnuker.api.twitter.api.SavedSearchesResources;
import de.vanita5.twittnuker.api.twitter.api.SearchResource;
import de.vanita5.twittnuker.api.twitter.api.SpamReportingResources;
import de.vanita5.twittnuker.api.twitter.api.TimelinesResources;
import de.vanita5.twittnuker.api.twitter.api.TrendsResources;
import de.vanita5.twittnuker.api.twitter.api.TweetResources;
import de.vanita5.twittnuker.api.twitter.api.UsersResources;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface Twitter extends SearchResource, TimelinesResources,
		TweetResources, UsersResources, ListsResources, DirectMessagesResources, FriendsFollowersResources,
		FavoritesResources, SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, PrivateActivityResources, PrivateTweetResources, PrivateTimelinesResources,
        PrivateFriendsFollowersResources, PrivateDirectMessagesResources {
}