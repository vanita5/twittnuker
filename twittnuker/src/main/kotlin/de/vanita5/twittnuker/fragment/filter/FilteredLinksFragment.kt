package de.vanita5.twittnuker.fragment.filter

import android.net.Uri
import de.vanita5.twittnuker.fragment.filter.BaseFiltersFragment
import de.vanita5.twittnuker.provider.TwidereDataStore

class FilteredLinksFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Links.COLUMNS

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Links.CONTENT_URI

}