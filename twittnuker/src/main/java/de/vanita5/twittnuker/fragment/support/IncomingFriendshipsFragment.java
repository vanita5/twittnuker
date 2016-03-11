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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.otto.Subscribe;

import de.vanita5.twittnuker.adapter.AbsUsersAdapter;
import de.vanita5.twittnuker.adapter.ParcelableUsersAdapter;
import de.vanita5.twittnuker.adapter.iface.IUsersAdapter;
import de.vanita5.twittnuker.loader.support.IDsUsersLoader;
import de.vanita5.twittnuker.loader.support.IncomingFriendshipsLoader;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.message.FollowRequestTaskEvent;
import de.vanita5.twittnuker.view.holder.UserViewHolder;

import java.util.List;

public class IncomingFriendshipsFragment extends CursorSupportUsersListFragment implements IUsersAdapter.RequestClickListener {
    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public IDsUsersLoader onCreateUsersLoader(final Context context, @NonNull final Bundle args, boolean fromUser) {
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final IncomingFriendshipsLoader loader = new IncomingFriendshipsLoader(context, accountId, getData(), fromUser);
        loader.setCursor(getNextCursor());
        return loader;
    }

    @NonNull
    @Override
    protected ParcelableUsersAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableUsersAdapter adapter = super.onCreateAdapter(context, compact);
        adapter.setRequestClickListener(this);
        return adapter;
    }

    @Override
    public void onAcceptClicked(UserViewHolder holder, int position) {
        final AbsUsersAdapter<List<ParcelableUser>> adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.acceptFriendshipAsync(user.account_id, user.id);
    }

    @Override
    public void onDenyClicked(UserViewHolder holder, int position) {
        final AbsUsersAdapter<List<ParcelableUser>> adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.denyFriendshipAsync(user.account_id, user.id);
    }

    @Subscribe
    public void onFollowRequestTaskEvent(FollowRequestTaskEvent event) {
        final ParcelableUsersAdapter adapter = getAdapter();
        final int position = adapter.findPosition(event.getAccountId(), event.getUserId());
        if (event.isFinished() && event.isSucceeded()) {
            adapter.removeUserAt(position);
        } else {
            adapter.notifyItemChanged(position);
        }
    }
}