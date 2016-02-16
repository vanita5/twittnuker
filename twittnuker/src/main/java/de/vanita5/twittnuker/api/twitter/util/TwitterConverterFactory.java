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

package de.vanita5.twittnuker.api.twitter.util;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.ParameterizedTypeAccessor;
import com.fasterxml.jackson.core.JsonParseException;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.mime.Body;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.auth.OAuthToken;
import de.vanita5.twittnuker.api.twitter.model.ResponseCode;
import de.vanita5.twittnuker.api.twitter.model.TwitterResponse;
import de.vanita5.twittnuker.util.TwidereTypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TwitterConverterFactory extends RestConverter.SimpleFactory<TwitterException> {

    private static SimpleArrayMap<Type, RestConverter<HttpResponse, ?, TwitterException>> sResponseConverters = new SimpleArrayMap<>();
    private static SimpleArrayMap<Type, RestConverter<?, Body, TwitterException>> sBodyConverters = new SimpleArrayMap<>();

    private static final Executor sWatchDogExecutor = Executors.newCachedThreadPool();

    static {
        sResponseConverters.put(ResponseCode.class, new ResponseCode.Converter());
        sResponseConverters.put(OAuthToken.class, new OAuthToken.Converter());
    }

    public static TwitterException parseTwitterException(HttpResponse resp) {
        try {
            final Body body = resp.getBody();
            if (body == null) return new TwitterException(resp);
            final JsonMapper<TwitterException> mapper;
            synchronized (TwitterConverterFactory.class) {
                mapper = LoganSquare.mapperFor(TwitterException.class);
            }
            final TwitterException parse = mapper.parse(body.stream());
            if (parse != null) return parse;
            return new TwitterException(resp);
        } catch (JsonParseException e) {
            return new TwitterException("Malformed JSON Data", e, resp);
        } catch (IOException e) {
            return new TwitterException("IOException while throwing exception", e, resp);
        }
    }

    @NonNull
    private static <T> T parseOrThrow(Body body, Type type)
            throws IOException, TwitterException, RestConverter.ConvertException {
        try {
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " ---> ?");
            }
            final ParameterizedType<T> parameterizedType = ParameterizedTypeAccessor.create(type);
            final WatchdogRunnable runnable = new WatchdogRunnable(new Exception());
            sWatchDogExecutor.execute(runnable);
            final JsonMapper<T> mapper;
            synchronized (TwitterConverterFactory.class) {
                mapper = LoganSquare.mapperFor(parameterizedType);
            }
            runnable.finished();
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " ---> " + TwidereTypeUtils.toSimpleName(mapper.getClass()));
            }
            final T parsed = mapper.parse(body.stream());
            if (BuildConfig.DEBUG) {
                Log.d("TwitterConverter", TwidereTypeUtils.toSimpleName(type) + " Finished");
            }
            if (parsed == null) {
                throw new TwitterException("Empty data");
            }
            return parsed;
        } catch (JsonParseException e) {
            throw new RestConverter.ConvertException("Malformed JSON Data");
        }
    }

    @Override
    public RestConverter<HttpResponse, ?, TwitterException> forResponse(Type type) {
        RestConverter<HttpResponse, ?, TwitterException> converter = sResponseConverters.get(type);
        if (converter != null) {
            return converter;
        }
        return new TwitterConverter(type);
    }

    @Override
    public RestConverter<?, Body, TwitterException> forRequest(Type type) {
        final RestConverter<?, Body, TwitterException> converter = sBodyConverters.get(type);
        if (converter != null) {
            return converter;
        }
        return super.forRequest(type);
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }

    public static class TwitterConverter implements RestConverter<HttpResponse, Object, TwitterException> {
        private final Type type;

        public TwitterConverter(Type type) {
            this.type = type;
        }

        @Override
        public Object convert(HttpResponse httpResponse) throws IOException, ConvertException, TwitterException {
            final Body body = httpResponse.getBody();
            final Object object = parseOrThrow(body, type);
            if (object instanceof TwitterResponse) {
                ((TwitterResponse) object).processResponseHeader(httpResponse);
            }
            return object;
        }
    }

    private static class WatchdogRunnable implements Runnable {
        private final Exception e;
        private boolean finished;

        public WatchdogRunnable(Exception e) {
            this.e = e;
        }

        @Override
        public void run() {
            // Crash if take more than 100ms
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                //
            }
            if (!finished) {
                throw new RuntimeException("Too long waiting for deserialization", e);
            }
        }

        public synchronized void finished() {
            this.finished = true;
        }
    }
}