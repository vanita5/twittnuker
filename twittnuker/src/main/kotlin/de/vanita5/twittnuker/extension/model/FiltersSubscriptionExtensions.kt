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

package de.vanita5.twittnuker.extension.model

import android.content.ComponentName
import android.content.Context
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.model.FiltersSubscription
import de.vanita5.twittnuker.util.JsonSerializer
import de.vanita5.twittnuker.util.filter.FiltersSubscriptionProvider
import de.vanita5.twittnuker.util.filter.LocalFiltersSubscriptionProvider
import de.vanita5.twittnuker.util.filter.UrlFiltersSubscriptionProvider


fun FiltersSubscription.instantiateComponent(context: Context): FiltersSubscriptionProvider? {
    val component = this.component ?: return null
    if (component.startsWith(":")) {
        // Load builtin service
        return LocalFiltersSubscriptionProvider.forName(context, component.substringAfter(":"), arguments)
    }
    return null
}

fun FiltersSubscription.getComponentLabel(context: Context): CharSequence {
    val component = this.component ?: return context.getString(R.string.title_filters_subscription_invalid)
    if (component.startsWith(":")) {
        when (component.substringAfter(":")) {
            "url" -> return context.getString(R.string.title_filters_subscription_url)
        }
        return context.getString(R.string.title_filters_subscription_invalid)
    }
    val cn = ComponentName.unflattenFromString(component) ?:
            return context.getString(R.string.title_filters_subscription_invalid)
    val pm = context.packageManager
    return pm.getServiceInfo(cn, 0).loadLabel(pm)
}

fun FiltersSubscription.setupUrl(url: String) {
    this.component = ":url"
    this.arguments = JsonSerializer.serialize(UrlFiltersSubscriptionProvider.Arguments().apply {
        this.url = url
    })
}