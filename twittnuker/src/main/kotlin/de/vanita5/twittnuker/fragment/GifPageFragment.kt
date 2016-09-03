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

package de.vanita5.twittnuker.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_media_viewer_gif.*
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.TwittnukerConstants.EXTRA_MEDIA
import de.vanita5.twittnuker.activity.MediaViewerActivity
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.UserKey
import pl.droidsonroids.gif.InputSource

class GifPageFragment : CacheDownloadMediaViewerFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gifView.setOnClickListener { (activity as MediaViewerActivity).toggleBar() }
        startLoading(false)
    }

    override fun getDownloadUri(): Uri? {
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    override fun getDownloadExtra(): Any? {
        return null
    }

    override fun displayMedia(result: CacheDownloadLoader.Result) {
        val context = context ?: return
        if (result.cacheUri != null) {
            gifView.setInputSource(InputSource.UriSource(context.contentResolver, result.cacheUri!!))
        } else {
            gifView.setInputSource(null)
        }
    }

    override fun isAbleToLoad(): Boolean {
        return downloadUri != null
    }

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_gif, parent, false)
    }

    override fun recycleMedia() {
        gifView.setInputSource(null)
    }

    private val media: ParcelableMedia
        get() = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA)

    private val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
}