package de.vanita5.twittnuker.model.util

import org.apache.commons.lang3.ArrayUtils
import de.vanita5.twittnuker.library.twitter.model.Activity
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableStatus

fun ParcelableActivity.getActivityStatus(): ParcelableStatus? {
    val status: ParcelableStatus
    if (Activity.Action.MENTION == action) {
        if (ArrayUtils.isEmpty(target_object_statuses)) return null
        status = target_object_statuses[0]
    } else if (Activity.Action.REPLY == action) {
        if (ArrayUtils.isEmpty(target_statuses)) return null
        status = target_statuses[0]
    } else if (Activity.Action.QUOTE == action) {
        if (ArrayUtils.isEmpty(target_statuses)) return null
        status = target_statuses[0]
    } else {
        return null
    }
    status.account_color = account_color
    status.user_color = status_user_color
    status.retweet_user_color = status_retweet_user_color
    status.quoted_user_color = status_quoted_user_color

    return status
}