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
package de.vanita5.twittnuker.adapter.iface;

import android.content.Context;

import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;
import de.vanita5.twittnuker.util.ImageLoadingHandler;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

public interface IStatusesAdapter<Data> extends IGapSupportedAdapter, ICardSupportedAdapter {

    ImageLoaderWrapper getImageLoader();

    Context getContext();

    ImageLoadingHandler getImageLoadingHandler();

    ParcelableStatus getStatus(int position);

    int getStatusCount();

    void onStatusClick(StatusViewHolder holder, int position);

    void onUserProfileClick(StatusViewHolder holder, int position);

    void setData(Data data);
}
