package de.vanita5.twittnuker.api.twitter.model;

public interface StatusActivitySummary extends TwitterResponse {

	public long getDescendentReplyCount();

	public IDs getFavoriters();

	public long getFavoritersCount();

	public IDs getRepliers();

	public long getRepliersCount();

	public IDs getRetweeters();

	public long getRetweetersCount();

}