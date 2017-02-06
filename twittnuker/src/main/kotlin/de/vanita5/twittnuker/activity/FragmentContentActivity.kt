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

package de.vanita5.twittnuker.activity

import android.os.Bundle
import android.support.v4.app.Fragment

class FragmentContentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_TITLE)
        val fragment = Fragment.instantiate(this, intent.getStringExtra(EXTRA_FRAGMENT),
                intent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS))
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(android.R.id.content, fragment)
        ft.commit()
    }

    companion object {
        const val EXTRA_FRAGMENT = "FCA:fragment"
        const val EXTRA_TITLE = "FCA:title"
        const val EXTRA_FRAGMENT_ARGUMENTS = "FCA:fragment_arguments"
    }
}