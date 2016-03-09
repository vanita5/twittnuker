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

package de.vanita5.twittnuker.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import de.vanita5.twittnuker.api.gnusocial.model.Attachment;
import de.vanita5.twittnuker.api.twitter.model.CardEntity;
import de.vanita5.twittnuker.api.twitter.model.EntitySupport;
import de.vanita5.twittnuker.api.twitter.model.ExtendedEntitySupport;
import de.vanita5.twittnuker.api.twitter.model.MediaEntity;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.UrlEntity;
import de.vanita5.twittnuker.model.ParcelableCardEntity;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableMediaUpdate;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.TwidereArrayUtils;
import de.vanita5.twittnuker.util.TwitterContentUtils;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;

import java.util.ArrayList;
import java.util.List;

public class ParcelableMediaUtils {
    @NonNull
    public static ParcelableMedia[] fromEntities(@Nullable final EntitySupport entities) {
        if (entities == null) return new ParcelableMedia[0];
        final List<ParcelableMedia> list = new ArrayList<>();
        final MediaEntity[] mediaEntities;
        if (entities instanceof ExtendedEntitySupport) {
            final ExtendedEntitySupport extendedEntities = (ExtendedEntitySupport) entities;
            final MediaEntity[] extendedMediaEntities = extendedEntities.getExtendedMediaEntities();
            mediaEntities = extendedMediaEntities != null ? extendedMediaEntities : entities.getMediaEntities();
        } else {
            mediaEntities = entities.getMediaEntities();
        }
        if (mediaEntities != null) {
            for (final MediaEntity media : mediaEntities) {
                final String mediaURL = InternalTwitterContentUtils.getMediaUrl(media);
                if (mediaURL != null) {
                    list.add(ParcelableMediaUtils.fromMediaEntity(media));
                }
            }
        }
        final UrlEntity[] urlEntities = entities.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity url : urlEntities) {
                final String expanded = url.getExpandedUrl();
                final ParcelableMedia media = PreviewMediaExtractor.fromLink(expanded);
                if (media != null) {
                    media.start = url.getStart();
                    media.end = url.getEnd();
                    list.add(media);
                }
            }
        }
        return list.toArray(new ParcelableMedia[list.size()]);
    }

    private static ParcelableMedia fromMediaEntity(MediaEntity entity) {
        final ParcelableMedia media = new ParcelableMedia();
        media.url = InternalTwitterContentUtils.getMediaUrl(entity);
        media.media_url = InternalTwitterContentUtils.getMediaUrl(entity);
        media.preview_url = InternalTwitterContentUtils.getMediaUrl(entity);
        media.start = entity.getStart();
        media.end = entity.getEnd();
        media.type = ParcelableMediaUtils.getTypeInt(entity.getType());
        final MediaEntity.Size size = entity.getSizes().get(MediaEntity.Size.LARGE);
        if (size != null) {
            media.width = size.getWidth();
            media.height = size.getHeight();
        } else {
            media.width = 0;
            media.height = 0;
        }
        media.video_info = ParcelableMedia.VideoInfo.fromMediaEntityInfo(entity.getVideoInfo());
        return media;
    }

    @Nullable
    public static ParcelableMedia[] fromMediaUpdates(@Nullable final ParcelableMediaUpdate[] mediaUpdates) {
        if (mediaUpdates == null) return null;
        final ParcelableMedia[] media = new ParcelableMedia[mediaUpdates.length];
        for (int i = 0, j = mediaUpdates.length; i < j; i++) {
            final ParcelableMediaUpdate mediaUpdate = mediaUpdates[i];
            media[i] = new ParcelableMedia(mediaUpdate);
        }
        return media;
    }

    @Nullable
    public static ParcelableMedia[] fromStatus(@NonNull final Status status) {
        final ParcelableMedia[] fromEntities = fromEntities(status);
        final ParcelableMedia[] fromAttachments = fromAttachments(status);
        final ParcelableMedia[] fromCard = fromCard(status.getCard(), status.getUrlEntities());
        final ParcelableMedia[] merged = new ParcelableMedia[fromCard.length +
                fromAttachments.length + fromEntities.length];
        TwidereArrayUtils.mergeArray(merged, fromEntities, fromAttachments, fromCard);
        return merged;
    }

    @NonNull
    private static ParcelableMedia[] fromAttachments(@NonNull Status status) {
        final Attachment[] attachments = status.getAttachments();
        if (attachments == null) return new ParcelableMedia[0];
        final ParcelableMedia[] temp = new ParcelableMedia[attachments.length];
        final String externalUrl = status.getExternalUrl();
        int i = 0;
        for (Attachment attachment : attachments) {
            final String mimetype = attachment.getMimetype();
            if (mimetype != null && mimetype.startsWith("image/")) {
                ParcelableMedia media = new ParcelableMedia();
                media.type = ParcelableMedia.Type.IMAGE;
                media.width = attachment.getWidth();
                media.height = attachment.getHeight();
                media.url = TextUtils.isEmpty(externalUrl) ? attachment.getUrl() : externalUrl;
                media.page_url = TextUtils.isEmpty(externalUrl) ? attachment.getUrl() : externalUrl;
                media.media_url = attachment.getUrl();
                media.preview_url = attachment.getLargeThumbUrl();
                temp[i++] = media;
            }
        }
        return ArrayUtils.subarray(temp, 0, i);
    }

    @NonNull
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card, @Nullable UrlEntity[] entities) {
        if (card == null) return new ParcelableMedia[0];
        final String name = card.getName();
        if ("animated_gif".equals(name) || "player".equals(name)) {
            final ParcelableMedia media = new ParcelableMedia();
            final CardEntity.BindingValue playerStreamUrl = card.getBindingValue("player_stream_url");
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, -1);
            CardEntity.StringValue appUrlResolved = (CardEntity.StringValue) card.getBindingValue("app_url_resolved");
            media.url = checkUrl(appUrlResolved) ? appUrlResolved.getValue() : card.getUrl();
            if ("animated_gif".equals(name)) {
                media.media_url = ((CardEntity.StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.Type.CARD_ANIMATED_GIF;
            } else if (playerStreamUrl instanceof CardEntity.StringValue) {
                media.media_url = ((CardEntity.StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.Type.VIDEO;
            } else {
                CardEntity.StringValue playerUrl = (CardEntity.StringValue) card.getBindingValue("player_url");
                if (playerUrl != null) {
                    media.media_url = playerUrl.getValue();
                }
                media.type = ParcelableMedia.Type.EXTERNAL_PLAYER;
            }
            final CardEntity.BindingValue playerImage = card.getBindingValue("player_image");
            if (playerImage instanceof CardEntity.ImageValue) {
                media.preview_url = ((CardEntity.ImageValue) playerImage).getUrl();
                media.width = ((CardEntity.ImageValue) playerImage).getWidth();
                media.height = ((CardEntity.ImageValue) playerImage).getHeight();
            }
            final CardEntity.BindingValue playerWidth = card.getBindingValue("player_width");
            final CardEntity.BindingValue playerHeight = card.getBindingValue("player_height");
            if (playerWidth instanceof CardEntity.StringValue && playerHeight instanceof CardEntity.StringValue) {
                media.width = NumberUtils.toInt(((CardEntity.StringValue) playerWidth).getValue(), -1);
                media.height = NumberUtils.toInt(((CardEntity.StringValue) playerHeight).getValue(), -1);
            }
            if (entities != null) {
                for (UrlEntity entity : entities) {
                    if (entity.getUrl().equals(media.url)) {
                        media.start = entity.getStart();
                        media.end = entity.getEnd();
                        break;
                    }
                }
            }
            return new ParcelableMedia[]{media};
        } else if ("summary_large_image".equals(name)) {
            final CardEntity.BindingValue photoImageFullSize = card.getBindingValue("photo_image_full_size");
            if (!(photoImageFullSize instanceof CardEntity.ImageValue))
                return new ParcelableMedia[0];

            final ParcelableMedia media = new ParcelableMedia();
            media.url = card.getUrl();
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, -1);
            media.type = ParcelableMedia.Type.IMAGE;
            media.media_url = ((CardEntity.ImageValue) photoImageFullSize).getUrl();
            media.width = ((CardEntity.ImageValue) photoImageFullSize).getWidth();
            media.height = ((CardEntity.ImageValue) photoImageFullSize).getHeight();
            final CardEntity.BindingValue summaryPhotoImage = card.getBindingValue("summary_photo_image");
            if (summaryPhotoImage instanceof CardEntity.ImageValue) {
                media.preview_url = ((CardEntity.ImageValue) summaryPhotoImage).getUrl();
            }
            if (entities != null) {
                for (UrlEntity entity : entities) {
                    if (entity.getUrl().equals(media.url)) {
                        media.start = entity.getStart();
                        media.end = entity.getEnd();
                        break;
                    }
                }
            }
            return new ParcelableMedia[]{media};
        }
        return new ParcelableMedia[0];
    }

    private static boolean checkUrl(CardEntity.StringValue value) {
        if (value == null) return false;
        final String valueString = value.getValue();
        return valueString != null && (valueString.startsWith("http://")
                || valueString.startsWith("https://"));
    }

    public static int getTypeInt(String type) {
        switch (type) {
            case MediaEntity.Type.PHOTO:
                return ParcelableMedia.Type.IMAGE;
            case MediaEntity.Type.VIDEO:
                return ParcelableMedia.Type.VIDEO;
            case MediaEntity.Type.ANIMATED_GIF:
                return ParcelableMedia.Type.ANIMATED_GIF;
        }
        return ParcelableMedia.Type.UNKNOWN;
    }

    public static ParcelableMedia image(final String url) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = url;
        media.media_url = url;
        media.preview_url = url;
        return media;
    }

    public static boolean hasPlayIcon(@ParcelableMedia.Type int type) {
        switch (type) {
            case ParcelableMedia.Type.VIDEO:
            case ParcelableMedia.Type.ANIMATED_GIF:
            case ParcelableMedia.Type.CARD_ANIMATED_GIF:
            case ParcelableMedia.Type.EXTERNAL_PLAYER:
                return true;
        }
        return false;
    }

    public static ParcelableMedia findByUrl(@Nullable ParcelableMedia[] media, @Nullable String url) {
        if (media == null || url == null) return null;
        for (ParcelableMedia item : media) {
            if (url.equals(item.url)) return item;
        }
        return null;
    }
}