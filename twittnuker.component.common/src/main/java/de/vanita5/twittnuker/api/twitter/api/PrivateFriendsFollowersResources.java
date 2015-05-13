package de.vanita5.twittnuker.api.twitter.api;

import de.vanita5.twittnuker.api.twitter.model.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;

public interface PrivateFriendsFollowersResources extends PrivateResources {

	public User acceptFriendship(long userId) throws TwitterException;

	public User acceptFriendship(String screenName) throws TwitterException;

	public User denyFriendship(long userId) throws TwitterException;

	public User denyFriendship(String screenName) throws TwitterException;

}
