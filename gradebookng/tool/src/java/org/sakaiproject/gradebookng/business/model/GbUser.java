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
import lombok.EqualsAndHashCode;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GbUser implements GbUserBase, Serializable, Comparable<GbUser> {

	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	private final String userUuid;

	/**
	 * If displaying an eid, this is the one to display
	 */
	private final String displayId;

	private final String displayName;

	private final String firstName;

	private final String lastName;

	private final String studentNumber;

	private final String sortName;

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
		this.sortName = u.getSortName();
	}

	public boolean isValid() {
		return StringUtils.isNotBlank(userUuid);
	}

	@Override
	public int compareTo(GbUser other)
	{
		String prop1 = sortName;
		String prop2 = other.getSortName();
		if (StringUtils.isBlank(prop1) && StringUtils.isBlank(prop2)) {
			prop1 = displayName;
			prop2 = other.getDisplayName();
		}
		if (StringUtils.equalsIgnoreCase(prop1, prop2)) {
			prop1 = displayId;
			prop2 = other.getDisplayId();
		}

		return StringUtils.compareIgnoreCase(prop1, prop2);
	}

	@Override
	public String toString() {
		return displayId;
	}
}
