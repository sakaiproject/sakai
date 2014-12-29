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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

/**
 * methods for accessing sql service methods in a database.
 */
public class BasicSqlServiceSqlDefault implements SqlServiceSql
{
	/**
	 * returns whether the the database supports reading and updating blobs.
	 */
	public boolean canReadAndUpdateBlob()
	{
		return false;
	}

	/**
	 * returns a databases representation of the specified java boolean value.
	 */
	public String getBooleanConstant(boolean value)
	{
		return value ? "1" : "0";
	}

	/**
	 * returns the sql statement which returns the next number in a sequence. <br/><br/>
	 * 
	 * @param table
	 *        name of table to read the sequence number from.
	 */
	public String getNextSequenceSql(String table)
	{
		return null;
	}

	/**
	 * returns whether the sql exception indicates that a record already exists in a table.
	 */
	public boolean getRecordAlreadyExists(SQLException ex)
	{
		return false;
	}

	/**
	 * sets the value of a bytes field in the specified column. <br/><br/>
	 * 
	 * @param pstmt
	 *        prepared statement
	 * @param var
	 *        value to bind to the last parameter in the sql statement.
	 * @param pos
	 *        number of column of bytes field.
	 */
	public PreparedStatement setBytes(PreparedStatement pstmt, String var, int pos) throws SQLException
	{
		pstmt.setCharacterStream(pos, new StringReader(var), var.length());

		return pstmt;
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
		pstmt.setTimestamp(pos, timestamp, calendar);
		return pstmt;
	}

   /**
    *  set a null in the given statement at the given postion
    * @param pstmt
    * @param pos
    */
   public PreparedStatement setNull(PreparedStatement pstmt, int pos) throws SQLException {
      // treat a Java null as an SQL null,
      // and ALSO treat a zero-length Java string as an SQL null
      // This makes sure that Oracle vs MySQL use the same value
      // for null.
      pstmt.setObject(pos, null);
      return pstmt;
   }

   public PreparedStatement setBytes(PreparedStatement pstmt, byte[] bytes, int pos) throws SQLException {
      pstmt.setBytes(pos, bytes);
      return pstmt;
   }

   public boolean isDeadLockError(int errorCode){
        return false;
    }

	
	public PreparedStatement prepareAutoColumn(Connection conn, String sql, String autoColumn) throws SQLException
	{
		if (autoColumn != null)
		{
			String[] autoColumns = new String[1];
			autoColumns[0] = autoColumn;
			return conn.prepareStatement(sql, autoColumns);
		}
		else
		{
			return conn.prepareStatement(sql);
		}
	}

	/**
	 * Extract the generated key for JDBC drivers that support getGeneratedKeys()
	 * 
	 * {@inheritDoc}
	 */
	
	public Long getGeneratedKey(PreparedStatement pstmt, String sql) throws SQLException
	{
		ResultSet keys = pstmt.getGeneratedKeys();
		if (keys.next())
		{
			return Long.valueOf(keys.getLong(1));
		}
		return null;
	}

}
