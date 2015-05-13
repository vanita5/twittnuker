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

package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Path;
import org.mariotaku.simplerestapi.param.Query;

import de.vanita5.twittnuker.api.twitter.model.IDs;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.StatusUpdate;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface TweetResources {
    @POST("/statuses/destroy/{id}.json")
    Status destroyStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/retweeters/ids.json")
    IDs getRetweetersIDs(@Query("id") long statusId, @Query Paging paging) throws TwitterException;

    @GET("/statuses/retweets/{id}.json")
    ResponseList<Status> getRetweets(@Path("id") long statusId, @Query Paging paging) throws TwitterException;

    @POST("/statuses/retweet/{id}.json")
    Status retweetStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/show.json")
    Status showStatus(@Query("id") long id) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form StatusUpdate latestStatus) throws TwitterException;

}