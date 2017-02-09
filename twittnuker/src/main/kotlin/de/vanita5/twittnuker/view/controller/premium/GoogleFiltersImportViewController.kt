/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view.controller.premium

import android.widget.Toast
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.util.IntentUtils
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService


class GoogleFiltersImportViewController : AbsGoogleInAppItemViewController() {
    override val feature: String
        get() = ExtraFeaturesService.FEATURE_FILTERS_IMPORT
    override val summary: String
        get() = context.getString(R.string.extra_feature_description_filters_import)
    override val title: String
        get() = context.getString(R.string.extra_feature_title_filters_import)
    override val availableLabel: String
        get() = context.getString(R.string.action_import)

    override fun onAvailableButtonClick() {
        IntentUtils.openFilters(context, "users")
        Toast.makeText(context, R.string.message_toast_filters_import_hint, Toast.LENGTH_SHORT).show()
    }
}