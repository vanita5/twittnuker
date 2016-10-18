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

package org.mariotaku.ktextension

import android.os.Build
import java.util.*

/**
 * Modified from:
 * https://github.com/apache/cordova-plugin-globalization/blob/master/src/android/Globalization.java

 * Returns a well-formed ITEF BCP 47 language tag representing this locale string
 * identifier for the client's current locale

 * @return String: The BCP 47 language tag for the current locale
 */
val Locale.bcp47Tag: String
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return toLanguageTag()
        }

        // we will use a dash as per BCP 47
        val SEP = '-'
        var language = language
        var country = country
        var variant = variant

        // special case for Norwegian Nynorsk since "NY" cannot be a variant as per BCP 47
        // this goes before the string matching since "NY" wont pass the variant checks
        if (language == "no" && country == "NO" && variant == "NY") {
            language = "nn"
            country = "NO"
            variant = ""
        }

        if (language.isEmpty() || !language.matches("\\p{Alpha}{2,8}".toRegex())) {
            language = "und"       // Follow the Locale#toLanguageTag() implementation
            // which says to return "und" for Undetermined
        } else if (language == "iw") {
            language = "he"        // correct deprecated "Hebrew"
        } else if (language == "in") {
            language = "id"        // correct deprecated "Indonesian"
        } else if (language == "ji") {
            language = "yi"        // correct deprecated "Yiddish"
        }

        // ensure valid country code, if not well formed, it's omitted
        if (!country.matches("\\p{Alpha}{2}|\\p{Digit}{3}".toRegex())) {
            country = ""
        }

        // variant subtags that begin with a letter must be at least 5 characters long
        if (!variant.matches("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}".toRegex())) {
            variant = ""
        }

        val bcp47Tag = StringBuilder(language)
        if (!country.isEmpty()) {
            bcp47Tag.append(SEP).append(country)
        }
        if (!variant.isEmpty()) {
            bcp47Tag.append(SEP).append(variant)
        }

        return bcp47Tag.toString()
    }