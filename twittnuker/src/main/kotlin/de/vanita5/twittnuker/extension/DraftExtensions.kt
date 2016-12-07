/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.extension

import android.content.Context
import android.net.Uri
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.MessageServiceFactory
import org.apache.james.mime4j.dom.address.Mailbox
import org.apache.james.mime4j.dom.field.*
import org.apache.james.mime4j.message.AbstractMessage
import org.apache.james.mime4j.message.BodyPart
import org.apache.james.mime4j.message.MultipartImpl
import org.apache.james.mime4j.message.SimpleContentHandler
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.storage.StorageBodyFactory
import org.apache.james.mime4j.stream.BodyDescriptor
import org.apache.james.mime4j.stream.MimeConfig
import org.mariotaku.ktextension.convert
import org.mariotaku.ktextension.toInt
import org.mariotaku.ktextension.toString
import de.vanita5.twittnuker.extension.model.getMimeType
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.ParcelableMediaUpdate
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.collection.NonEmptyHashMap
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*


fun Draft.writeMimeMessageTo(context: Context, st: OutputStream) {
    val bodyFactory = StorageBodyFactory()
    val storageProvider = bodyFactory.storageProvider
    val contentResolver = context.contentResolver

    val factory = MessageServiceFactory.newInstance()
    val builder = factory.newMessageBuilder()
    val writer = factory.newMessageWriter()

    val message = builder.newMessage() as AbstractMessage

    message.date = Date(this.timestamp)
    message.setFrom(this.account_keys?.map { Mailbox(it.id, it.host) })

    val multipart = MultipartImpl("mixed")
    multipart.addBodyPart(BodyPart().apply {
        setText(bodyFactory.textBody(this@writeMimeMessageTo.text))
    })
    this.media?.forEach { mediaItem ->
        multipart.addBodyPart(BodyPart().apply {
            val uri = Uri.parse(mediaItem.uri)
            val storage = storageProvider.store(contentResolver.openInputStream(uri))
            val mimeType = mediaItem.getMimeType(contentResolver) ?: "application/octet-stream"
            val parameters = NonEmptyHashMap<String, String?>()
            parameters["alt_text"] = mediaItem.alt_text
            parameters["media_type"] = mediaItem.type.toString()
            this.setBody(bodyFactory.binaryBody(storage), mimeType, parameters)
            this.filename = uri.lastPathSegment
        })
    }

    message.setMultipart(multipart)
    writer.writeMessage(message, st)
}

fun Draft.readMimeMessageFrom(context: Context, st: InputStream) {
    val config = MimeConfig()
    val parser = MimeStreamParser(config)
    parser.setContentHandler(DraftContentHandler(this))
    parser.parse(st)
}

private class DraftContentHandler(private val draft: Draft) : SimpleContentHandler() {
    private val processingStack = Stack<SimpleContentHandler>()
    private val mediaList: MutableList<ParcelableMediaUpdate> = ArrayList()
    override fun headers(header: Header) {
        if (processingStack.isEmpty()) {
            draft.timestamp = header.getField("Date").convert {
                (it as DateTimeField).date.time
            }
            draft.account_keys = header.getField("From").convert { field ->
                when (field) {
                    is MailboxField -> {
                        return@convert arrayOf(field.mailbox.convert { UserKey(it.localPart, it.domain) })
                    }
                    is MailboxListField -> {
                        return@convert field.mailboxList.map { UserKey(it.localPart, it.domain) }.toTypedArray()
                    }
                    else -> {
                        return@convert null
                    }
                }
            }
        } else {
            processingStack.peek().headers(header)
        }
    }

    override fun startMultipart(bd: BodyDescriptor) {
    }

    override fun preamble(`is`: InputStream?) {
        processingStack.peek().preamble(`is`)
    }

    override fun startBodyPart() {
        processingStack.push(BodyPartHandler(draft))
    }

    override fun body(bd: BodyDescriptor?, `is`: InputStream?) {
        processingStack.peek().body(bd, `is`)
    }

    override fun endBodyPart() {
        val handler = processingStack.pop() as BodyPartHandler
        handler.media?.let {
            mediaList.add(it)
        }
    }

    override fun epilogue(`is`: InputStream?) {
        processingStack.peek().epilogue(`is`)
    }

    override fun endMultipart() {
        draft.media = mediaList.toTypedArray()
    }
}

private class BodyPartHandler(private val draft: Draft) : SimpleContentHandler() {
    internal lateinit var header: Header
    internal var media: ParcelableMediaUpdate? = null

    override fun headers(header: Header) {
        this.header = header
    }

    override fun body(bd: BodyDescriptor, st: InputStream) {
        body(header, bd, st)
    }

    fun body(header: Header, bd: BodyDescriptor, st: InputStream) {
        val contentDisposition = header.getField("Content-Disposition") as? ContentDispositionField
        if (contentDisposition != null && contentDisposition.isAttachment) {
            when (contentDisposition.filename) {
                else -> {
                    val contentType = header.getField("Content-Type") as? ContentTypeField
                    media = ParcelableMediaUpdate().apply {
                        this.type = contentType?.getParameter("media_type").toInt(ParcelableMedia.Type.UNKNOWN)
                        this.alt_text = contentType?.getParameter("alt_text")
                    }
                }
            }
        } else if (bd.mimeType == "text/plain" && draft.text == null) {
            draft.text = st.toString(Charset.forName(bd.charset))
        }
    }
}
