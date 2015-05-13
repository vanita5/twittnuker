package de.vanita5.twittnuker.api.twitter.api;

import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;

public interface PrivateActivityResources extends PrivateResources {
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException;

	public ResponseList<Activity> getActivitiesAboutMe(Paging paging) throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends(Paging paging) throws TwitterException;
}
