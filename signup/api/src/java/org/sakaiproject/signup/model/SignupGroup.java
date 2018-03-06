/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.model;

/**
 * <p>
 * This class holds the information for signup group. It's mapped directly to
 * the DB storage by Hibernate
 * </p>
 */

public class SignupGroup {

	private String title;

	private String groupId;

	private String calendarEventId;

	private String calendarId;

	public String getGroupId() {
		return groupId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param groupId
	 *            a unique group Id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * get the title for the group
	 * 
	 * @return a title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * this is a setter.
	 * 
	 * @param title
	 *            a name for this group
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * get the Calendar Event Id
	 * 
	 * @return the Calendar Event Id
	 */
	public String getCalendarEventId() {
		return calendarEventId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param calendarEventId
	 *            the Calendar Event Id
	 */
	public void setCalendarEventId(String calendarEventId) {
		this.calendarEventId = calendarEventId;
	}

	/**
	 * get the Calendar Id
	 * 
	 * @return a Calendar Id
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * this is a setter.
	 * 
	 * @param calendarId
	 *            a unique calendar Id
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

}
