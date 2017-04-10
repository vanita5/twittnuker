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

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.nayuki.qrcodegen.QrCode
import kotlinx.android.synthetic.main.fragment_user_qr.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.util.LinkCreator


class UserQRDialogFragment : BaseDialogFragment() {

    private val user: ParcelableUser get() = arguments.getParcelable(EXTRA_USER)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user_qr, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val qrCode = QrCode.encodeText(LinkCreator.getUserWebLink(user).toString(), QrCode.Ecc.HIGH)
        qrView.setImageDrawable(BitmapDrawable(resources, qrCode.toBitmap(1, 0)).apply {
            this.setAntiAlias(false)
            this.isFilterBitmap = false
        })
    }
}