/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.DirectMessage;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;

@SuppressWarnings("RedundantThrows")
@Queries({@KeyValue(key = "full_text", valueKey = "full_text"),
        @KeyValue(key = "include_entities", valueKey = "include_entities"),
        @KeyValue(key = "include_cards", valueKey = "include_cards"),
        @KeyValue(key = "cards_platform", valueKey = "cards_platform")})
public interface DirectMessagesResources {

    @POST("/direct_messages/destroy.json")
    @BodyType(BodyType.FORM)
    DirectMessage destroyDirectMessage(@Param("id") long id) throws TwitterException;

    @GET("/direct_messages.json")
    ResponseList<DirectMessage> getDirectMessages(@Query Paging paging) throws TwitterException;

    @GET("/direct_messages/sent.json")
    ResponseList<DirectMessage> getSentDirectMessages(@Query Paging paging) throws TwitterException;

    @POST("/direct_messages/new.json")
    @BodyType(BodyType.FORM)
    DirectMessage sendDirectMessage(@Param("user_id") long userId, @Param("text") String text)
            throws TwitterException;

    @POST("/direct_messages/new.json")
    @BodyType(BodyType.FORM)
    DirectMessage sendDirectMessage(@Param("user_id") long userId, @Param("text") String text,
                                    @Param("media_id") long mediaId) throws TwitterException;

    @POST("/direct_messages/new.json")
    @BodyType(BodyType.FORM)
    DirectMessage sendDirectMessage(@Param("screen_name") String screenName, @Param("text") String text)
            throws TwitterException;

    @POST("/direct_messages/new.json")
    @BodyType(BodyType.FORM)
    DirectMessage sendDirectMessage(@Param("screen_name") String screenName, @Param("text") String text,
                                    @Param("media_id") long mediaId) throws TwitterException;

    @GET("/direct_messages/show.json")
    DirectMessage showDirectMessage(@Query("id") long id) throws TwitterException;
}