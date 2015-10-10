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

package de.vanita5.twittnuker.util.dagger;

import android.support.v7.widget.RecyclerView;

import de.vanita5.twittnuker.activity.BasePreferenceActivity;
import de.vanita5.twittnuker.activity.BaseThemedActivity;
import de.vanita5.twittnuker.activity.support.BaseAppCompatActivity;
import de.vanita5.twittnuker.activity.support.ThemedFragmentActivity;
import de.vanita5.twittnuker.adapter.AccountsAdapter;
import de.vanita5.twittnuker.adapter.AccountsSpinnerAdapter;
import de.vanita5.twittnuker.adapter.BaseAdapter;
import de.vanita5.twittnuker.adapter.BaseArrayAdapter;
import de.vanita5.twittnuker.adapter.DraftsAdapter;
import de.vanita5.twittnuker.adapter.UserHashtagAutoCompleteAdapter;
import de.vanita5.twittnuker.fragment.BaseFragment;
import de.vanita5.twittnuker.fragment.BaseListFragment;
import de.vanita5.twittnuker.fragment.support.BaseSupportDialogFragment;
import de.vanita5.twittnuker.fragment.support.BaseSupportFragment;
import de.vanita5.twittnuker.provider.TwidereCommandProvider;
import de.vanita5.twittnuker.provider.TwidereDataProvider;
import de.vanita5.twittnuker.service.BackgroundOperationService;
import de.vanita5.twittnuker.service.RefreshService;
import de.vanita5.twittnuker.task.ManagedAsyncTask;
import de.vanita5.twittnuker.util.MultiSelectEventHandler;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;

import dagger.Component;

@Component(modules = ApplicationModule.class)
public interface GeneralComponent {
    void inject(StatusViewHolder.DummyStatusHolderAdapter object);

    void inject(BaseFragment object);

    void inject(BaseSupportFragment object);

    void inject(MultiSelectEventHandler object);

    void inject(BasePreferenceActivity object);

    void inject(BaseThemedActivity object);

    void inject(BaseSupportDialogFragment object);

    void inject(RefreshService object);

    void inject(ThemedFragmentActivity object);

    void inject(TwidereCommandProvider object);

    void inject(TwidereDataProvider object);

    void inject(BaseListFragment object);

    void inject(BaseAppCompatActivity object);

    void inject(BackgroundOperationService object);

    void inject(BaseAdapter<RecyclerView.ViewHolder> object);

    void inject(AccountsAdapter object);

    void inject(UserHashtagAutoCompleteAdapter object);

    void inject(AccountsSpinnerAdapter object);

    void inject(BaseArrayAdapter<Object> object);

    void inject(DraftsAdapter object);

    void inject(ManagedAsyncTask<Object, Object, Object> object);
}