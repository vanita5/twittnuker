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

package de.vanita5.microblog.library.mastodon;

import de.vanita5.microblog.library.mastodon.api.AccountsResources;
import de.vanita5.microblog.library.mastodon.api.AppsResources;
import de.vanita5.microblog.library.mastodon.api.BlocksResources;
import de.vanita5.microblog.library.mastodon.api.FavouritesResources;
import de.vanita5.microblog.library.mastodon.api.FollowRequestsResources;
import de.vanita5.microblog.library.mastodon.api.FollowsResources;
import de.vanita5.microblog.library.mastodon.api.InstancesResources;
import de.vanita5.microblog.library.mastodon.api.MediaResources;
import de.vanita5.microblog.library.mastodon.api.MutesResources;
import de.vanita5.microblog.library.mastodon.api.NotificationsResources;
import de.vanita5.microblog.library.mastodon.api.ReportsResources;
import de.vanita5.microblog.library.mastodon.api.SearchResources;
import de.vanita5.microblog.library.mastodon.api.StatusesResources;
import de.vanita5.microblog.library.mastodon.api.StreamingResources;
import de.vanita5.microblog.library.mastodon.api.TimelinesResources;

public interface Mastodon extends AccountsResources, AppsResources, BlocksResources,
        FavouritesResources, FollowRequestsResources, FollowsResources, InstancesResources,
        MediaResources, MutesResources, NotificationsResources, ReportsResources, SearchResources,
        StatusesResources, TimelinesResources, StreamingResources {

}