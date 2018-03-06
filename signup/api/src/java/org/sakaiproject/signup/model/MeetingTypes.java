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
 * <P>
 * This interface defines the constant types for event/meeting categories
 * </P>
 */
public interface MeetingTypes {

	/**
	 * this represents an event/meeting, which has many time slots and requires
	 * signup. It's more suitable to <b>One on One</b> tutorial meeting/event
	 */
	static final String INDIVIDUAL = "individual";

	/**
	 * This represents an event/meeting, which has only one time slot and
	 * requires signup
	 */
	static final String GROUP = "group";
	
	static final String CUSTOM_TIMESLOTS="custom_ts";

	/**
	 * This represents an event/meeting, which is an open session and signup is
	 * not required.
	 */
	static final String ANNOUNCEMENT = "announcement";
	
	static final String DAILY = "daily";
	
	static final String WEEKDAYS = "wkdays_mon-fri";
	
	static final String WEEKLY = "weekly";
	
	static final String BIWEEKLY = "biweekly";
	
	static final String ONCE_ONLY = "no_repeat";

}
