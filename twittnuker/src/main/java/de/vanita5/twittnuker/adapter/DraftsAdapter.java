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

package de.vanita5.twittnuker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.DraftItemCursorIndices;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableMediaUpdate;
import de.vanita5.twittnuker.provider.TwidereDataStore.Drafts;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.MediaLoadingHandler;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.ApplicationModule;
import de.vanita5.twittnuker.util.dagger.DaggerGeneralComponent;
import de.vanita5.twittnuker.view.holder.DraftViewHolder;

import javax.inject.Inject;

import static de.vanita5.twittnuker.util.Utils.getAccountColors;

public class DraftsAdapter extends SimpleCursorAdapter implements Constants {

    @Inject
    MediaLoaderWrapper mImageLoader;
    @Inject
    SharedPreferencesWrapper mPreferences;
    private final MediaLoadingHandler mMediaLoadingHandler;
    private final int mMediaPreviewStyle;

    private float mTextSize;
    private DraftItemCursorIndices mIndices;

    public DraftsAdapter(final Context context) {
        super(context, R.layout.list_item_draft, null, new String[0], new int[0], 0);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(context)).build().inject(this);
        mMediaLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final DraftViewHolder holder = (DraftViewHolder) view.getTag();
        final long[] accountIds = TwidereArrayUtils.parseLongArray(cursor.getString(mIndices.account_ids), ',');
        final String text = cursor.getString(mIndices.text);
        final ParcelableMediaUpdate[] mediaUpdates = ParcelableMediaUpdate.fromJSONString(cursor.getString(mIndices.media));
        final long timestamp = cursor.getLong(mIndices.timestamp);
        final int actionType = cursor.getInt(mIndices.action_type);
        final String actionName = getActionName(context, actionType);
        holder.media_preview_container.setStyle(mMediaPreviewStyle);
        if (actionType == Drafts.ACTION_UPDATE_STATUS) {
            final ParcelableMedia[] media = ParcelableMedia.fromMediaUpdates(mediaUpdates);
            holder.media_preview_container.setVisibility(View.VISIBLE);
            holder.media_preview_container.displayMedia(media, mImageLoader, -1L, null, mMediaLoadingHandler);
        } else {
            holder.media_preview_container.setVisibility(View.GONE);
        }
        holder.content.drawEnd(getAccountColors(context, accountIds));
        holder.setTextSize(mTextSize);
        final boolean emptyContent = TextUtils.isEmpty(text);
        if (emptyContent) {
            holder.text.setText(R.string.empty_content);
        } else {
            holder.text.setText(text);
        }
        holder.text.setTypeface(holder.text.getTypeface(), emptyContent ? Typeface.ITALIC : Typeface.NORMAL);

        if (timestamp > 0) {
            final String timeString = Utils.formatSameDayTime(context, timestamp);
            holder.time.setText(context.getString(R.string.action_name_saved_at_time, actionName, timeString));
        } else {
            holder.time.setText(actionName);
        }
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        final Object tag = view.getTag();
        if (!(tag instanceof DraftViewHolder)) {
            view.setTag(new DraftViewHolder(view));
        }
        return view;
    }

    public void setTextSize(final float text_size) {
        mTextSize = text_size;
    }

    @Override
    public Cursor swapCursor(final Cursor c) {
        final Cursor old = super.swapCursor(c);
        if (c != null) {
            mIndices = new DraftItemCursorIndices(c);
        }
        return old;
    }

    private static String getActionName(final Context context, final int actionType) {
        if (actionType <= 0) return context.getString(R.string.update_status);
        switch (actionType) {
            case Drafts.ACTION_UPDATE_STATUS:
                return context.getString(R.string.update_status);
            case Drafts.ACTION_SEND_DIRECT_MESSAGE:
                return context.getString(R.string.send_direct_message);
        }
        return null;
    }
}