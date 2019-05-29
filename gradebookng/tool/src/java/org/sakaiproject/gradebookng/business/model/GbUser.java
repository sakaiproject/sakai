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

import lombok.experimental.Accessors;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.user.api.User;
import org.sakaiproject.gradebookng.business.util.FormatHelper;

import java.util.Collections;
import java.util.List;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Getter
public class GbUser implements Serializable, Comparable<GbUser> {

	private static final long serialVersionUID = 1L;

	private final String userUuid;

	/**
	 * If displaying an eid, this is the one to display
	 */
	private final String displayId;

	private final String displayName;

	private final String firstName;

	private final String lastName;

	private final String studentNumber;

	@Setter
	@Accessors(chain = true)
	private List<String> sections;

	public GbUser(final User u) {
		this(u, "");
	}

	public GbUser(final User u, String studentNumber) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = FormatHelper.htmlEscape(u.getDisplayName());
		this.firstName = FormatHelper.htmlEscape(u.getFirstName());
		this.lastName = FormatHelper.htmlEscape(u.getLastName());
		this.studentNumber = FormatHelper.htmlEscape(studentNumber);
		this.sections = Collections.emptyList();
	}

	public GbUser(final String userUUID, final String displayID, final String displayName, final String firstName, final String lastName, final String studentNumber) {
		this.userUuid = userUUID;
		this.displayId = displayID;
		this.displayName = FormatHelper.htmlEscape(displayName);
		this.firstName = FormatHelper.htmlEscape(firstName);
		this.lastName = FormatHelper.htmlEscape(lastName);
		this.studentNumber = FormatHelper.htmlEscape(studentNumber);
		this.sections = Collections.emptyList();
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
