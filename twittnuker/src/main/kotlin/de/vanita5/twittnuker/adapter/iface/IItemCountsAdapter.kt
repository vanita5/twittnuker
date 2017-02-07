package de.vanita5.twittnuker.adapter.iface

import de.vanita5.twittnuker.model.ItemCounts

interface IItemCountsAdapter {

    val itemCounts: ItemCounts

    fun getItemCountIndex(position: Int): Int {
        return itemCounts.getItemCountIndex(position)
    }

    fun getItemStartPosition(index: Int): Int {
        return itemCounts.getItemStartPosition(index)
    }

}