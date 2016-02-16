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

package de.vanita5.twittnuker.test;

import com.bluelinelabs.logansquare.Constants;

import org.junit.Test;
import de.vanita5.twittnuker.api.twitter.model.DirectMessage;
import de.vanita5.twittnuker.api.twitter.model.HashtagEntity;
import de.vanita5.twittnuker.api.twitter.model.IDs;
import de.vanita5.twittnuker.api.twitter.model.MediaEntity;
import de.vanita5.twittnuker.api.twitter.model.SavedSearch;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.Trend;
import de.vanita5.twittnuker.api.twitter.model.Trends;
import de.vanita5.twittnuker.api.twitter.model.UrlEntity;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.api.twitter.model.UserList;
import de.vanita5.twittnuker.api.twitter.model.UserMentionEntity;
import de.vanita5.twittnuker.util.TwidereTypeUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DeadLockTest {

    private static final Executor sWatchDogExecutor = Executors.newCachedThreadPool();

    private static Class[] TEST_CLASSES = {Status.class, DirectMessage.class, User.class,
            UserList.class, Trend.class, Trends.class, SavedSearch.class, IDs.class,
            MediaEntity.class, UrlEntity.class, HashtagEntity.class, UserMentionEntity.class};

    @Test
    public void testWithoutSynchronization() throws Exception {
        for (Class testClass : TEST_CLASSES) {
            new Thread(new RunnableWithoutSynchronization(testClass)).start();
        }
    }

    @Test
    public void testWithSynchronization() {
//        for (Class testClass : TEST_CLASSES) {
//            new Thread(new RunnableWithSynchronization(testClass)).start();
//        }
    }

    static class RunnableWithSynchronization implements Runnable {

        private final Class<?> cls;

        RunnableWithSynchronization(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public void run() {
            final WatchdogRunnable runnable = new WatchdogRunnable(cls);
            sWatchDogExecutor.execute(runnable);
            try {
                synchronized (RunnableWithSynchronization.class) {
                    Class.forName(cls.getName() + Constants.MAPPER_CLASS_SUFFIX);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                runnable.finished();
            }
        }
    }

    static class RunnableWithoutSynchronization implements Runnable {

        private final Class<?> cls;

        RunnableWithoutSynchronization(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public void run() {
            final WatchdogRunnable runnable = new WatchdogRunnable(cls);
            sWatchDogExecutor.execute(runnable);
            try {
                Class.forName(cls.getName() + Constants.MAPPER_CLASS_SUFFIX);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                runnable.finished();
            }
        }
    }

    private static class WatchdogRunnable implements Runnable {
        private final Class<?> cls;
        private boolean finished;

        public WatchdogRunnable(Class<?> cls) {
            this.cls = cls;
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
                throw new RuntimeException("Too long waiting: " + TwidereTypeUtils.toSimpleName(cls));
            }
        }

        public synchronized void finished() {
            this.finished = true;
        }
    }

}