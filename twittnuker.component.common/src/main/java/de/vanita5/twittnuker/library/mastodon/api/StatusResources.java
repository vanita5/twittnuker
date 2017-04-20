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

package de.vanita5.twittnuker.library.mastodon.api;

import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.mastodon.model.Account;
import de.vanita5.twittnuker.library.mastodon.model.Card;
import de.vanita5.twittnuker.library.mastodon.model.Context;
import de.vanita5.twittnuker.library.mastodon.model.Status;
import de.vanita5.twittnuker.library.mastodon.model.StatusUpdate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;

import java.util.List;


public interface StatusResources {
    @GET("/v1/statuses/{id}")
    Status fetchStatus(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/context")
    Context getStatusContext(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/card")
    Card getStatusCard(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/reblogged_by")
    List<Account> getStatusRebloggedBy(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/favourited_by")
    List<Account> getStatusFavouritedBy(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses")
    Status postStatus(@Param StatusUpdate update) throws MicroBlogException;
}