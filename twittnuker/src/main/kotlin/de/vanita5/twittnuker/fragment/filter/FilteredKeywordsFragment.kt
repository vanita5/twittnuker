package de.vanita5.twittnuker.fragment.filter

import android.net.Uri
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters

class FilteredKeywordsFragment : BaseFiltersFragment() {

    override val contentUri: Uri = Filters.Keywords.CONTENT_URI

    override val contentColumns: Array<String> = Filters.Keywords.COLUMNS

}