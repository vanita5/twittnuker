/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util;

import static de.vanita5.twittnuker.util.Utils.openImage;
import static de.vanita5.twittnuker.util.Utils.openStatus;
import static de.vanita5.twittnuker.util.Utils.openTweetSearch;
import static de.vanita5.twittnuker.util.Utils.openUserListDetails;
import static de.vanita5.twittnuker.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.TwidereLinkify.OnLinkClickListener;

import static de.vanita5.twittnuker.util.shortener.TweetShortenerUtils.expandHototin;


public class OnLinkClickHandler implements OnLinkClickListener, Constants {

	protected final Activity activity;
    protected final MultiSelectManager manager;

    public OnLinkClickHandler(final Context context, final MultiSelectManager manager) {
		activity = context instanceof Activity ? (Activity) context : null;
        this.manager = manager;
	}

	@Override
	public void onLinkClick(final String link, final String orig, final long account_id, final int type,
			final boolean sensitive) {
		
		if (activity == null || manager.isActive()) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION: {
				openUserProfile(activity, account_id, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(activity, account_id, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				openImage(activity, link, sensitive);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK: {
				openLink(link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LIST: {
				final String[] mention_list = link.split("\\/");
				if (mention_list == null || mention_list.length != 2) {
					break;
				}
				openUserListDetails(activity, account_id, -1, -1, mention_list[0], mention_list[1]);
				break;
			}
			case TwidereLinkify.LINK_TYPE_CASHTAG: {
				openTweetSearch(activity, account_id, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_USER_ID: {
				openUserProfile(activity, account_id, ParseUtils.parseLong(link), null);
				break;
			}
			case TwidereLinkify.LINK_TYPE_STATUS: {
				openStatus(activity, account_id, ParseUtils.parseLong(link));
				break;
			}
			case TwidereLinkify.LINK_TYPE_HOTOTIN: {
				expandHototin(activity, link);
				break;
			}
		}
	}

	protected void openLink(final String link) {
        if (activity == null || manager.isActive()) return;
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}
}
