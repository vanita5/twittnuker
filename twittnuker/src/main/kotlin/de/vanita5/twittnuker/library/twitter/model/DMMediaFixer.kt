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

package de.vanita5.twittnuker.library.twitter.model

import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.DMResponse.Entry.Message

/**
 * I don't know why Twitter doesn't return video/animatedGif when requesting DM, but this will help
 */
fun DMResponse.fixMedia(microBlog: MicroBlog) {
    entries?.forEach { entry ->
        // Ensure it's a normal message
        val data = entry.message?.messageData ?: return@forEach
        // Ensure this message don't have attachment
        if (data.attachment != null) return@forEach

        // Don't try if it's a group dm
        if (conversations?.get(entry.message.conversationId)?.type == DMResponse.Conversation.Type.GROUP_DM) {
            return@forEach
        }

        val mediaUrl = "https://twitter.com/messages/media/${data.id}"
        if (data.entities?.urls?.find { it.expandedUrl == mediaUrl } == null) return@forEach
        val message = try {
            microBlog.showDirectMessage(data.id)
        } catch (e: MicroBlogException) {
            // Ignore
            return@forEach
        }
        val media = message.entities?.media?.find { it.expandedUrl == mediaUrl } ?: return@forEach

        when (media.type) {
            MediaEntity.Type.VIDEO -> {
                data.attachment = attachment { video = media }
            }
            MediaEntity.Type.PHOTO -> {
                data.attachment = attachment { photo = media }
            }
            MediaEntity.Type.ANIMATED_GIF -> {
                data.attachment = attachment { animatedGif = media }
            }
        }
    }
}

private inline fun attachment(apply: Message.Data.Attachment.() -> Unit): Message.Data.Attachment {
    val attachment = Message.Data.Attachment()
    apply(attachment)
    return attachment
}