/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_media_viewer_browser_fragment.*
import kotlinx.android.synthetic.main.layout_media_viewer_texture_video_view.*
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.EXTRA_MEDIA
import de.vanita5.twittnuker.model.ParcelableMedia

class ExternalBrowserPageFragment : MediaViewerFragment() {

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup,
                                   savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_browser_fragment, parent, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadsImagesAutomatically = true
        val media = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA) ?: throw NullPointerException()
        webView.loadUrl(if (TextUtils.isEmpty(media.media_url)) media.url else media.media_url)
        videoContainer.setAspectRatioSource(VideoPageFragment.MediaAspectRatioSource(media, this))
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }


    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }

    override fun recycleMedia() {

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity?.supportInvalidateOptionsMenu()
        }
    }
}