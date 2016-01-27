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
package de.vanita5.twittnuker.adapter.iface;

import android.support.annotation.Nullable;
import android.view.View;

import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.MediaLoadingHandler;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.view.CardMediaContainer.PreviewStyle;
import de.vanita5.twittnuker.view.holder.GapViewHolder;
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder;

public interface IStatusesAdapter<Data> extends IContentCardAdapter, IGapSupportedAdapter {

    int getLinkHighlightingStyle();

    @PreviewStyle
    int getMediaPreviewStyle();

    ParcelableStatus getStatus(int position);

    long getStatusId(int position);

    long getAccountId(int position);

    @Nullable
    ParcelableStatus findStatusById(long accountId, long statusId);

    int getStatusesCount();

    TwidereLinkify getTwidereLinkify();

    boolean isCardActionsHidden();

    boolean isMediaPreviewEnabled();

    boolean isNameFirst();

    boolean isSensitiveContentEnabled();

    void setData(Data data);

    boolean shouldShowAccountsColor();

    boolean shouldUseStarsForLikes();

    MediaLoadingHandler getMediaLoadingHandler();

    @Nullable
    IStatusViewHolder.StatusClickListener getStatusClickListener();

    @Nullable
    StatusAdapterListener getStatusAdapterListener();

    interface StatusAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition);

        void onStatusActionClick(IStatusViewHolder holder, int id, int position);

        void onStatusClick(IStatusViewHolder holder, int position);

        boolean onStatusLongClick(IStatusViewHolder holder, int position);

        void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position);

        void onUserProfileClick(IStatusViewHolder holder, ParcelableStatus status, int position);
    }
}