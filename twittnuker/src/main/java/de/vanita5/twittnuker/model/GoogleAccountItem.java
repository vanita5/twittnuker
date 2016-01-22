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

package de.vanita5.twittnuker.model;

/**
 * Represents an account on the backend server
 */
public class GoogleAccountItem {

	public String accountId;

	public boolean nMentions;
	public boolean nDMs;
	public boolean nFollower;

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public boolean showMentions() {
		return nMentions;
	}

	public void setShowMentions(boolean nMentions) {
		this.nMentions = nMentions;
	}

	public boolean showDMs() {
		return nDMs;
	}

	public void setShowDMs(boolean nDMs) {
		this.nDMs = nDMs;
	}

	public boolean showFollower() {
		return nFollower;
	}

	public void setShowFollower(boolean nFollower) {
		this.nFollower = nFollower;
	}
}
