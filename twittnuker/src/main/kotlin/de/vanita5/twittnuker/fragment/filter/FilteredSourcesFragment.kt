package de.vanita5.twittnuker.fragment.filter

import android.net.Uri
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters

class FilteredSourcesFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String> = Filters.Sources.COLUMNS

    override val contentUri: Uri = Filters.Sources.CONTENT_URI

    override val autoCompleteType: Int = AUTO_COMPLETE_TYPE_SOURCES

}