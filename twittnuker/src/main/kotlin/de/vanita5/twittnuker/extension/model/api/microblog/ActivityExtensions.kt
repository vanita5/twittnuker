/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.extension.model.api.microblog

import org.mariotaku.ktextension.mapToArray
import de.vanita5.microblog.library.twitter.model.Activity
import de.vanita5.microblog.library.twitter.model.Activity.Action
import de.vanita5.microblog.library.twitter.model.Status
import de.vanita5.twittnuker.extension.model.api.applyTo
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.toLite
import de.vanita5.twittnuker.extension.model.toSummaryLine
import de.vanita5.twittnuker.extension.model.updateFilterInfo
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey

inline val Activity.activityStatus: Status?
    get() = when (action) {
        Action.MENTION -> {
            targetObjectStatuses?.firstOrNull()
        }
        Action.REPLY -> {
            targetStatuses?.firstOrNull()
        }
        Action.QUOTE -> {
            targetStatuses?.firstOrNull()
        }
        else -> null
    }

fun Activity.toParcelable(details: AccountDetails, isGap: Boolean = false,
                          profileImageSize: String = "normal"): ParcelableActivity {
    return toParcelable(details.key, details.type, isGap, profileImageSize).apply {
        account_color = details.color
    }
}

fun Activity.toParcelable(accountKey: UserKey, accountType: String, isGap: Boolean = false,
                          profileImageSize: String = "normal"): ParcelableActivity {
    val result = ParcelableActivity()
    result.account_key = accountKey
    result.id = "$minPosition-$maxPosition"
    result.timestamp = createdAt.time
    result.max_sort_position = maxSortPosition
    result.min_sort_position = minSortPosition
    result.max_position = maxPosition
    result.min_position = minPosition

    result.action = action

    result.sources = sources?.mapToArray {
        it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
    }

    result.targets = ParcelableActivity.RelatedObject().also {
        it.statuses = targetStatuses?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize)
        }
        it.users = targetUsers?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
        }
        it.user_lists = targetUserLists?.mapToArray {
            it.toParcelable(accountKey, profileImageSize = profileImageSize)
        }
    }

    result.target_objects = ParcelableActivity.RelatedObject().also {
        it.statuses = targetObjectStatuses?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize)
        }
        it.users = targetObjectUsers?.mapToArray {
            it.toParcelable(accountKey, accountType, profileImageSize = profileImageSize)
        }
        it.user_lists = targetObjectUserLists?.mapToArray {
            it.toParcelable(accountKey, profileImageSize = profileImageSize)
        }
    }

    val status = activityStatus
    if (status == null) {
        when (action) {
            Action.FOLLOW -> {
                // No summary line
            }
            Action.FAVORITE -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.RETWEET -> {
                // Target objects (Statuses) as summary line
                result.summary_line = result.target_objects?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.FAVORITED_RETWEET, Action.RETWEETED_RETWEET -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.RETWEETED_MENTION, Action.FAVORITED_MENTION -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
            Action.LIST_MEMBER_ADDED -> {
                // Target objects (lists) as summary line
            }
            Action.JOINED_TWITTER -> {
                // No summary line
            }
            Action.MEDIA_TAGGED, Action.FAVORITED_MEDIA_TAGGED, Action.RETWEETED_MEDIA_TAGGED -> {
                // Targets (Statuses) as summary line
                result.summary_line = result.targets?.statuses?.mapToArray {
                    it.toSummaryLine()
                }
            }
        }
        result.user_key = result.sources?.singleOrNull()?.key ?: UserKey("multiple", null)
    } else {
        status.applyTo(accountKey, accountType, profileImageSize, result)
        result.summary_line = arrayOf(result.toSummaryLine())
    }

    result.sources_lite = result.sources?.mapToArray { it.toLite() }
    result.source_keys = result.sources_lite?.mapToArray { it.key }

    result.has_following_source = sources?.fold(false) { folded, item ->
        return@fold folded || (item.isFollowing == true)
    } ?: false
    result.is_gap = isGap

    result.updateFilterInfo()

    return result
}

private fun ParcelableUserList.toSummaryLine(): ParcelableActivity.SummaryLine {
    val result = ParcelableActivity.SummaryLine()
    result.name = name
    result.content = description
    return result
}