package de.vanita5.twittnuker.model;

/**
 * This represents a single Push Notification received from the backend server.
 * BigView Notifications contain multiple PushNotificationContent Models!
 */
public class PushNotificationContent {

	public static final String PUSH_NOTIFICATION_TYPE_MENTION = "type_mention";
	public static final String PUSH_NOTIFICATION_TYPE_RETWEET = "type_retweet";
	public static final String PUSH_NOTIFICATION_TYPE_FOLLOWER = "type_new_follower";
	public static final String PUSH_NOTIFICATION_TYPE_FAVORITE = "type_favorite";

	private long accountId;
	private long timestamp;

	private String fromUser;
	private String message;
	private String type;

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
