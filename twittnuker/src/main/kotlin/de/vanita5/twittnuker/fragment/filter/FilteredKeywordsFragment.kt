package de.vanita5.twittnuker.fragment.filter

import android.net.Uri
import de.vanita5.twittnuker.fragment.BaseFiltersFragment
import de.vanita5.twittnuker.provider.TwidereDataStore

class FilteredKeywordsFragment : BaseFiltersFragment() {

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Keywords.CONTENT_URI

    override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Keywords.COLUMNS

}