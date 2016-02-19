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

package de.vanita5.twittnuker.util;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;
import com.bluelinelabs.logansquare.ParameterizedTypeAccessor;

import de.vanita5.twittnuker.common.BuildConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoganSquareMapperFinder {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    public static <T> JsonMapper<T> mapperFor(Class<T> cls) throws ClassLoaderDeadLockException {
        return mapperFor(ParameterizedTypeAccessor.<T>create(cls));
    }

    public static <T> JsonMapper<T> mapperFor(Type type) throws ClassLoaderDeadLockException {
        return mapperFor(ParameterizedTypeAccessor.<T>create(type));
    }

    public static <T> JsonMapper<T> mapperFor(final ParameterizedType<T> type) throws ClassLoaderDeadLockException {
        final Future<JsonMapper<T>> future = pool.submit(new Callable<JsonMapper<T>>() {
            @Override
            public JsonMapper<T> call() {
                return LoganSquare.mapperFor(type);
            }
        });
        final JsonMapper<T> mapper;
        //noinspection TryWithIdenticalCatches
        try {
            mapper = future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            }
            BugReporter.logException(e);
            throw new ClassLoaderDeadLockException(e);
        }
        return mapper;
    }

    public static class ClassLoaderDeadLockException extends IOException {
        public ClassLoaderDeadLockException() {
            super();
        }

        public ClassLoaderDeadLockException(String detailMessage) {
            super(detailMessage);
        }

        public ClassLoaderDeadLockException(String message, Throwable cause) {
            super(message, cause);
        }

        public ClassLoaderDeadLockException(Throwable cause) {
            super(cause);
        }
    }
}