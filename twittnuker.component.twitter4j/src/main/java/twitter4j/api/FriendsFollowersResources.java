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

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Friendship;
import twitter4j.IDs;
import twitter4j.PageableResponseList;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface FriendsFollowersResources {

	User createFriendship(long userId) throws TwitterException;

	User createFriendship(long userId, boolean follow) throws TwitterException;

	User createFriendship(String screenName) throws TwitterException;

	User createFriendship(String screenName, boolean follow) throws TwitterException;

	User destroyFriendship(long userId) throws TwitterException;

	User destroyFriendship(String screenName) throws TwitterException;

	IDs getFollowersIDs(Paging paging) throws TwitterException;

	IDs getFollowersIDs(long userId, Paging paging) throws TwitterException;

	IDs getFollowersIDs(String screenName, Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/followers/list.json")
    PageableResponseList<User> getFollowersList(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

	IDs getFriendsIDs(Paging paging) throws TwitterException;

	IDs getFriendsIDs(long userId, Paging paging) throws TwitterException;

	IDs getFriendsIDs(String screenName, Paging paging) throws TwitterException;

	PageableResponseList<User> getFriendsList(Paging paging) throws TwitterException;

	PageableResponseList<User> getFriendsList(long userId, Paging paging) throws TwitterException;

	PageableResponseList<User> getFriendsList(String screenName, Paging paging) throws TwitterException;

	IDs getIncomingFriendships(Paging paging) throws TwitterException;

	IDs getOutgoingFriendships(Paging paging) throws TwitterException;

	ResponseList<Friendship> lookupFriendships(long[] ids) throws TwitterException;

	ResponseList<Friendship> lookupFriendships(String[] screenNames) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("source_id") long sourceId, @Query("target_id") long targetId) throws TwitterException;

    @GET("/friendships/show.json")
    Relationship showFriendship(@Query("target_id") long targetId) throws TwitterException;

	Relationship showFriendship(String sourceScreenName, String targetScreenName) throws TwitterException;

	Relationship updateFriendship(long userId, boolean enableDeviceNotification, boolean retweets)
			throws TwitterException;

	Relationship updateFriendship(String screenName, boolean enableDeviceNotification, boolean retweets)
			throws TwitterException;
}