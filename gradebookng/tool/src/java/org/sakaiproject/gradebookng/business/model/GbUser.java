/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.user.api.User;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbUser implements Serializable, Comparable<GbUser> {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String userUuid;

	/**
	 * If displaying an eid, this is the one to display
	 */
	@Getter
	private final String displayId;

	@Getter
	private final String displayName;

	@Getter
	private final String firstName;

	@Getter
	private final String lastName;

	@Getter
	private final String studentNumber;

	public GbUser(final User u) {
		this(u, "");
	}

	public GbUser(final User u, String studentNumber) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
		this.firstName = u.getFirstName();
		this.lastName = u.getLastName();
		this.studentNumber = studentNumber;
	}

	public GbUser(final String userUUID, final String displayID, final String displayName, final String firstName, final String lastName, final String studentNumber) {
		this.userUuid = userUUID;
		this.displayId = displayID;
		this.displayName = displayName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.studentNumber = studentNumber;
	}

	public static GbUser forDisplayOnly(final String displayID, final String displayName) {
		return new GbUser("", displayID, displayName, "", "", "");
	}

	public boolean isValid() {
		return StringUtils.isNotBlank(userUuid);
	}

	@Override
	public int compareTo(GbUser user)
	{
		int comp = displayId.compareToIgnoreCase(user.displayId);
		if (comp == 0) {
			comp = displayName.compareToIgnoreCase(user.displayName);
		}

		return comp;
	}

	@Override
	public String toString() {
		return displayId;
	}
}
