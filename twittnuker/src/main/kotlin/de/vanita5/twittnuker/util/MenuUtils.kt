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

package de.vanita5.twittnuker.util

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.setItemChecked
import org.mariotaku.ktextension.setMenuItemIcon
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.activity.AccountSelectorActivity
import de.vanita5.twittnuker.activity.ColorPickerDialogActivity
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.fragment.AbsStatusesFragment
import de.vanita5.twittnuker.fragment.AddStatusFilterDialogFragment
import de.vanita5.twittnuker.fragment.DestroyStatusDialogFragment
import de.vanita5.twittnuker.graphic.ActionIconDrawable
import de.vanita5.twittnuker.graphic.PaddingDrawable
import de.vanita5.twittnuker.menu.FavoriteItemProvider
import de.vanita5.twittnuker.menu.SupportStatusShareProvider
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.CreateFavoriteTask
import de.vanita5.twittnuker.task.DestroyFavoriteTask
import de.vanita5.twittnuker.task.RetweetStatusTask
import de.vanita5.twittnuker.util.menu.TwidereMenuInfo

object MenuUtils {

    fun setItemAvailability(menu: Menu?, id: Int, available: Boolean) {
        if (menu == null) return
        val item = menu.findItem(id) ?: return
        item.isVisible = available
        item.isEnabled = available
    }

    fun setItemChecked(menu: Menu?, id: Int, checked: Boolean) {
        menu?.setItemChecked(id, checked)
    }

    fun setMenuItemIcon(menu: Menu?, id: Int, @DrawableRes icon: Int) {
        menu?.setMenuItemIcon(id, icon)
    }

    fun setMenuItemTitle(menu: Menu?, id: Int, @StringRes icon: Int) {
        if (menu == null) return
        val item = menu.findItem(id) ?: return
        item.setTitle(icon)
    }

