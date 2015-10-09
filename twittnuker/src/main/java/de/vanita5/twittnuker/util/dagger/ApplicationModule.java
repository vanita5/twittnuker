/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.dagger;

import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ActivityTracker;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ReadStateManager;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final ActivityTracker activityTracker;
    private final AsyncTwitterWrapper asyncTwitterWrapper;
    private final ReadStateManager readStateManager;

    public ApplicationModule(TwittnukerApplication application) {
        activityTracker = new ActivityTracker();
        asyncTwitterWrapper = new AsyncTwitterWrapper(application);
        readStateManager = new ReadStateManager(application);
    }

    @Provides
    ActivityTracker provideActivityStack() {
        return activityTracker;
    }

    @Provides
    AsyncTwitterWrapper provideAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    @Provides
    ReadStateManager provideReadStateManager() {
        return readStateManager;
    }

    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }

    public AsyncTwitterWrapper getAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    public ReadStateManager getReadStateManager() {
        return readStateManager;
    }
}