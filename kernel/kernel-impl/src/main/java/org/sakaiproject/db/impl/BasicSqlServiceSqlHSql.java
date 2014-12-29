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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.db.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
	@Override
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
	@Override
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
	@Override
	public PreparedStatement setTimestamp(PreparedStatement pstmt, Timestamp timestamp, GregorianCalendar calendar, int pos) throws SQLException
	{
		pstmt.setTimestamp(pos, timestamp, null);
		return pstmt;
	}

	@Override
	public  PreparedStatement prepareAutoColumn(Connection conn, String sql, String autoColumn) throws SQLException
	{
		// HSQL does not support autoColumn
		return conn.prepareStatement(sql);
	}

	/**
	 * Extract the generated key since HSQL does not support getGeneratedKeys()
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Long getGeneratedKey(PreparedStatement pstmt, String sql) throws SQLException
	{
		String[] tokens = sql.trim().split("\\s+");

		// Only handle INSERT INTO table_name - ignore everything else
		if ( tokens.length >= 3 && "insert".equalsIgnoreCase(tokens[0]) && "into".equalsIgnoreCase(tokens[1]) ) {
			Statement kstmt = pstmt.getConnection().createStatement();

			// The use of IDENTITY() is reasonable since Sakai across the 
			// board only allows one autoColumn in the dbInsert() method
			// If it works, we get the right thing - if it fails, it 
			// will alert the developer. as they are developing
			ResultSet keys = kstmt.executeQuery("SELECT IDENTITY() FROM " + tokens[2]);
			if (keys.next()) {
				return Long.valueOf(keys.getLong(1));
			}
		}

		return null;
	}

}
