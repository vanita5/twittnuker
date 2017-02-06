/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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