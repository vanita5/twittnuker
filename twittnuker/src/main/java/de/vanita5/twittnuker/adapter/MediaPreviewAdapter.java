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
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;

import java.util.Collection;

public class MediaPreviewAdapter extends ArrayAdapter<String> implements Constants {

	private final MediaLoaderWrapper mImageLoader;
	private final SharedPreferences mPreferences;
	private final ImageLoadingHandler mImageLoadingHandler;

	private boolean mIsPossiblySensitive;

	public MediaPreviewAdapter(final Context context) {
		super(context, R.layout.gallery_item_image_preview);
		mImageLoader = ((TwittnukerApplication) context.getApplicationContext()).getMediaLoaderWrapper();
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mImageLoadingHandler = new ImageLoadingHandler();
	}

	public void addAll(final Collection<String> data, final boolean is_possibly_sensitive) {
		mIsPossiblySensitive = is_possibly_sensitive;
		addAll(data);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final String link = getItem(position);
		final ImageView image_view = (ImageView) view.findViewById(R.id.media_preview_item);
		image_view.setTag(link);
		if (mIsPossiblySensitive && !mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, true)) {
			view.findViewById(R.id.media_preview_progress).setVisibility(View.GONE);
			image_view.setBackgroundResource(R.drawable.image_preview_nsfw);
			mImageLoader.cancelDisplayTask(image_view);
		} else if (!link.equals(mImageLoadingHandler.getLoadingUri(image_view))) {
			image_view.setBackgroundResource(0);
			mImageLoader.displayPreviewImage(image_view, link, mImageLoadingHandler);
		}
		return view;
	}

}