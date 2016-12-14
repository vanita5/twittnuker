package org.mariotaku.ktextension

import android.graphics.drawable.Drawable
import android.support.v4.view.MenuItemCompat
import android.view.Menu

fun Menu.setItemAvailability(id: Int, available: Boolean) {
    val item = findItem(id) ?: return
    item.isVisible = available
    item.isEnabled = available
}

fun Menu.setMenuGroupAvailability(groupId: Int, available: Boolean) {
    setGroupEnabled(groupId, available)
    setGroupVisible(groupId, available)
}

fun Menu.setItemChecked(id: Int, checked: Boolean) {
    findItem(id)?.isChecked = checked
}

fun Menu.setMenuItemIcon(id: Int, icon: Int) {
    findItem(id)?.setIcon(icon)
}

fun Menu.setMenuItemIcon(id: Int, icon: Drawable) {
    findItem(id)?.setIcon(icon)
}

fun Menu.setMenuItemTitle(id: Int, title: Int) {
    findItem(id)?.setTitle(title)
}

fun Menu.setMenuItemShowAsActionFlags(id: Int, flags: Int) {
    val item = findItem(id) ?: return
    item.setShowAsActionFlags(flags)
    MenuItemCompat.setShowAsAction(item, flags)
}