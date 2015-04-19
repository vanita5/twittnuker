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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.View;

import de.vanita5.twittnuker.adapter.AbsUsersAdapter;
import de.vanita5.twittnuker.adapter.AbsUsersAdapter.UserAdapterListener;
import de.vanita5.twittnuker.loader.iface.IExtendedLoader;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.holder.UserViewHolder;

abstract class AbsUsersFragment<Data> extends AbsContentListFragment<AbsUsersAdapter<Data>> implements LoaderCallbacks<Data>, UserAdapterListener {

	public final Data getData() {
		return getAdapter().getData();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        getAdapter().setListener(this);

		final Bundle loaderArgs = new Bundle(getArguments());
		loaderArgs.putBoolean(EXTRA_FROM_USER, true);
		getLoaderManager().initLoader(0, loaderArgs, this);
	}

	@Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateUsersLoader(getActivity(), args, fromUser);
    }

    @Override
    public void onLoadFinished(Loader<Data> loader, Data data) {
		final AbsUsersAdapter<Data> adapter = getAdapter();
		adapter.setData(data);
		if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
			adapter.setLoadMoreSupported(hasMoreData(data));
			setRefreshEnabled(true);
		}
		if (loader instanceof IExtendedLoader) {
			((IExtendedLoader) loader).setFromUser(false);
		}
		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Data> loader) {
		if (loader instanceof IExtendedLoader) {
			((IExtendedLoader) loader).setFromUser(false);
		}
	}

    @Override
    public void onUserClick(UserViewHolder holder, int position) {
        final ParcelableUser user = getAdapter().getUser(position);
        final FragmentActivity activity = getActivity();
        final View profileImageView = holder.getProfileImageView();
        final View profileTypeView = holder.getProfileTypeView();
        final Bundle options = Utils.makeSceneTransitionOption(activity,
				new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
				new Pair<>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
        Utils.openUserProfile(activity, user.account_id, user.id, user.screen_name, options);
    }


    @Override
    public boolean onUserLongClick(UserViewHolder holder, int position) {

        return true;
    }

	protected ParcelableUser getSelectedUser() {
		//TODO return selected
		return null;
	}

    protected abstract boolean hasMoreData(Data data);

    protected abstract Loader<Data> onCreateUsersLoader(Context context, Bundle args, boolean fromUser);

}