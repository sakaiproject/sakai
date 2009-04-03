/******************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *****************************************************************************/

package org.sakaiproject.api.app.scheduler;

import java.util.Date;


/**
 * A pea which represents a delayed invocation (a job which will cause a spring bean method 
 * to be executed at a specific time and date)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class DelayedInvocation {

	public String uuid;
	public Date date;
	public String componentId;
	public String contextId;


	public DelayedInvocation() { }

	/**
	 * @param uuid
	 * @param date
	 * @param componentId
	 * @param contextId
	 */
	public DelayedInvocation(String uuid, Date date, String componentId, String contextId) {
		this.uuid = uuid;
		this.date = date;
		this.componentId = componentId;
		this.contextId = contextId;
	}

	public String toString() {
		return("uuid: "+uuid+" date: "+date+" componentId: "+componentId+" contextId: "+contextId);
	}

}
