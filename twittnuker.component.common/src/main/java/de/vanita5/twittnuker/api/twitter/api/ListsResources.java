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
import org.mariotaku.simplerestapi.param.Query;

import de.vanita5.twittnuker.api.twitter.model.PageableResponseList;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.api.twitter.model.UserList;
import de.vanita5.twittnuker.api.twitter.model.UserListUpdate;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface ListsResources {
    @POST("/lists/members/create.json")
    @Body(BodyType.FORM)
    UserList addUserListMember(@Query("list_id") long listId, @Query("user_id") long userId) throws TwitterException;

    @POST("/lists/members/create.json")
    @Body(BodyType.FORM)
    UserList addUserListMember(@Query("list_id") long listId, @Query("screen_name") String userScreenName) throws TwitterException;

    @POST("/lists/members/create_all.json")
    @Body(BodyType.FORM)
    UserList addUserListMembers(@Form("list_id") long listId, @Form("user_id") long[] userIds) throws TwitterException;

    @POST("/lists/members/create_all.json")
    @Body(BodyType.FORM)
    UserList addUserListMembers(@Form("list_id") long listId, @Form("screen_name") String[] screenNames) throws TwitterException;

    @POST("/lists/create.json")
    @Body(BodyType.FORM)
    UserList createUserList(@Form UserListUpdate update) throws TwitterException;

    UserList createUserListSubscription(@Query("list_id") long listId) throws TwitterException;

    UserList deleteUserListMember(@Query("list_id") long listId, @Query("user_id") long userId) throws TwitterException;

    UserList deleteUserListMember(@Query("list_id") long listId, String screenName) throws TwitterException;

    UserList deleteUserListMembers(@Query("list_id") long listId, long[] userIds) throws TwitterException;

    UserList deleteUserListMembers(@Query("list_id") long listId, String[] screenNames) throws TwitterException;

    UserList destroyUserList(@Query("list_id") long listId) throws TwitterException;

    UserList destroyUserListSubscription(@Query("list_id") long listId) throws TwitterException;

    @GET("/lists/members.json")
    PageableResponseList<User> getUserListMembers(@Query("list_id") long listId, @Query Paging paging) throws TwitterException;

    @GET("/lists/members.json")
    PageableResponseList<User> getUserListMembers(@Query("slug") String slug, @Query("owner_id") long ownerId, @Query Paging paging)
			throws TwitterException;

    @GET("/lists/members.json")
    PageableResponseList<User> getUserListMembers(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
			throws TwitterException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query Paging paging) throws TwitterException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("user_id") long listMemberId, @Query Paging paging) throws TwitterException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("user_id") long listMemberId, @Query Paging paging,
                                                          @Query("filter_to_owned_lists") boolean filterToOwnedLists) throws TwitterException;

    @GET("/lists/memberships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("screen_name") String listMemberScreenName, @Query Paging paging)
			throws TwitterException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListMemberships(@Query("screen_name") String listMemberScreenName, @Query Paging paging,
			boolean filterToOwnedLists) throws TwitterException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnerships(@Query Paging paging) throws TwitterException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnerships(@Query("user_id") long listMemberId, @Query Paging paging) throws TwitterException;

    @GET("/lists/ownerships.json")
    PageableResponseList<UserList> getUserListOwnerships(@Query("screen_name") String listMemberScreenName, @Query Paging paging)
            throws TwitterException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserLists(@Query("user_id") long userId, @Query("reverse") boolean reverse) throws TwitterException;

    @GET("/lists/list.json")
    ResponseList<UserList> getUserLists(@Query("screen_name") String screenName, @Query("reverse") boolean reverse) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("list_id") long listId, @Query Paging paging) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("slug") String slug, @Query("owner_id") long ownerId, @Query Paging paging) throws TwitterException;

    @GET("/lists/statuses.json")
    ResponseList<Status> getUserListStatuses(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
			throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") long listId, @Query Paging paging) throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String slug, @Query("owner_id") long ownerId, @Query Paging paging)
			throws TwitterException;

    @GET("/lists/subscribers.json")
    PageableResponseList<User> getUserListSubscribers(@Query("list_id") String slug, @Query("owner_screen_name") String ownerScreenName, @Query Paging paging)
			throws TwitterException;


    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptions(@Query("screen_name") String listOwnerScreenName, long cursor)
			throws TwitterException;

    @GET("/lists/subscriptions.json")
    PageableResponseList<UserList> getUserListSubscriptions(@Query("user_id") long userId, long cursor)
            throws TwitterException;

    @GET("/lists/show.json")
    UserList showUserList(@Query("list_id") long listId) throws TwitterException;

    @GET("/lists/show.json")
    UserList showUserList(@Query("slug") String slug, @Query("owner_id") long ownerId) throws TwitterException;

    @GET("/lists/show.json")
    UserList showUserList(@Query("slug") String slug, @Query("owner_screen_name") String ownerScreenName) throws TwitterException;

    @POST("/lists/update.json")
    @Body(BodyType.FORM)
    UserList updateUserList(@Query("list_id") long listId, @Form UserListUpdate update) throws TwitterException;
}