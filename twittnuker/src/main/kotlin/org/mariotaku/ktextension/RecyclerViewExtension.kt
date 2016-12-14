package org.mariotaku.ktextension

import android.support.v7.widget.RecyclerView

fun RecyclerView.Adapter<*>.findPositionByItemId(itemId: Long): Int {
    for (i in 0 until itemCount) {
        if (getItemId(i) == itemId) return i
    }
    return RecyclerView.NO_POSITION
}