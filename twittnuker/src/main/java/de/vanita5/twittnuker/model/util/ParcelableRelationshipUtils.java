package de.vanita5.twittnuker.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.vanita5.twittnuker.library.twitter.model.Relationship;
import de.vanita5.twittnuker.model.ParcelableRelationship;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.UserKey;

public class ParcelableRelationshipUtils {

    public static ParcelableRelationship create(@NonNull UserKey accountKey, @NonNull UserKey userKey,
                                                @Nullable Relationship relationship, boolean filtering) {
        ParcelableRelationship obj = new ParcelableRelationship();
        obj.account_key = accountKey;
        obj.user_key = userKey;
        if (relationship != null) {
            obj.following = relationship.isSourceFollowingTarget();
            obj.followed_by = relationship.isSourceFollowedByTarget();
            obj.blocking = relationship.isSourceBlockingTarget();
            obj.blocked_by = relationship.isSourceBlockedByTarget();
            obj.muting = relationship.isSourceMutingTarget();
            obj.retweet_enabled = relationship.isSourceWantRetweetsFromTarget();
            obj.notifications_enabled = relationship.isSourceNotificationsEnabledForTarget();
            obj.can_dm = relationship.canSourceDMTarget();
        }
        obj.filtering = filtering;
        return obj;
    }

    public static ParcelableRelationship create(ParcelableUser user, boolean filtering) {
        ParcelableRelationship obj = new ParcelableRelationship();
        obj.account_key = user.account_key;
        obj.user_key = user.key;
        obj.filtering = filtering;
        if (user.extras != null) {
            obj.following = user.is_following;
            obj.followed_by = user.extras.followed_by;
            obj.blocking = user.extras.blocking;
            obj.blocked_by = user.extras.blocked_by;
            obj.can_dm = user.extras.followed_by;
        }
        return obj;
    }
}