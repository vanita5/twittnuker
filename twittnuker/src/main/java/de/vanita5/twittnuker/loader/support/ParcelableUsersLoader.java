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

package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.loader.iface.IExtendedLoader;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.NoDuplicatesArrayList;

import java.util.Collections;
import java.util.List;

public abstract class ParcelableUsersLoader extends AsyncTaskLoader<List<ParcelableUser>>
        implements IExtendedLoader, Constants {

    private final List<ParcelableUser> mData = Collections
            .synchronizedList(new NoDuplicatesArrayList<ParcelableUser>());
    private boolean mFromUser;

    public ParcelableUsersLoader(final Context context, final List<ParcelableUser> data, boolean fromUser) {
        super(context);
        setFromUser(fromUser);
        if (data != null) {
            mData.addAll(data);
        }
    }

    @Override
    public void setFromUser(boolean fromUser) {
        mFromUser = fromUser;
    }

    @Override
    public boolean isFromUser() {
        return mFromUser;
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

    protected List<ParcelableUser> getData() {
        return mData;
    }

    protected boolean hasId(final long id) {
        for (final ParcelableUser user : mData) {
            if (user.key.getId() == id) return true;
        }
        return false;
    }

}