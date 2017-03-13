/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.activity.ComposeActivity
import de.vanita5.twittnuker.activity.MediaViewerActivity
import de.vanita5.twittnuker.activity.PremiumDashboardActivity
import de.vanita5.twittnuker.adapter.*
import de.vanita5.twittnuker.app.TwittnukerApplication
import de.vanita5.twittnuker.fragment.*
import de.vanita5.twittnuker.fragment.filter.FilteredUsersFragment
import de.vanita5.twittnuker.fragment.media.ExoPlayerPageFragment
import de.vanita5.twittnuker.loader.*
import de.vanita5.twittnuker.preference.AccountsListPreference
import de.vanita5.twittnuker.preference.KeyboardShortcutPreference
import de.vanita5.twittnuker.preference.PremiumEntryPreference
import de.vanita5.twittnuker.preference.PremiumEntryPreferenceCategory
import de.vanita5.twittnuker.preference.sync.SyncItemPreference
import de.vanita5.twittnuker.provider.CacheProvider
import de.vanita5.twittnuker.provider.TwidereDataProvider
import de.vanita5.twittnuker.service.*
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.task.ManagedAsyncTask
import de.vanita5.twittnuker.text.util.EmojiEditableFactory
import de.vanita5.twittnuker.text.util.EmojiSpannableFactory
import de.vanita5.twittnuker.util.MultiSelectEventHandler
import de.vanita5.twittnuker.util.NotificationHelper
import de.vanita5.twittnuker.util.filter.UrlFiltersSubscriptionProvider
import de.vanita5.twittnuker.util.sync.SyncTaskRunner
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface GeneralComponent {
    fun inject(adapter: DummyItemAdapter)

    fun inject(obj: BaseFragment)

    fun inject(obj: MultiSelectEventHandler)

    fun inject(obj: BaseDialogFragment)

    fun inject(obj: LegacyTaskService)

    fun inject(obj: ComposeActivity)

    fun inject(obj: TwidereDataProvider)

    fun inject(obj: BaseActivity)

    fun inject(obj: BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)

    fun inject(obj: AccountDetailsAdapter)

    fun inject(obj: ComposeAutoCompleteAdapter)

    fun inject(obj: UserAutoCompleteAdapter)

    fun inject(obj: AccountsSpinnerAdapter)

    fun inject(obj: BaseArrayAdapter<Any>)

    fun inject(obj: DraftsAdapter)

    fun inject(obj: ManagedAsyncTask<Any, Any, Any>)

    fun inject(obj: BasePreferenceFragment)

    fun inject(obj: FilteredUsersFragment.FilterUsersListAdapter)

    fun inject(obj: EmojiSpannableFactory)

    fun inject(obj: EmojiEditableFactory)

    fun inject(obj: AccountsListPreference.AccountItemPreference)

    fun inject(obj: DependencyHolder)

    fun inject(provider: CacheProvider)

    fun inject(loader: MicroBlogAPIStatusesLoader)

    fun inject(activity: MediaViewerActivity)

    fun inject(service: JobTaskService)

    fun inject(obj: NotificationHelper)

    fun inject(task: BaseAbstractTask<Any, Any, Any>)

    fun inject(preference: KeyboardShortcutPreference)

    fun inject(loader: ParcelableUserLoader)

    fun inject(loader: ParcelableStatusLoader)

    fun inject(loader: APIEditorDialogFragment.LoadDefaultsChooserDialogFragment.DefaultAPIConfigLoader)

    fun inject(application: TwittnukerApplication)

    fun inject(fragment: ThemedPreferenceDialogFragmentCompat)

    fun inject(service: BaseIntentService)

    fun inject(runner: SyncTaskRunner)

    fun inject(preference: SyncItemPreference)

    fun inject(provider: UrlFiltersSubscriptionProvider)

    fun inject(preference: PremiumEntryPreference)

    fun inject(preference: PremiumEntryPreferenceCategory)

    fun inject(loader: CacheUserSearchLoader)

    fun inject(loader: BaseUserListsLoader)

    fun inject(controller: PremiumDashboardActivity.ExtraFeatureViewController)

    fun inject(fragment: ExoPlayerPageFragment)

    fun inject(service: StreamingService)

    fun inject(service: BaseService)
}