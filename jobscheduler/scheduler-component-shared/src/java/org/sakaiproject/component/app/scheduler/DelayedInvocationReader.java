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
