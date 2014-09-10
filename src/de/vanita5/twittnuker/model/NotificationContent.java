package de.vanita5.twittnuker.model;

/**
 * This represents a single (Push) Notification.
 * BigView Notifications contain multiple NotificationContent Models!
 */
public class NotificationContent {

	public static final String NOTIFICATION_TYPE_MENTION = "type_mention";
	public static final String NOTIFICATION_TYPE_RETWEET = "type_retweet";
	public static final String NOTIFICATION_TYPE_FOLLOWER = "type_new_follower";
	public static final String NOTIFICATION_TYPE_FAVORITE = "type_favorite";
	public static final String NOTIFICATION_TYPE_DIRECT_MESSAGE = "type_direct_message";

	public static final String NOTIFICATION_TYPE_ERROR_420 = "type_error_420";

	private long accountId;
	private long timestamp;

	private String fromUser;
	private String message;
	private String type;
	private String profileImageUrl;

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

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		else if (!(o instanceof NotificationContent)) return false;
		else if (this.accountId != ((NotificationContent) o).accountId)
			return false;
		else if (this.fromUser != null
			&& !this.fromUser.equals(((NotificationContent) o).fromUser))
			return false;
		else if (this.message != null
			&& !this.message.equals(((NotificationContent) o).message))
			return false;
		else if (this.type != null
			&& !this.type.equals(((NotificationContent) o).type))
			return false;
		else if (this.profileImageUrl != null
			&& !this.profileImageUrl.equals(((NotificationContent) o).profileImageUrl))
			return false;
		return true;
	}
}
