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

package de.vanita5.twittnuker.util.media;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.Body;
import de.vanita5.twittnuker.extension.model.AccountExtensionsKt;
import de.vanita5.twittnuker.extension.model.CredentialsExtensionsKt;
import de.vanita5.twittnuker.model.CacheMetadata;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.account.cred.Credentials;
import de.vanita5.twittnuker.model.util.AccountUtils;
import de.vanita5.twittnuker.util.JsonSerializer;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.UserAgentUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;
import de.vanita5.twittnuker.util.net.NoIntercept;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TwidereMediaDownloader implements MediaDownloader {

    private final Context context;
    private final RestHttpClient client;
    private final String userAgent;

    public TwidereMediaDownloader(final Context context, final RestHttpClient client) {
        this.context = context;
        this.client = client;
        this.userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context);
    }

    @NonNull
    @Override
    public CacheDownloadLoader.DownloadResult get(@NonNull String url, Object extra) throws IOException {
        try {
            boolean skipUrlReplacing = false;
            if (extra instanceof MediaExtra) {
                skipUrlReplacing = ((MediaExtra) extra).isSkipUrlReplacing();
            }
            if (!skipUrlReplacing) {
                final ParcelableMedia media = PreviewMediaExtractor.fromLink(url, client, extra);
                if (media != null && media.media_url != null) {
                    return getInternal(media.media_url, extra);
                }
            }
            return getInternal(url, extra);
        } catch (IOException e) {
            if (extra instanceof MediaExtra) {
                final String fallbackUrl = ((MediaExtra) extra).getFallbackUrl();
                if (fallbackUrl != null) {
                    final ParcelableMedia media = PreviewMediaExtractor.fromLink(fallbackUrl,
                            client, extra);
                    if (media != null && media.media_url != null) {
                        return getInternal(media.media_url, extra);
                    } else {
                        return getInternal(fallbackUrl, extra);
                    }
                }
            }
            throw e;
        }
    }

    protected CacheDownloadLoader.DownloadResult getInternal(@NonNull String url,
                                                             @Nullable Object extra) throws IOException {
        final Uri uri = Uri.parse(url);
        Credentials credentials = null;
        if (extra instanceof MediaExtra) {
            UserKey accountKey = ((MediaExtra) extra).getAccountKey();
            if (accountKey != null) {
                final AccountManager am = AccountManager.get(context);
                Account account = AccountUtils.findByAccountKey(am, accountKey);
                if (account != null) {
                    credentials = AccountExtensionsKt.getCredentials(account, am);
                }
            }
        }
        final Uri modifiedUri = getReplacedUri(uri, credentials != null ? credentials.api_url_format : null);
        final MultiValueMap<String> additionalHeaders = new MultiValueMap<>();
        additionalHeaders.add("User-Agent", userAgent);
        final String method = GET.METHOD;
        final String requestUri;
        if (isAuthRequired(credentials, uri)) {
            additionalHeaders.add("Authorization", CredentialsExtensionsKt.authorizationHeader(credentials,
                    uri, modifiedUri, null));
            requestUri = modifiedUri.toString();
        } else  {
            requestUri = modifiedUri.toString();
        }
        final HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.method(method);
        builder.url(requestUri);
        builder.headers(additionalHeaders);
        builder.tag(NoIntercept.INSTANCE);
        final HttpResponse resp = client.newCall(builder.build()).execute();
        if (!resp.isSuccessful()) {
            final String detailMessage = "Unable to get " + requestUri + ", response code: "
                    + resp.getStatus();
            if (resp.getStatus() == 404) {
                throw new FileNotFoundException(detailMessage);
            }
            throw new IOException(detailMessage);
        }
        final Body body = resp.getBody();
        final CacheMetadata metadata = new CacheMetadata();
        metadata.setContentType(Utils.INSTANCE.sanitizeMimeType(body.contentType().getContentType()));
        return new TwidereDownloadResult(body, metadata);
    }

    public static String getEndpoint(Uri uri) {
        final StringBuilder sb = new StringBuilder();
        sb.append(uri.getScheme());
        sb.append("://");
        sb.append(uri.getHost());
        if (uri.getPort() != -1) {
            sb.append(':');
            sb.append(uri.getPort());
        }
        sb.append("/");
        return sb.toString();
    }

    public static boolean isAuthRequired(@Nullable final Credentials credentials, @NonNull final Uri uri) {
        if (credentials == null) return false;
        final String host = uri.getHost();
        if (credentials.api_url_format != null && credentials.api_url_format.contains(host)) {
            return true;
        }
        return "ton.twitter.com".equalsIgnoreCase(host);
    }

    private static boolean isTwitterUri(final Uri uri) {
        return uri != null && "ton.twitter.com".equalsIgnoreCase(uri.getHost());
    }

    public static Uri getReplacedUri(@NonNull final Uri uri, final String apiUrlFormat) {
        if (apiUrlFormat == null) return uri;
        if (isTwitterUri(uri)) {
            final StringBuilder sb = new StringBuilder();
            final String host = uri.getHost();
            final String domain = host.substring(0, host.lastIndexOf(".twitter.com"));
            final String path = uri.getPath();
            sb.append(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, domain, path));
            final String query = uri.getQuery();
            if (!TextUtils.isEmpty(query)) {
                sb.append("?");
                sb.append(query);
            }
            final String fragment = uri.getFragment();
            if (!TextUtils.isEmpty(fragment)) {
                sb.append("#");
                sb.append(fragment);
            }
            return Uri.parse(sb.toString());
        }
        return uri;
    }

    private static class TwidereDownloadResult implements CacheDownloadLoader.DownloadResult {
        private final Body mBody;
        private final CacheMetadata mMetadata;

        public TwidereDownloadResult(Body body, CacheMetadata metadata) {
            mBody = body;
            mMetadata = metadata;
        }

        @Override
        public void close() throws IOException {
            mBody.close();
        }

        @Override
        public long getLength() throws IOException {
            return mBody.length();
        }

        @NonNull
        @Override
        public InputStream getStream() throws IOException {
            return mBody.stream();
        }

        @Override
        public byte[] getExtra() {
            if (mMetadata == null) return null;
            final String serialize = JsonSerializer.serialize(mMetadata, CacheMetadata.class);
            if (serialize == null) return null;
            return serialize.getBytes();
        }
    }
}