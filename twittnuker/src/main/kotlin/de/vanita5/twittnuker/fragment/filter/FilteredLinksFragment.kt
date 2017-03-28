package de.vanita5.twittnuker.fragment.filter

import android.net.Uri
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters

class FilteredLinksFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String> = Filters.Links.COLUMNS

    override val contentUri: Uri = Filters.Links.CONTENT_URI

}