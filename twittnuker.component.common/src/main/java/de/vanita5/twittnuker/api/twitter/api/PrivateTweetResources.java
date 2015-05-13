package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.StatusActivitySummary;
import de.vanita5.twittnuker.api.twitter.model.TranslationResult;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;

public interface PrivateTweetResources extends PrivateResources {

    StatusActivitySummary getStatusActivitySummary(@Query("id") long statusId) throws TwitterException;

    StatusActivitySummary getStatusActivitySummary(long statusId, boolean includeUserEntities) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId, @Query Paging paging) throws TwitterException;

    @GET("/translations/show.json")
    TranslationResult showTranslation(@Query("id") long statusId, @Query("dest") String dest) throws TwitterException;
}