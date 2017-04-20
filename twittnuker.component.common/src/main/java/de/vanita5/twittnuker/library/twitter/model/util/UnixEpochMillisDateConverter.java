/*
 *          Twittnuker - Twitter client for Android
 *
 *  Copyright 2013-2017 vanita5 <mail@vanit.as>
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 *  Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.vanita5.twittnuker.library.twitter.model.util;

import com.bluelinelabs.logansquare.typeconverters.LongBasedTypeConverter;

import java.util.Date;


public class UnixEpochMillisDateConverter extends LongBasedTypeConverter<Date> {
    @Override
    public Date getFromLong(final long l) {
        return new Date(l);
    }

    @Override
    public long convertToLong(final Date object) {
        if (object == null) return -1;
        return object.getTime();
    }

}