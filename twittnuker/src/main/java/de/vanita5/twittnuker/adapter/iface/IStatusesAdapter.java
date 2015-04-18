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
package de.vanita5.twittnuker.adapter.iface;

import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.MediaLoadingHandler;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.view.CardMediaContainer.PreviewStyle;
import de.vanita5.twittnuker.view.holder.StatusViewHolder.StatusClickListener;

public interface IStatusesAdapter<Data> extends IContentCardAdapter, StatusClickListener,
        IGapSupportedAdapter, ContentCardClickListener {

    int getLinkHighlightingStyle();

    @PreviewStyle
    int getMediaPreviewStyle();

    ParcelableStatus getStatus(int position);

    long getStatusId(int position);

    int getStatusesCount();

    TwidereLinkify getTwidereLinkify();

    boolean isCardActionsHidden();

    boolean isMediaPreviewEnabled();

    boolean isNameFirst();

    boolean isSensitiveContentEnabled();

    void setData(Data data);

    boolean shouldShowAccountsColor();

    MediaLoadingHandler getMediaLoadingHandler();
}