    @JvmOverloads fun addIntentToMenu(context: Context?, menu: Menu?, queryIntent: Intent?,
                                      groupId: Int = Menu.NONE) {
        if (context == null || menu == null || queryIntent == null) return
        val pm = context.packageManager
        val res = context.resources
        val density = res.displayMetrics.density
        val padding = Math.round(density * 4)
        val activities = pm.queryIntentActivities(queryIntent, 0)
        for (info in activities) {
            val intent = Intent(queryIntent)
            val icon = info.loadIcon(pm)
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name)
            val item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm))
            item.intent = intent
            val iw = icon.intrinsicWidth
            val ih = icon.intrinsicHeight
            if (iw > 0 && ih > 0) {
                val iconWithPadding = PaddingDrawable(icon, padding)
                iconWithPadding.setBounds(0, 0, iw, ih)
                item.icon = iconWithPadding
            } else {
                item.icon = icon
            }
        }
    }

    fun setupForStatus(context: Context,
                       preferences: SharedPreferencesWrapper,
                       menu: Menu,
                       status: ParcelableStatus,
                       twitter: AsyncTwitterWrapper,
                       manager: UserColorNameManager) {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context),
                status.account_key, true) ?: return
        setupForStatus(context, preferences, menu, status, account, twitter, manager)
    }

    @UiThread
    fun setupForStatus(context: Context,
                       preferences: SharedPreferencesWrapper,
                       menu: Menu,
                       status: ParcelableStatus,
                       details: AccountDetails,
                       twitter: AsyncTwitterWrapper,
                       manager: UserColorNameManager) {
        if (menu is ContextMenu) {
            menu.setHeaderTitle(context.getString(R.string.status_menu_title_format,
                    manager.getDisplayName(status.user_key, status.user_name, status.user_screen_name,
                            preferences[nameFirstKey]),
                    status.text_unescaped))
        }
        val retweetHighlight = ContextCompat.getColor(context, R.color.highlight_retweet)
        val favoriteHighlight = ContextCompat.getColor(context, R.color.highlight_favorite)
        val likeHighlight = ContextCompat.getColor(context, R.color.highlight_like)
        val loveHighlight = ContextCompat.getColor(context, R.color.highlight_love)
        val isMyRetweet: Boolean
        if (RetweetStatusTask.isCreatingRetweet(status.account_key, status.id)) {
            isMyRetweet = true
        } else if (twitter.isDestroyingStatus(status.account_key, status.id)) {
            isMyRetweet = false
        } else {
            isMyRetweet = status.retweeted || Utils.isMyRetweet(status)
        }
        val delete = menu.findItem(R.id.delete)
        if (delete != null) {
            delete.isVisible = Utils.isMyStatus(status)
        }
        val retweet = menu.findItem(R.id.retweet)
        if (retweet != null) {
            ActionIconDrawable.setMenuHighlight(retweet, TwidereMenuInfo(isMyRetweet, retweetHighlight))
            retweet.setTitle(if (isMyRetweet) R.string.action_cancel_retweet else R.string.action_retweet)
        }
        val favorite = menu.findItem(R.id.favorite)
        var isFavorite = false
        if (favorite != null) {
            if (CreateFavoriteTask.isCreatingFavorite(status.account_key, status.id)) {
                isFavorite = true
            } else if (DestroyFavoriteTask.isDestroyingFavorite(status.account_key, status.id)) {
                isFavorite = false
            } else {
                isFavorite = status.is_favorite
            }
            val provider = MenuItemCompat.getActionProvider(favorite)
            val useStar = preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK)
            if (provider is FavoriteItemProvider) {
                provider.setIsFavorite(favorite, isFavorite)
            } else {
                if (useStar) {
                    val oldIcon = favorite.icon
                    if (oldIcon is ActionIconDrawable) {
                        val starIcon = ContextCompat.getDrawable(context, R.drawable.ic_action_star)
                        favorite.icon = ActionIconDrawable(starIcon, oldIcon.defaultColor)
                    } else {
                        favorite.setIcon(R.drawable.ic_action_star)
                    }
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, favoriteHighlight))
                } else {
                    ActionIconDrawable.setMenuHighlight(favorite, TwidereMenuInfo(isFavorite, likeHighlight))
                }
            }
            if (useStar) {
                favorite.setTitle(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite)
            } else {
                favorite.setTitle(if (isFavorite) R.string.action_undo_like else R.string.action_like)
            }
        }
        val love = menu.findItem(R.id.love)
        if (love != null) {
            ActionIconDrawable.setMenuHighlight(love, TwidereMenuInfo(isMyRetweet && status.is_favorite, loveHighlight))
            love.setTitle(if (isMyRetweet && (if (favorite != null) isFavorite else status.is_favorite)) R.string.undo_love else R.string.love)
        }
        val translate = menu.findItem(R.id.translate)
        if (translate != null) {
            val isOfficialKey = Utils.isOfficialCredentials(context, details)
            setItemAvailability(menu, R.id.translate, isOfficialKey)
        }
        val shareItem = menu.findItem(R.id.share)
        val shareProvider = MenuItemCompat.getActionProvider(shareItem)
        if (shareProvider is SupportStatusShareProvider) {
            shareProvider.status = status
        } else if (shareProvider is ShareActionProvider) {
            val shareIntent = Utils.createStatusShareIntent(context, status)
            shareProvider.setShareIntent(shareIntent)
        } else if (shareItem.hasSubMenu()) {
            val shareSubMenu = shareItem.subMenu
            val shareIntent = Utils.createStatusShareIntent(context, status)
            shareSubMenu.removeGroup(Constants.MENU_GROUP_STATUS_SHARE)
            addIntentToMenu(context, shareSubMenu, shareIntent, Constants.MENU_GROUP_STATUS_SHARE)
        } else {
            val shareIntent = Utils.createStatusShareIntent(context, status)
            val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_status))
            shareItem.intent = chooserIntent
        }

    }

    fun handleStatusClick(context: Context,
                          fragment: Fragment?,
                          fm: FragmentManager,
                          colorNameManager: UserColorNameManager,
                          twitter: AsyncTwitterWrapper,
                          status: ParcelableStatus,
                          item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> {
                if (ClipboardUtils.setText(context, status.text_plain)) {
                    Utils.showOkMessage(context, R.string.text_copied, false)
                }
            }
            R.id.retweet -> {
                Utils.retweet(status, twitter)
            }
            R.id.quote -> {
                val intent = Intent(INTENT_ACTION_QUOTE)
                intent.putExtra(EXTRA_STATUS, status)
                context.startActivity(intent)
            }
            R.id.reply -> {
                val intent = Intent(INTENT_ACTION_REPLY)
                intent.putExtra(EXTRA_STATUS, status)
                context.startActivity(intent)
            }
            R.id.favorite -> {
                Utils.favorite(status, twitter, item)
            }
            R.id.delete -> {
                DestroyStatusDialogFragment.show(fm, status)
            }
            R.id.add_to_filter -> {
                AddStatusFilterDialogFragment.show(fm, status)
            }
            R.id.love -> {
                Utils.retweet(status, twitter)
                Utils.favorite(status, twitter, item)
            }
            R.id.set_color -> {
                val intent = Intent(context, ColorPickerDialogActivity::class.java)
                val color = colorNameManager.getUserColor(status.user_key)
                if (color != 0) {
                    intent.putExtra(EXTRA_COLOR, color)
                }
                intent.putExtra(EXTRA_CLEAR_BUTTON, color != 0)
                intent.putExtra(EXTRA_ALPHA_SLIDER, false)
                if (fragment != null) {
                    fragment.startActivityForResult(intent, REQUEST_SET_COLOR)
                } else if (context is Activity) {
                    context.startActivityForResult(intent, REQUEST_SET_COLOR)
                }
            }
            R.id.open_with_account -> {
                val intent = Intent(INTENT_ACTION_SELECT_ACCOUNT)
                intent.setClass(context, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                intent.putExtra(EXTRA_ACCOUNT_HOST, status.user_key.host)
                if (fragment != null) {
                    fragment.startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
                } else if (context is Activity) {
                    context.startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
                }
            }
            R.id.open_in_browser -> {
                val uri = LinkCreator.getStatusWebLink(status)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.`package` = null
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_open_in_browser)))
                }
            }
            R.id.copy_url -> {
                val uri = LinkCreator.getStatusWebLink(status)
                ClipboardUtils.setText(context, uri.toString())
                Utils.showOkMessage(context, R.string.message_toast_link_copied_to_clipboard, false)
            }
            R.id.make_gap -> {
                val resolver = context.contentResolver
                val values = ContentValues()
                values.put(Statuses.IS_GAP, 1)
                val where = Expression.equalsArgs(Statuses._ID).sql
                val whereArgs = arrayOf(status._id.toString())
                resolver.update(Statuses.CONTENT_URI, values, where, whereArgs)
            }
            else -> {
                if (item.intent != null) {
                    try {
                        context.startActivity(item.intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(LOGTAG, e)
                        return false
                    }

                }
            }
        }
        return true
    }
}