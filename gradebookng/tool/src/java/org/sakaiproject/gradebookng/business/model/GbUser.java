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

import org.sakaiproject.user.api.User;

import lombok.Getter;

/**
 * DTO for a user. Enhance as required.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbUser implements Serializable {

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

	public GbUser(final User u) {
		this.userUuid = u.getId();
		this.displayId = u.getDisplayId();
		this.displayName = u.getDisplayName();
	}

}
