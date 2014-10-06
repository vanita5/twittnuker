package de.vanita5.twittnuker.model;

/**
 * Represents an account on the backend server
 */
public class GoogleAccountItem {

	public String accountId;

	public boolean nMentions;
	public boolean nDMs;
	public boolean nFollower;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public boolean showMentions() {
		return nMentions;
	}

	public void setShowMentions(boolean nMentions) {
		this.nMentions = nMentions;
	}

	public boolean showDMs() {
		return nDMs;
	}

	public void setShowDMs(boolean nDMs) {
		this.nDMs = nDMs;
	}

	public boolean showFollower() {
		return nFollower;
	}

	public void setShowFollower(boolean nFollower) {
		this.nFollower = nFollower;
	}
}
