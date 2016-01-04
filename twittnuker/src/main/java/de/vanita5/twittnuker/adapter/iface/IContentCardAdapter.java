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

package de.vanita5.twittnuker.adapter.iface;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.text.BidiFormatter;

import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.view.ShapedImageView.ShapeStyle;

public interface IContentCardAdapter extends ILoadMoreSupportAdapter {

    @NonNull
    Context getContext();

    @NonNull
    UserColorNameManager getUserColorNameManager();

    int getItemCount();

    @ShapeStyle
    int getProfileImageStyle();

    boolean isProfileImageEnabled();

    float getTextSize();

    @NonNull
    AsyncTwitterWrapper getTwitterWrapper();

    @NonNull
    MediaLoaderWrapper getMediaLoader();

    @NonNull
    BidiFormatter getBidiFormatter();
}