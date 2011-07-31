/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.db.api;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

import org.sakaiproject.exception.ServerOverloadException;

/**
 * <p>
 * ExtendedSqlService adds functionality to SqlService
 * </p>
 * <p>
 * The Connection objects managed by this service are standard java.sql.Connection objects.
 * </p>
 */
public interface ExtendedSqlService
{
	/**
	 * Execute the "write/update" sql - no response, using a set of fields from an array plus one more as params and connection.
	 * 
	 * @param sql
	 *        The sql statement.
	 * @param fields
	 *        The array of fields for parameters.
	 * @param lastField
	 *        The value to bind to the last parameter in the sql statement.
	 * @param callerConnection
	 *        The connection to use.
	 * @param failQuiet
	 *        If true, don't log errors from statement failure
	 * @return the number of records affected or -1 if something goes wrong if not due to unique constraint 
	 * violation or duplicate key (i.e. the record already exists) OR we are instructed to fail quiet.
	 */
	int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet);

}
