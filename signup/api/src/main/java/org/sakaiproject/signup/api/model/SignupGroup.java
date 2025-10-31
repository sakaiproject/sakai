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

package org.sakaiproject.signup.api.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * <p>
 * This class holds the information for signup group. It's mapped directly to
 * the DB storage by Hibernate
 * </p>
 */
@Embeddable
@Getter
@Setter
public class SignupGroup {

	@Column(name = "title")
	private String title;

	@Column(name = "group_id", length = 99, nullable = false)
	private String groupId;

	@Column(name = "calendar_event_id", length = 2000)
	private String calendarEventId;

	@Column(name = "calendar_id", length = 99)
	private String calendarId;

}
