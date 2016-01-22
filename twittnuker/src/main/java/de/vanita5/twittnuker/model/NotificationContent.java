/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.model;

import de.vanita5.twittnuker.api.twitter.model.User;

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
    public static final String NOTIFICATION_TYPE_QUOTE = "type_quote";

    public static final String NOTIFICATION_TYPE_ERROR_420 = "type_error_420";

    private long accountId;
    private long timestamp;

    private String objectId;
    private String objectUserId;
    private String fromUser;
    private String message;
    private String type;
    private String profileImageUrl;

    private User sourceUser;
    private ParcelableStatus originalStatus;
    private ParcelableDirectMessage originalMessage;

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

    public ParcelableStatus getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(ParcelableStatus originalStatus) {
        this.originalStatus = originalStatus;
    }

    public ParcelableDirectMessage getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(ParcelableDirectMessage originalMessage) {
        this.originalMessage = originalMessage;
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationContent that = (NotificationContent) o;

        if (accountId != that.accountId) return false;
        if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null)
            return false;
        if (objectUserId != null ? !objectUserId.equals(that.objectUserId) : that.objectUserId != null)
            return false;
        if (fromUser != null ? !fromUser.equals(that.fromUser) : that.fromUser != null)
            return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return !(type != null ? !type.equals(that.type) : that.type != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + (objectUserId != null ? objectUserId.hashCode() : 0);
        result = 31 * result + (fromUser != null ? fromUser.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public String getObjectUserId() {
        return objectUserId;
    }

    public void setObjectUserId(String objectUserId) {
        this.objectUserId = objectUserId;
    }
}
