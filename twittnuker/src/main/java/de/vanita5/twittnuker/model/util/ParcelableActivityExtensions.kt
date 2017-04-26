package de.vanita5.twittnuker.model.util

import de.vanita5.microblog.library.twitter.model.Activity
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableStatus

fun ParcelableActivity.getActivityStatus(): ParcelableStatus? {
    val status: ParcelableStatus
    when (action) {
        Activity.Action.MENTION -> {
            if (target_object_statuses?.isEmpty() ?: true) return null
            status = target_object_statuses[0]
        }
        Activity.Action.REPLY -> {
            if (target_statuses?.isEmpty() ?: true) return null
            status = target_statuses[0]
        }
        Activity.Action.QUOTE -> {
            if (target_statuses?.isEmpty() ?: true) return null
            status = target_statuses[0]
        }
        else -> return null
    }
    status.account_color = account_color
    return status
}