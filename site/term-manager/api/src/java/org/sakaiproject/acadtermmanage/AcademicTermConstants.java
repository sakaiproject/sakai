/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.acadtermmanage;

public final class AcademicTermConstants {
	
	// defined by Sakai:
	public static final String PROP_NAME_TERM_EID="term_eid";
	public static final String PROP_NAME_TERM_TITLE="term";
	
	
	// defined by myself, for posting events to the sakai eventservice:
	
	/** Name of event being posted to the sakai eventservice if an academic session has been updated. */
	public static final String EVENTSERVICE_EVENT_ACADEMICSESSION_UPDATE = "acadtermmanage.as.upd";

	/** Name of event being posted to the sakai eventservice if an academic session has been added. */
	public static final String EVENTSERVICE_EVENT_ACADEMICSESSION_ADD = "acadtermnanage.as.add";

	
	/** Prefix used to build resource references for academic session events.
	 *  The reference will be built by appending the updated academic session's EID to this prefix.
	 */
	// TODO look at the course management API if it already defines a prefix for its AcademicSessions
	public static final String EVENTSERVICE_EVENT_RESOURCE_PREFIX= "/academicsession/";

	private AcademicTermConstants() {
		// don't want anybody to instantiate this class because it's just supposed to be a collection of constants 
	}
}
