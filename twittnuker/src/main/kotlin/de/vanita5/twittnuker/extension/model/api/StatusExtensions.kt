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

package de.vanita5.twittnuker.extension.model.api

import android.text.Spanned
import android.text.style.URLSpan
import org.mariotaku.ktextension.mapToArray
import de.vanita5.microblog.library.twitter.model.Status
import de.vanita5.twittnuker.extension.model.toParcelable
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.model.util.ParcelableLocationUtils
import de.vanita5.twittnuker.model.util.ParcelableMediaUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils.addFilterFlag
import de.vanita5.twittnuker.model.util.ParcelableUserMentionUtils
import de.vanita5.twittnuker.model.util.UserKeyUtils
import de.vanita5.twittnuker.util.HtmlSpanBuilder
import de.vanita5.twittnuker.util.InternalTwitterContentUtils

fun Status.toParcelable(details: AccountDetails, profileImageSize: String = "normal"): ParcelableStatus {
    return toParcelable(details.key, details.type, profileImageSize).apply {
        account_color = details.color
    }
}

fun Status.toParcelable(accountKey: UserKey, accountType: String, profileImageSize: String = "normal"): ParcelableStatus {
    val result = ParcelableStatus()
    val extras = ParcelableStatus.Extras()
    result.account_key = accountKey
    result.id = id
    result.sort_id = sortId
    result.timestamp = createdAt?.time ?: 0

    extras.external_url = inferredExternalUrl
    extras.support_entities = entities != null
    extras.statusnet_conversation_id = statusnetConversationId
    extras.conversation_id = conversationId
    result.is_pinned_status = user.pinnedTweetIds?.contains(id) ?: false

    val retweetedStatus = retweetedStatus
    result.is_retweet = isRetweet
    result.retweeted = wasRetweeted()
    val status: Status
    if (retweetedStatus != null) {
        status = retweetedStatus
        val retweetUser = user
        result.retweet_id = retweetedStatus.id
        result.retweet_timestamp = retweetedStatus.createdAt?.time ?: 0
        result.retweeted_by_user_key = UserKeyUtils.fromUser(retweetUser)
        result.retweeted_by_user_name = retweetUser.name
        result.retweeted_by_user_screen_name = retweetUser.screenName
        result.retweeted_by_user_profile_image = retweetUser.getProfileImageOfSize(profileImageSize)

        extras.retweeted_external_url = retweetedStatus.inferredExternalUrl

        if (retweetUser.isBlocking == true) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.BLOCKING_USER)
        }
        if (retweetUser.isBlockedBy == true) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.BLOCKED_BY_USER)
        }
        if (retweetedStatus.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBILITY_SENSITIVE)
        }
    } else {
        status = this
        if (status.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBILITY_SENSITIVE)
        }
    }

    val quoted = status.quotedStatus
    result.is_quote = status.isQuoteStatus
    result.quoted_id = status.quotedStatusId
    if (quoted != null) {
        val quotedUser = quoted.user
        result.quoted_id = quoted.id
        extras.quoted_external_url = quoted.inferredExternalUrl

        val quotedText = quoted.htmlText
        // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
        // We should treat this as an html
        if (quotedText.isHtml) {
            val html = HtmlSpanBuilder.fromHtml(quotedText, quoted.extendedText)
            result.quoted_text_unescaped = html?.toString()
            result.quoted_text_plain = result.quoted_text_unescaped
            result.quoted_spans = html?.spanItems
        } else {
            val textWithIndices = InternalTwitterContentUtils.formatStatusTextWithIndices(quoted)
            result.quoted_text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(quotedText)
            result.quoted_text_unescaped = textWithIndices.text
            result.quoted_spans = textWithIndices.spans
            extras.quoted_display_text_range = textWithIndices.range
        }

        result.quoted_timestamp = quoted.createdAt.time
        result.quoted_source = quoted.source
        result.quoted_media = ParcelableMediaUtils.fromStatus(quoted, accountKey, accountType)

        result.quoted_user_key = UserKeyUtils.fromUser(quotedUser)
        result.quoted_user_name = quotedUser.name
        result.quoted_user_screen_name = quotedUser.screenName
        result.quoted_user_profile_image = quotedUser.getProfileImageOfSize(profileImageSize)
        result.quoted_user_is_protected = quotedUser.isProtected
        result.quoted_user_is_verified = quotedUser.isVerified

        if (quoted.isPossiblySensitive) {
            result.addFilterFlag(ParcelableStatus.FilterFlags.POSSIBILITY_SENSITIVE)
        }
    } else if (status.isQuoteStatus) {
        result.addFilterFlag(ParcelableStatus.FilterFlags.QUOTE_NOT_AVAILABLE)
    }

    result.reply_count = status.replyCount
    result.retweet_count = status.retweetCount
    result.favorite_count = status.favoriteCount

    result.in_reply_to_name = status.inReplyToName
    result.in_reply_to_screen_name = status.inReplyToScreenName
    result.in_reply_to_status_id = status.inReplyToStatusId
    result.in_reply_to_user_key = status.getInReplyToUserKey(accountKey)

    val user = status.user
    result.user_key = UserKeyUtils.fromUser(user)
    result.user_name = user.name
    result.user_screen_name = user.screenName
    result.user_profile_image_url = user.getProfileImageOfSize(profileImageSize)
    result.user_is_protected = user.isProtected
    result.user_is_verified = user.isVerified
    result.user_is_following = user.isFollowing == true
    extras.user_statusnet_profile_url = user.statusnetProfileUrl
    extras.user_profile_image_url_fallback = user.profileImageUrlHttps ?: user.profileImageUrl
    val text = status.htmlText
    // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
    // We should treat this as an html
    if (text.isHtml) {
        val html = HtmlSpanBuilder.fromHtml(text, status.extendedText)
        result.text_unescaped = html?.toString()
        result.text_plain = result.text_unescaped
        result.spans = html?.spanItems
    } else {
        val textWithIndices = InternalTwitterContentUtils.formatStatusTextWithIndices(status)
        result.text_unescaped = textWithIndices.text
        result.text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(text)
        result.spans = textWithIndices.spans
        extras.display_text_range = textWithIndices.range
    }

    result.media = ParcelableMediaUtils.fromStatus(status, accountKey, accountType)
    result.source = status.source
    result.location = status.parcelableLocation
    result.is_favorite = status.isFavorited
    if (result.account_key.maybeEquals(result.retweeted_by_user_key)) {
        result.my_retweet_id = result.id
    } else {
        result.my_retweet_id = status.currentUserRetweet
    }
    result.is_possibly_sensitive = status.isPossiblySensitive
    result.mentions = ParcelableUserMentionUtils.fromUserMentionEntities(user,
            status.userMentionEntities)
    result.card = status.card?.toParcelable(accountKey, accountType)
    result.card_name = result.card?.name
    result.place_full_name = status.placeFullName
    result.lang = status.lang
    result.extras = extras
    return result
}

