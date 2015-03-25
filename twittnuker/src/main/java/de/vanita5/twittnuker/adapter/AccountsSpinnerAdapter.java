/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
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

package de.vanita5.twittnuker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.support.DirectMessagesConversationFragment;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;

import java.util.Collection;

public class AccountsSpinnerAdapter extends ArrayAdapter<ParcelableAccount> {

	private final MediaLoaderWrapper mImageLoader;
	private final boolean mDisplayProfileImage;

	public AccountsSpinnerAdapter(final Context context) {
        this(context, R.layout.list_item_user);
    }

    public AccountsSpinnerAdapter(final Context context, int itemViewResource) {
        super(context, itemViewResource);
		mImageLoader = TwittnukerApplication.getInstance(context).getImageLoaderWrapper();
		mDisplayProfileImage = context.getSharedPreferences(DirectMessagesConversationFragment.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getBoolean(DirectMessagesConversationFragment.KEY_DISPLAY_PROFILE_IMAGE, true);
	}

    public AccountsSpinnerAdapter(final Context context, final Collection<ParcelableAccount> accounts) {
		this(context);
		addAll(accounts);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).account_id;
	}

	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getDropDownView(position, convertView, parent);
		bindView(view, getItem(position));
		return view;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		bindView(view, getItem(position));
		return view;
	}

    private void bindView(final View view, final ParcelableAccount item) {
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
        if (text2 != null) {
		    text2.setVisibility(item.is_dummy ? View.GONE : View.VISIBLE);
        }
        if (icon != null) {
		    icon.setVisibility(item.is_dummy ? View.GONE : View.VISIBLE);
        }
		if (!item.is_dummy) {
            if (text1 != null) {
			    text1.setText(item.name);
            }
            if (text2 != null) {
			    text2.setText(String.format("@%s", item.screen_name));
            }
            if (icon != null) {
                if (mDisplayProfileImage) {
                    mImageLoader.displayProfileImage(icon, item.profile_image_url);
                } else {
                    mImageLoader.cancelDisplayTask(icon);
//                    icon.setImageResource(R.drawable.ic_profile_image_default);
                }
            }
        } else if (text1 != null) {
			text1.setText(R.string.none);
		}
	}

}