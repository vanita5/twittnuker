/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.view.holder;

import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.MessageConversationAdapter;
import de.vanita5.twittnuker.model.ParcelableDirectMessage;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;

public class IncomingMessageViewHolder extends MessageViewHolder {

	private final ImageView profileImageView;

	public IncomingMessageViewHolder(MessageConversationAdapter adapter, View itemView) {
		super(adapter, itemView);
		profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
	}

	@Override
	public void displayMessage(Cursor cursor, ParcelableDirectMessage.CursorIndices indices) {
		super.displayMessage(cursor, indices);
		final MediaLoaderWrapper wrapper = adapter.getMediaLoader();
		if (adapter.isProfileImageEnabled()) {
			profileImageView.setVisibility(View.VISIBLE);
			wrapper.displayProfileImage(profileImageView, cursor.getString(indices.sender_profile_image_url));
		} else {
			profileImageView.setVisibility(View.GONE);
			wrapper.cancelDisplayTask(profileImageView);
		}
	}


}