internal inline val CharSequence.spanItems get() = (this as? Spanned)?.let { text ->
    text.getSpans(0, length, URLSpan::class.java).mapToArray { SpanItem.from(text, it) }
}

internal inline val String.isHtml get() = contains('<') && contains('>')

private inline val Status.inReplyToName get() = userMentionEntities?.firstOrNull {
    inReplyToUserId == it.id
}?.name ?: attentions?.firstOrNull {
    inReplyToUserId == it.id
}?.fullName ?: inReplyToScreenName


private inline val Status.placeFullName get() = place?.fullName ?: location?.takeIf {
    ParcelableLocation.valueOf(location) == null
}

private inline val Status.inferredExternalUrl get() = externalUrl ?: uri?.let { uri ->
    noticeUriRegex.matchEntire(uri)?.let { result: MatchResult ->
        "https://${result.groups[1]?.value}/notice/${result.groups[3]?.value}"
    }
}

private val Status.parcelableLocation: ParcelableLocation?
    get() {
        val geoLocation = geoLocation
        if (geoLocation != null) {
            return ParcelableLocationUtils.fromGeoLocation(geoLocation)
        }
        val locationString = location ?: return null
        val location = ParcelableLocation.valueOf(locationString)
        if (location != null) {
            return location
        }
        return null
    }

private fun Status.getInReplyToUserKey(accountKey: UserKey): UserKey? {
    val inReplyToUserId = inReplyToUserId ?: return null
    val entities = userMentionEntities
    if (entities != null) {
        if (entities.any { inReplyToUserId == it.id }) {
            return UserKey(inReplyToUserId, accountKey.host)
        }
    }
    val attentions = attentions
    if (attentions != null) {
        attentions.firstOrNull { inReplyToUserId == it.id }?.let {
            val host = UserKeyUtils.getUserHost(it.ostatusUri,
                    accountKey.host)
            return UserKey(inReplyToUserId, host)
        }
    }
    return UserKey(inReplyToUserId, accountKey.host)
}

private val noticeUriRegex = Regex("tag:([\\w\\d.]+),(\\d{4}-\\d{2}-\\d{2}):noticeId=(\\d+):objectType=(\\w+)")