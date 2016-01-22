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

package de.vanita5.twittnuker.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.vanita5.twittnuker.R;

public class LoadIndicatorViewHolder extends RecyclerView.ViewHolder {
    private final View loadProgress;

    public LoadIndicatorViewHolder(View view) {
        super(view);
        loadProgress = view.findViewById(R.id.load_progress);
    }

    public void setLoadProgressVisible(boolean visible) {
        loadProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}