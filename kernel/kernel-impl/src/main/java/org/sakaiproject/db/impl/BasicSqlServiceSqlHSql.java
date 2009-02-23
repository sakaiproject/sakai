/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.db.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

/**
 * methods for accessing sql service methods in a hypersonic sql database.
 */
public class BasicSqlServiceSqlHSql extends BasicSqlServiceSqlDefault
{
	/**
	 * returns whether the sql exception indicates that a record already exists in a table.
	 */
	public boolean getRecordAlreadyExists(SQLException ex)
	{
		// From HSQLDB src/org/hsqldb/resources/sql-error-messages.properties 
		//009=23000 Violation of unique index $$: duplicate value(s) for column(s) $$
		//104=23000 Violation of unique constraint $$: duplicate value(s) for column(s) $$
		return ex.getErrorCode() == -104 || ex.getErrorCode() == -9;
	}

	/**
	 * returns the sql statement which returns the next number in a sequence. <br/><br/>
	 * 
	 * @param table
	 *        name of table to read the sequence number from.
	 */
	public String getNextSequenceSql(String table)
	{
		return "SELECT NEXT VALUE FOR " + table + " FROM DUAL";
	}

	/**
	 * sets the value of a timestamp field in the specified column. <br/><br/>
	 * 
	 * @param pstmt
	 *        prepared statement
	 * @param timestamp
	 *        timestamp
	 * @param calendar
	 *        calendar
	 * @param pos
	 *        number of column of bytes field.
	 */
	public PreparedStatement setTimestamp(PreparedStatement pstmt, Timestamp timestamp, GregorianCalendar calendar, int pos) throws SQLException
	{
		pstmt.setTimestamp(pos, timestamp, null);
		return pstmt;
	}
}
