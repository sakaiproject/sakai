/******************************************************************************
 * DelayedInvocationReader.java - created by aaronz@vt.edu on Mar 19, 2007
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

package org.sakaiproject.component.app.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.db.api.SqlReader;

/**
 * An SQLReader so we can get the info out of the DB reasonably
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class DelayedInvocationReader implements SqlReader {

	private static final Log LOG = LogFactory.getLog(DelayedInvocationReader.class);

	public Object readSqlResultRecord(ResultSet result) {

		DelayedInvocation invocation = new DelayedInvocation();

		try {
			invocation.uuid = result.getString("INVOCATION_ID");
			invocation.date = result.getTimestamp("INVOCATION_TIME");
			invocation.componentId = result.getString("COMPONENT");
			invocation.contextId = result.getString("CONTEXT");
		} catch (SQLException e) {
			LOG.error("SqlException: " + e);
			return null;
		}

		return invocation;
	}

}
