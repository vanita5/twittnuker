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

package de.vanita5.twittnuker.util.twitter.card

import de.vanita5.twittnuker.extension.model.getString
import de.vanita5.twittnuker.model.ParcelableCardEntity
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.util.TwitterCardUtils
import de.vanita5.twittnuker.view.ContainerView
import de.vanita5.twittnuker.view.controller.twitter.card.CardBrowserViewController
import de.vanita5.twittnuker.view.controller.twitter.card.CardPollViewController
import java.util.*

abstract class TwitterCardViewFactory {

    abstract fun from(status: ParcelableStatus): ContainerView.ViewController?

    companion object {
        fun from(status: ParcelableStatus): ContainerView.ViewController? {
            val vc = fromImplementations(status)
            if (vc != null) return vc
            return createCardFragment(status)
        }

        private fun fromImplementations(status: ParcelableStatus): ContainerView.ViewController? {
            ServiceLoader.load(TwitterCardViewFactory::class.java).forEach { factory ->
                val vc = factory.from(status)
                if (vc != null) return vc
            }
            return null
        }

        private fun createCardFragment(status: ParcelableStatus): ContainerView.ViewController? {
            val card = status.card
            if (card == null || card.name == null) return null
            if (TwitterCardUtils.CARD_NAME_PLAYER == card.name) {
                return createGenericPlayerFragment(card)
            } else if (TwitterCardUtils.CARD_NAME_AUDIO == card.name) {
                return createGenericPlayerFragment(card)
            } else if (TwitterCardUtils.isPoll(card)) {
                return createCardPollFragment(status)
            }
            return null
        }


        private fun createCardPollFragment(status: ParcelableStatus): ContainerView.ViewController {
            return CardPollViewController.show(status)
        }

        private fun createGenericPlayerFragment(card: ParcelableCardEntity): ContainerView.ViewController? {
            val playerUrl = card.getString("player_url") ?: return null
            return CardBrowserViewController.show(playerUrl)
        }
    }
}