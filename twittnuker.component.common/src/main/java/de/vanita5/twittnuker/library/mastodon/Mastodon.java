/*
 *          Twittnuker - Twitter client for Android
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.vanita5.twittnuker.library.mastodon;

import de.vanita5.twittnuker.library.mastodon.api.AccountResources;
import de.vanita5.twittnuker.library.mastodon.api.ApplicationResources;
import de.vanita5.twittnuker.library.mastodon.api.BlockResources;
import de.vanita5.twittnuker.library.mastodon.api.FavouriteResources;
import de.vanita5.twittnuker.library.mastodon.api.FollowRequestResources;
import de.vanita5.twittnuker.library.mastodon.api.FollowResources;
import de.vanita5.twittnuker.library.mastodon.api.InstanceResources;
import de.vanita5.twittnuker.library.mastodon.api.MediaResources;
import de.vanita5.twittnuker.library.mastodon.api.MuteResources;
import de.vanita5.twittnuker.library.mastodon.api.NotificationResources;
import de.vanita5.twittnuker.library.mastodon.api.ReportResources;
import de.vanita5.twittnuker.library.mastodon.api.SearchResources;
import de.vanita5.twittnuker.library.mastodon.api.StatusResources;
import de.vanita5.twittnuker.library.mastodon.api.TimelineResources;

public interface Mastodon extends AccountResources, ApplicationResources, BlockResources,
        FavouriteResources, FollowRequestResources, FollowResources, InstanceResources,
        MediaResources, MuteResources, NotificationResources, ReportResources, SearchResources,
        StatusResources, TimelineResources {

}