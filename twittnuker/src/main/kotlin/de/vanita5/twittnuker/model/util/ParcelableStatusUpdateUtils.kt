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

package de.vanita5.twittnuker.model.util

import android.accounts.AccountManager
import android.content.Context
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.unique_id_non_null
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableStatusUpdate
import de.vanita5.twittnuker.model.draft.QuoteStatusActionExtras
import de.vanita5.twittnuker.model.draft.StatusObjectActionExtras
import de.vanita5.twittnuker.model.draft.UpdateStatusActionExtras
import de.vanita5.twittnuker.util.LinkCreator

object ParcelableStatusUpdateUtils {

    fun fromDraftItem(context: Context, draft: Draft): ParcelableStatusUpdate {
        val statusUpdate = ParcelableStatusUpdate()
        statusUpdate.accounts = draft.account_keys?.let {
            AccountUtils.getAllAccountDetails(AccountManager.get(context), it, true)
        } ?: emptyArray()
        statusUpdate.text = draft.text
        statusUpdate.location = draft.location
        statusUpdate.media = draft.media
        val actionExtras = draft.action_extras
        when (actionExtras) {
            is UpdateStatusActionExtras -> {
                statusUpdate.in_reply_to_status = actionExtras.inReplyToStatus
                statusUpdate.is_possibly_sensitive = actionExtras.isPossiblySensitive
                statusUpdate.display_coordinates = actionExtras.displayCoordinates
                statusUpdate.attachment_url = actionExtras.attachmentUrl
                statusUpdate.excluded_reply_user_ids = actionExtras.excludedReplyUserIds
                statusUpdate.extended_reply_mode = actionExtras.isExtendedReplyMode
                statusUpdate.summary  = actionExtras.summaryText
                statusUpdate.visibility  = actionExtras.visibility
            }
            is QuoteStatusActionExtras -> {
                val onlyAccount = statusUpdate.accounts.singleOrNull()
                val status = actionExtras.status
                val quoteOriginalStatus = actionExtras.isQuoteOriginalStatus
                if (status != null && onlyAccount != null) {
                    when (onlyAccount.type) {
                        AccountType.FANFOU -> {
                            if (!status.is_quote || !quoteOriginalStatus) {
                                statusUpdate.repost_status_id = status.id
                                statusUpdate.text = context.getString(R.string.fanfou_repost_format,
                                        draft.text, status.user_screen_name, status.text_plain)
                            } else {
                                statusUpdate.text = context.getString(R.string.fanfou_repost_format,
                                        draft.text, status.quoted_user_screen_name,
                                        status.quoted_text_plain)
                                statusUpdate.repost_status_id = status.quoted_id
                            }
                        }
                        else -> {
                            val statusLink = if (!status.is_quote || !quoteOriginalStatus) {
                                LinkCreator.getStatusWebLink(status)
                            } else {
                                LinkCreator.getQuotedStatusWebLink(status)
                            }
                            statusUpdate.attachment_url = statusLink.toString()
                            statusUpdate.text = draft.text
                        }
                    }
                }
            }
            is StatusObjectActionExtras -> {
                when (draft.action_type) {
                    Draft.Action.QUOTE -> {
                        val link = LinkCreator.getStatusWebLink(actionExtras.status)
                        statusUpdate.attachment_url = link.toString()
                    }
                }
            }
        }
        statusUpdate.draft_action = draft.action_type
        statusUpdate.draft_unique_id = draft.unique_id_non_null
        return statusUpdate
    }

}