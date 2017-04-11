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

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrCodeAndroid
import kotlinx.android.synthetic.main.fragment_user_qr.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.extension.loadProfileImage
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
        val bitmap = QrCodeAndroid.toBitmap(qrCode, 1, 0, Bitmap.Config.ARGB_8888)
        val profileImageSize = getString(R.string.profile_image_size)
        qrView.setImageDrawable(BitmapDrawable(resources, bitmap).apply {
            this.setAntiAlias(false)
            this.isFilterBitmap = false
        })
        profileImage.setShapeBackground(Color.WHITE)
        Glide.with(this).loadProfileImage(context, user, profileImage.style, profileImage.cornerRadius,
                profileImage.cornerRadiusRatio, profileImageSize).into(profileImage)
    }

}