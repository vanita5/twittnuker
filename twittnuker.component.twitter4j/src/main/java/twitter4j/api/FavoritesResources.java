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

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface FavoritesResources {
	/**
	 * Favorites the status specified in the ID parameter as the authenticating
	 * user. Returns the favorite status when successful. <br>
	 * This method calls http://api.twitter.com/1.1/favorites/create/[id].json
	 * 
	 * @param id the ID of the status to favorite
	 * @return Status
	 * @throws twitter4j.TwitterException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/post/favorites/create/:id">POST
	 *      favorites/create/:id | Twitter Developers</a>
	 */
	Status createFavorite(long id) throws TwitterException;

	/**
	 * Un-favorites the status specified in the ID parameter as the
	 * authenticating user. Returns the un-favorited status in the requested
	 * format when successful. <br>
	 * This method calls http://api.twitter.com/1.1/favorites/destroy/[id].json
	 * 
	 * @param id the ID of the status to un-favorite
	 * @return Status
	 * @throws twitter4j.TwitterException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/post/favorites/destroy/:id">POST
	 *      favorites/destroy/:id | Twitter Developers</a>
	 */
	Status destroyFavorite(long id) throws TwitterException;

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