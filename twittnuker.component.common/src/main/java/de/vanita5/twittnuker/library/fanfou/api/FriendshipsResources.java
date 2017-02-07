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

package de.vanita5.twittnuker.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;

import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.library.twitter.model.User;

@SuppressWarnings("RedundantThrows")
public interface FriendshipsResources {

    @POST("/friendships/create.json")
    User createFanfouFriendship(@Param("id") String id) throws MicroBlogException;

    @POST("/friendships/destroy.json")
    User destroyFanfouFriendship(@Param("id") String id) throws MicroBlogException;

    @POST("/friendships/accept.json")
    User acceptFanfouFriendship(@Param("id") String id) throws MicroBlogException;

    @POST("/friendships/deny.json")
    User denyFanfouFriendship(@Param("id") String id) throws MicroBlogException;

    @GET("/friendships/requests.json")
    ResponseList<User> getFriendshipsRequests(@Query Paging paging) throws MicroBlogException;

}