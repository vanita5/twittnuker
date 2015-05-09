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
import org.mariotaku.simplerestapi.param.Path;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ReportAs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface TweetResources {
    @POST("/statuses/destroy/{id}.json")
    Status destroyStatus(@Path("id") long statusId) throws TwitterException;

	IDs getRetweetersIDs(long statusId) throws TwitterException;

    IDs getRetweetersIDs(long statusId, Paging paging) throws TwitterException;

	/**
	 * Returns up to 100 of the first retweets of a given tweet. <br>
	 * This method calls http://api.twitter.com/1.1/statuses/retweets
	 * 
	 * @param statusId The numerical ID of the tweet you want the retweets of.
	 * @return the retweets of a given tweet
	 * @throws twitter4j.TwitterException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/statuses/retweets/:id">Tweets
	 *      Resources › statuses/retweets/:id</a>
	 * @since Twitter4J 2.0.10
	 */
	ResponseList<Status> getRetweets(long statusId) throws TwitterException;

	/**
	 * Returns up to 100 of the first retweets of a given tweet. <br>
	 * This method calls http://api.twitter.com/1.1/statuses/retweets
	 * 
	 * @param statusId The numerical ID of the desired status.
	 * @param count Specifies the number of records to retrieve. Must be less
	 *            than or equal to 100.
	 * @return the retweets of a given tweet
	 * @throws twitter4j.TwitterException when Twitter service or network is unavailable
	 * @see <a
	 *      href="https://dev.twitter.com/docs/api/1.1/get/statuses/retweets/:id">Tweets
	 *      Resources › statuses/retweets/:id</a>
	 * @since Twitter4J 2.0.10
	 */
	ResponseList<Status> getRetweets(long statusId, int count) throws TwitterException;

	int reportSpam(long statusId, ReportAs reportAs, boolean blockUser) throws TwitterException;

    @POST("/statuses/retweet/{id}.json")
    Status retweetStatus(@Path("id") long statusId) throws TwitterException;

    @GET("/statuses/show.json")
    Status showStatus(@Query("id") long id) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form({"status", "in_reply_to_status_id", "possibly_sensitive", "lat",
            "long", "place_id", "display_coordinates", "media_ids"}) StatusUpdate latestStatus) throws TwitterException;

    @POST("/statuses/update.json")
    @Body(BodyType.FORM)
    Status updateStatus(@Form("status") String status) throws TwitterException;
}