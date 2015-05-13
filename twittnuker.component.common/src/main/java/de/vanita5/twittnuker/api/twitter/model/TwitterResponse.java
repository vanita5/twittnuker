/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vanita5.twittnuker.api.twitter.model;

import org.mariotaku.simplerestapi.http.RestResponse;

/**
 * Super interface of Twitter Response data interfaces which indicates that rate
 * limit status is avaialble.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @see DirectMessage
 * @see Status
 * @see User
 */
public interface TwitterResponse  {
	int NONE = 0;

	int READ = 1;

	int READ_WRITE = 2;
	int READ_WRITE_DIRECTMESSAGES = 3;

	void processResponseHeader(RestResponse resp);

	int getAccessLevel();

	RateLimitStatus getRateLimitStatus();

}