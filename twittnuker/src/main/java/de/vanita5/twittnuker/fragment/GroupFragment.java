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

package de.vanita5.twittnuker.fragment;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.SupportTabsAdapter;
import de.vanita5.twittnuker.api.MicroBlog;
import de.vanita5.twittnuker.api.statusnet.model.Group;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.model.ParcelableGroup;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.ParcelableGroupUtils;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.Utils;

public class GroupFragment extends AbsToolbarTabPagesFragment implements
        LoaderCallbacks<SingleResponse<ParcelableGroup>> {
    private ParcelableGroup mGroup;
    private boolean mGroupLoaderInitialized;

    @Override
    protected void addTabs(SupportTabsAdapter adapter) {
        final Bundle args = getArguments();
        adapter.addTab(GroupTimelineFragment.class, args, getString(R.string.statuses), 0, 0, "statuses");
        adapter.addTab(GroupMembersFragment.class, args, getString(R.string.members), 0, 1, "members");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utils.setNdefPushMessageCallback(getActivity(), new NfcAdapter.CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableGroup group = getGroup();
                if (group == null || group.url == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(group.url),
                });
            }
        });

        getGroupInfo(false);
    }

    @Override
    public Loader<SingleResponse<ParcelableGroup>> onCreateLoader(int id, Bundle args) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String groupId = args.getString(EXTRA_GROUP_ID);
        final String groupName = args.getString(EXTRA_GROUP_NAME);
        final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
        return new ParcelableGroupLoader(getContext(), omitIntentExtra, getArguments(), accountKey,
                groupId, groupName);
    }

    @Override
    public void onLoadFinished(Loader<SingleResponse<ParcelableGroup>> loader, SingleResponse<ParcelableGroup> data) {
        if (data.hasData()) {
            displayGroup(data.getData());
        }
    }

    @Override
    public void onLoaderReset(Loader<SingleResponse<ParcelableGroup>> loader) {

    }

    public void displayGroup(final ParcelableGroup group) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        getLoaderManager().destroyLoader(0);
        mGroup = group;

        if (group != null) {
            activity.setTitle(group.fullname);
        } else {
            activity.setTitle(R.string.user_list);
        }
        invalidateOptionsMenu();
    }


    public void getGroupInfo(final boolean omitIntentExtra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(0);
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
        if (!mGroupLoaderInitialized) {
            lm.initLoader(0, args, this);
            mGroupLoaderInitialized = true;
        } else {
            lm.restartLoader(0, args, this);
        }
    }

    public ParcelableGroup getGroup() {
        return mGroup;
    }

    static class ParcelableGroupLoader extends AsyncTaskLoader<SingleResponse<ParcelableGroup>> {

        private final boolean mOmitIntentExtra;
        private final Bundle mExtras;
        private final UserKey mAccountKey;
        private final String mGroupId;
        private final String mGroupName;

        private ParcelableGroupLoader(final Context context, final boolean omitIntentExtra,
                                      final Bundle extras, final UserKey accountKey,
                                      final String groupId, final String groupName) {
            super(context);
            mOmitIntentExtra = omitIntentExtra;
            mExtras = extras;
            mAccountKey = accountKey;
            mGroupId = groupId;
            mGroupName = groupName;
        }

        @Override
        public SingleResponse<ParcelableGroup> loadInBackground() {
            if (!mOmitIntentExtra && mExtras != null) {
                final ParcelableGroup cache = mExtras.getParcelable(EXTRA_GROUP);
                if (cache != null) return SingleResponse.getInstance(cache);
            }
            final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(getContext(), mAccountKey,
                    true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Group group;
                if (mGroupId != null) {
                    group = twitter.showGroup(mGroupId);
                } else if (mGroupName != null) {
                    group = twitter.showGroupByName(mGroupName);
                } else {
                    return SingleResponse.getInstance();
                }
                return SingleResponse.getInstance(ParcelableGroupUtils.from(group, mAccountKey, 0,
                        group.isMember()));
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        public void onStartLoading() {
            forceLoad();
        }

    }
}