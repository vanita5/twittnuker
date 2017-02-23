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
import de.vanita5.twittnuker.extension.model.unique_id_non_null
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableStatusUpdate
import de.vanita5.twittnuker.model.draft.UpdateStatusActionExtras

object ParcelableStatusUpdateUtils {

    fun fromDraftItem(context: Context, draft: Draft): ParcelableStatusUpdate {
        val statusUpdate = ParcelableStatusUpdate()
        statusUpdate.accounts = draft.account_keys?.let {
            AccountUtils.getAllAccountDetails(AccountManager.get(context), it, true)
        } ?: emptyArray()
        statusUpdate.text = draft.text
        statusUpdate.location = draft.location
        statusUpdate.media = draft.media
        if (draft.action_extras is UpdateStatusActionExtras) {
            val extra = draft.action_extras as UpdateStatusActionExtras?
            statusUpdate.in_reply_to_status = extra!!.inReplyToStatus
            statusUpdate.is_possibly_sensitive = extra.isPossiblySensitive
            statusUpdate.display_coordinates = extra.displayCoordinates
            statusUpdate.attachment_url = extra.attachmentUrl
        }
        statusUpdate.draft_action = draft.action_type
        statusUpdate.draft_unique_id = draft.unique_id_non_null
        return statusUpdate
    }

}