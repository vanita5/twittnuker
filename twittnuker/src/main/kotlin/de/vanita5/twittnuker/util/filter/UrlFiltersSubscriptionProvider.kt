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

package de.vanita5.twittnuker.util.filter

import android.content.Context
import android.net.Uri
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import org.mariotaku.ktextension.convert
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.http.mime.Body
import de.vanita5.twittnuker.extension.model.parse
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.util.ETagCache
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import java.io.IOException
import javax.inject.Inject


class UrlFiltersSubscriptionProvider(context: Context, val arguments: Arguments) : LocalFiltersSubscriptionProvider(context) {
    @Inject
    internal lateinit var restHttpClient: RestHttpClient
    @Inject
    internal lateinit var etagCache: ETagCache
    private var filters: FiltersData? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(IOException::class)
    override fun fetchFilters(): Boolean {
        val builder = HttpRequest.Builder()
        builder.method(GET.METHOD)
        builder.url(arguments.url)
        val headers = MultiValueMap<String>()
        etagCache[arguments.url]?.let { etag ->
            headers.add("If-None-Match", etag)
        }

        builder.headers(headers)
        val request = builder.build()
        restHttpClient.newCall(request).execute().use { response ->
            if (response.status != 200) {
                return false
            }
            val etag = response.getHeader("ETag")
            this.filters = response.body?.convert { body ->
                when (response.body.contentType()?.contentType) {
                    "application/json" -> {
                        return@convert body.toJsonFilters()
                    }
                    "application/xml", "text/xml" -> {
                        return@convert body.toXmlFilters()
                    }
                    else -> {
                        // Infer from extension
                        val uri = Uri.parse(arguments.url)
                        when (uri.lastPathSegment?.substringAfterLast('.')) {
                            "xml" -> return@convert body.toXmlFilters()
                            "json" -> return@convert body.toJsonFilters()
                        }
                        return@convert null
                    }
                }
            }
            etagCache[arguments.url] = etag
            return true
        }
    }

    override fun getUsers(): List<FiltersData.UserItem>? {
        return filters?.users
    }

    override fun getKeywords(): List<FiltersData.BaseItem>? {
        return filters?.keywords
    }

    override fun getSources(): List<FiltersData.BaseItem>? {
        return filters?.sources
    }

    override fun getLinks(): List<FiltersData.BaseItem>? {
        return filters?.links
    }

    private fun Body.toJsonFilters(): FiltersData? {
        return LoganSquare.parse(stream(), FiltersData::class.java)
    }

    private fun Body.toXmlFilters(): FiltersData? {
        return FiltersData().apply {
            this.parse(stream().newPullParser(Charsets.UTF_8))
        }
    }

    @JsonObject
    class Arguments {
        @JsonField(name = arrayOf("url"))
        lateinit var url: String
    }
}