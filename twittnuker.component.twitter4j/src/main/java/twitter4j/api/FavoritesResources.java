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

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface FavoritesResources {

    @POST("/favorites/create.json")
    @Body(BodyType.FORM)
    Status createFavorite(@Form("id") long id) throws TwitterException;

    @POST("/favorites/destroy.json")
    @Body(BodyType.FORM)
    Status destroyFavorite(@Form("id") long id) throws TwitterException;

    @GET("/favorites/list.json")
	ResponseList<Status> getFavorites() throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("user_id") long userId) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("user_id") long userId, @Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("screen_name") String screenName) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("screen_name") String screenName, @Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;
}