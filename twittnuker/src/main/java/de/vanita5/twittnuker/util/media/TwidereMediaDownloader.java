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

package de.vanita5.twittnuker.util.media;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.mime.Body;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.auth.OAuthAuthorization;
import de.vanita5.twittnuker.api.twitter.auth.OAuthEndpoint;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.UserAgentUtils;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;

import java.io.IOException;

public class TwidereMediaDownloader implements MediaDownloader, Constants {

    private final Context mContext;
    private final RestHttpClient mClient;
    private final String mUserAgent;

    public TwidereMediaDownloader(final Context context, RestHttpClient client) {
        mContext = context;
        mClient = client;
        mUserAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context);
        reloadConnectivitySettings();
    }

    public void reloadConnectivitySettings() {
    }

    @NonNull
    @Override
    public CacheDownloadLoader.DownloadResult get(@NonNull String url, Object extra) throws IOException {
        final ParcelableMedia media = PreviewMediaExtractor.fromLink(url, mClient, extra);
        return getInternal(media != null && media.media_url != null ? media.media_url : url, extra);
    }

    protected CacheDownloadLoader.DownloadResult getInternal(@NonNull String url,
                                                             @Nullable Object extra) throws IOException {
        final Uri uri = Uri.parse(url);
        final Authorization auth;
        final ParcelableCredentials account;
        if (extra instanceof MediaExtra) {
            account = ParcelableAccount.getCredentials(mContext, ((MediaExtra) extra).getAccountId());
            auth = TwitterAPIFactory.getAuthorization(account);
        } else {
            account = null;
            auth = null;
        }
        Uri modifiedUri = getReplacedUri(uri, account != null ? account.api_url_format : null);
        final MultiValueMap<String> additionalHeaders = new MultiValueMap<>();
        additionalHeaders.add("User-Agent", mUserAgent);
        final String method = GET.METHOD;
        final String requestUri;
        if (isAuthRequired(uri, account) && auth != null && auth.hasAuthorization()) {
            final Endpoint endpoint;
            if (auth instanceof OAuthAuthorization) {
                endpoint = new OAuthEndpoint(getEndpoint(modifiedUri), getEndpoint(uri));
            } else {
                endpoint = new Endpoint(getEndpoint(modifiedUri));
            }
            final MultiValueMap<String> queries = new MultiValueMap<>();
            for (String name : uri.getQueryParameterNames()) {
                for (String value : uri.getQueryParameters(name)) {
                    queries.add(name, value);
                }
            }
            final RestRequest info = new RestRequest(method, false, uri.getPath(), additionalHeaders,
                    queries, null, null, null, null);
            additionalHeaders.add("Authorization", auth.getHeader(endpoint, info));
            requestUri = modifiedUri.toString();
        } else  {
            requestUri = modifiedUri.toString();
        }
        final HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.method(method);
        builder.url(requestUri);
        builder.headers(additionalHeaders);
        final HttpResponse resp = mClient.newCall(builder.build()).execute();
        if (!resp.isSuccessful()) throw new IOException("Unable to get media");
        final Body body = resp.getBody();
        return new CacheDownloadLoader.DownloadResult(body.length(), body.stream());
    }

    private String getEndpoint(Uri uri) {
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

    private boolean isAuthRequired(final Uri uri, @Nullable final ParcelableCredentials credentials) {
        if (credentials == null) return false;
        final String host = uri.getHost();
        if (credentials.api_url_format != null && credentials.api_url_format.contains(host)) {
            return true;
        }
        return "ton.twitter.com".equalsIgnoreCase(host);
    }

    private boolean isTwitterUri(final Uri uri) {
        return uri != null && "ton.twitter.com".equalsIgnoreCase(uri.getHost());
    }

    private Uri getReplacedUri(@NonNull final Uri uri, final String apiUrlFormat) {
        if (apiUrlFormat == null) return uri;
        if (isTwitterUri(uri)) {
            final StringBuilder sb = new StringBuilder();
            final String host = uri.getHost();
            final String domain = host.substring(0, host.lastIndexOf(".twitter.com"));
            final String path = uri.getPath();
            sb.append(TwitterAPIFactory.getApiUrl(apiUrlFormat, domain, path));
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
}