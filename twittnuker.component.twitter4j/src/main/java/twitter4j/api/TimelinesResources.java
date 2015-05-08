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

package twitter4j.api;

import org.mariotaku.simplerestapi.param.Query;
import org.mariotaku.simplerestapi.method.GET;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface TimelinesResources {

    @GET("/statuses/home_timeline.json")
	ResponseList<Status> getHomeTimeline() throws TwitterException;

    @GET("/statuses/home_timeline.json")
    ResponseList<Status> getHomeTimeline(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

    @GET("/statuses/mentions_timeline.json")
	ResponseList<Status> getMentionsTimeline() throws TwitterException;

    @GET("/statuses/mentions_timeline.json")
    ResponseList<Status> getMentionsTimeline(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

    @GET("/statuses/retweets_of_me.json")
	ResponseList<Status> getRetweetsOfMe() throws TwitterException;

    @GET("/statuses/retweets_of_me.json")
    ResponseList<Status> getRetweetsOfMe(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
	ResponseList<Status> getUserTimeline() throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("user_id") long userId) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("user_id") long userId, @Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("screen_name") String screenName) throws TwitterException;

    @GET("/statuses/user_timeline.json")
    ResponseList<Status> getUserTimeline(@Query("screen_name") String screenName, @Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;
}