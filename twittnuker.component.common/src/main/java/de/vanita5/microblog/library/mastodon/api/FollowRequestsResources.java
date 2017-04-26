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

package de.vanita5.microblog.library.mastodon.api;

import de.vanita5.microblog.library.MicroBlogException;
import de.vanita5.microblog.library.mastodon.model.Account;
import de.vanita5.microblog.library.mastodon.model.LinkHeaderList;
import de.vanita5.microblog.library.twitter.model.Paging;
import de.vanita5.microblog.library.twitter.model.ResponseCode;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;

public interface FollowRequestsResources {
    @GET("/v1/follow_requests")
    LinkHeaderList<Account> getFollowRequests(@Query Paging paging) throws MicroBlogException;

    @POST("/v1/follow_requests/{id}/authorize")
    ResponseCode authorizeFollowRequest(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/follow_requests/{id}/reject")
    ResponseCode rejectFollowRequest(@Path("id") String id) throws MicroBlogException;

}