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

package de.vanita5.twittnuker.library.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;
import de.vanita5.twittnuker.library.statusnet.model.Group;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.library.twitter.model.Status;
import de.vanita5.twittnuker.library.twitter.model.User;

@SuppressWarnings("RedundantThrows")
public interface GroupResources {

    @GET("/statusnet/groups/timeline/{group_id}.json")
    ResponseList<Status> getGroupStatuses(@Path("group_id") String groupId, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/timeline/{group_name}.json")
    ResponseList<Status> getGroupStatusesByName(@Path("group_name") String name, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/show.json")
    Group showGroup(@Query("group_id") String groupId) throws MicroBlogException;

    @GET("/statusnet/groups/show.json")
    Group showGroupByName(@Query("group_name") String groupName) throws MicroBlogException;

    @GET("/statusnet/groups/membership.json")
    ResponseList<User> getGroupMembers(@Query("group_id") String groupId, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/membership.json")
    ResponseList<User> getGroupMembersByName(@Query("group_name") String groupName, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/list.json")
    ResponseList<Group> getGroupsByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;

    @GET("/statusnet/groups/list.json")
    ResponseList<Group> getGroups(@Query("id") String userId) throws MicroBlogException;

    @GET("/statusnet/groups/list_all.json")
    ResponseList<Group> getAllGroups(@Query Paging paging) throws MicroBlogException;

}