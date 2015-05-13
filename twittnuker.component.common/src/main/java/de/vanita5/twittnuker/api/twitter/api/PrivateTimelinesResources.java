package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;

public interface PrivateTimelinesResources extends PrivateResources {

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline() throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("user_id") long userId) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("user_id") long userId, @Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("screen_name") String screenName) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("screen_name") String screenName,@Query({"since_id", "max_id", "count", "page"})  Paging paging) throws TwitterException;
}