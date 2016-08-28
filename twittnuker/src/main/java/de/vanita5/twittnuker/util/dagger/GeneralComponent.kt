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

package de.vanita5.twittnuker.util.dagger

import android.support.v7.widget.RecyclerView
import dagger.Component
import de.vanita5.twittnuker.activity.APIEditorActivity
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.activity.ComposeActivity
import de.vanita5.twittnuker.activity.MediaViewerActivity
import de.vanita5.twittnuker.adapter.*
import de.vanita5.twittnuker.fragment.*
import de.vanita5.twittnuker.loader.MicroBlogAPIStatusesLoader
import de.vanita5.twittnuker.loader.ParcelableStatusLoader
import de.vanita5.twittnuker.loader.ParcelableUserLoader
import de.vanita5.twittnuker.preference.AccountsListPreference
import de.vanita5.twittnuker.preference.KeyboardShortcutPreference
import de.vanita5.twittnuker.provider.CacheProvider
import de.vanita5.twittnuker.provider.TwidereDataProvider
import de.vanita5.twittnuker.service.BackgroundOperationService
import de.vanita5.twittnuker.service.RefreshService
import de.vanita5.twittnuker.task.*
import de.vanita5.twittnuker.task.twitter.GetActivitiesTask
import de.vanita5.twittnuker.task.twitter.GetStatusesTask
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.text.util.EmojiEditableFactory
import de.vanita5.twittnuker.text.util.EmojiSpannableFactory
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.MultiSelectEventHandler
import de.vanita5.twittnuker.util.NotificationHelper
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface GeneralComponent {
    fun inject(adapter: DummyItemAdapter)

    fun inject(`object`: BaseSupportFragment)

    fun inject(`object`: MultiSelectEventHandler)

    fun inject(`object`: BaseDialogFragment)

    fun inject(`object`: RefreshService)

    fun inject(`object`: ComposeActivity)

    fun inject(`object`: TwidereDataProvider)

    fun inject(`object`: BaseListFragment)

    fun inject(`object`: BaseActivity)

    fun inject(`object`: BackgroundOperationService)

    fun inject(`object`: BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)

    fun inject(`object`: AccountsAdapter)

    fun inject(`object`: ComposeAutoCompleteAdapter)

    fun inject(`object`: UserAutoCompleteAdapter)

    fun inject(`object`: AccountsSpinnerAdapter)

    fun inject(`object`: BaseArrayAdapter<Any>)

    fun inject(`object`: DraftsAdapter)

    fun inject(`object`: ManagedAsyncTask<Any, Any, Any>)

    fun inject(`object`: BasePreferenceFragment)

    fun inject(`object`: BaseFiltersFragment.FilteredUsersFragment.FilterUsersListAdapter)

    fun inject(`object`: EmojiSpannableFactory)

    fun inject(`object`: EmojiEditableFactory)

    fun inject(`object`: AccountsListPreference.AccountItemPreference)

    fun inject(`object`: MessagesConversationFragment.SetReadStateTask)

    fun inject(`object`: DependencyHolder)

    fun inject(provider: CacheProvider)

    fun inject(loader: MicroBlogAPIStatusesLoader)

    fun inject(activity: MediaViewerActivity)

    fun inject(task: GetStatusesTask)

    fun inject(task: GetActivitiesTask)

    fun inject(task: GetDirectMessagesTask)

    fun inject(`object`: NotificationHelper)

    fun inject(task: AbsFriendshipOperationTask)

    fun inject(preference: KeyboardShortcutPreference)

    fun inject(loader: ParcelableUserLoader)

    fun inject(loader: ParcelableStatusLoader)

    fun inject(task: GetTrendsTask)

    fun inject(task: UpdateProfileBackgroundImageTask<Any>)

    fun inject(task: UpdateProfileBannerImageTask<Any>)

    fun inject(task: AsyncTwitterWrapper.UpdateProfileImageTask<Any>)

    fun inject(loader: APIEditorActivity.LoadDefaultsChooserDialogFragment.DefaultAPIConfigLoader)

    fun inject(task: UpdateStatusTask)
}