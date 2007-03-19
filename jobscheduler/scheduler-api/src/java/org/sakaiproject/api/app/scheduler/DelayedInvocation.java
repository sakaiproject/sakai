/******************************************************************************
 * DelayedInvocation.java - created by aaronz@vt.edu on Mar 19, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
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
