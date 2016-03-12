/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import de.vanita5.twittnuker.adapter.iface.ContentCardClickListener;
import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.view.CardMediaContainer;

public interface IStatusViewHolder extends CardMediaContainer.OnMediaClickListener {
    void displayStatus(ParcelableStatus status, boolean displayInReplyTo);

    void displayStatus(@NonNull ParcelableStatus status,
                       boolean displayInReplyTo, boolean shouldDisplayExtraType);

    @Nullable
    ImageView getProfileImageView();

    @Nullable
    ImageView getProfileTypeView();

    @Override
    void onMediaClick(View view, ParcelableMedia media, AccountKey accountKey, long extraId);

    void setStatusClickListener(StatusClickListener listener);

    void setTextSize(float textSize);

    void playLikeAnimation(LikeAnimationDrawable.OnLikedListener listener);

    interface StatusClickListener extends ContentCardClickListener {

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition);

        void onStatusClick(IStatusViewHolder holder, int position);

        boolean onStatusLongClick(IStatusViewHolder holder, int position);

        void onUserProfileClick(IStatusViewHolder holder, int position);
    }

    abstract class SimpleStatusClickListener implements StatusClickListener {

        public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {

        }

        public void onStatusClick(IStatusViewHolder holder, int position) {

        }

        public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
            return false;
        }

        public void onUserProfileClick(IStatusViewHolder holder, int position) {

        }

        @Override
        public void onItemActionClick(RecyclerView.ViewHolder holder, int id, int position) {

        }

        @Override
        public void onItemMenuClick(RecyclerView.ViewHolder holder, View menuView, int position) {

        }
    }
}