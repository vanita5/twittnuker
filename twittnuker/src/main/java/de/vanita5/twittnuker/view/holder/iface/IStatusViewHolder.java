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

package de.vanita5.twittnuker.view.holder.iface;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import de.vanita5.twittnuker.adapter.iface.ContentCardClickListener;
import de.vanita5.twittnuker.api.twitter.model.TranslationResult;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.view.CardMediaContainer;

public interface IStatusViewHolder extends CardMediaContainer.OnMediaClickListener {
    void displayStatus(ParcelableStatus status, boolean displayInReplyTo);

    void displayStatus(@NonNull ParcelableStatus status, @Nullable TranslationResult translation,
                       boolean displayInReplyTo, boolean shouldDisplayExtraType);

    @Nullable
    ImageView getProfileImageView();

    @Nullable
    ImageView getProfileTypeView();

    void onClick(View v);

    boolean onLongClick(View v);

    @Override
    void onMediaClick(View view, ParcelableMedia media, long accountId);

    void setStatusClickListener(StatusClickListener listener);

    void setTextSize(float textSize);

    interface StatusClickListener extends ContentCardClickListener {

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusClick(IStatusViewHolder holder, int position);

        boolean onStatusLongClick(IStatusViewHolder holder, int position);

        void onUserProfileClick(IStatusViewHolder holder, int position);
    }
}