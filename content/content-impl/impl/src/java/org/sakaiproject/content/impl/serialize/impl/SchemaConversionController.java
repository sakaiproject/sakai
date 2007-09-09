/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.serialize.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 *
 */
public class SchemaConversionController
{
	
	private static final Log log = LogFactory.getLog(SchemaConversionController.class);
	

	public void migrate(DataSource datasource, SchemaConversionHandler convert) {
		Connection  connection = null;
		PreparedStatement selectNextBatch = null;
		PreparedStatement markNextBatch = null;
		PreparedStatement completeNextBatch = null;
		PreparedStatement selectRecord = null;
		PreparedStatement updateRecord = null;
		ResultSet rs = null;
		try {
			connection = datasource.getConnection();
			selectNextBatch = connection.prepareStatement(convert.getSelectNextBatch());
			markNextBatch = connection.prepareStatement(convert.getMarkNextBatch());
			completeNextBatch = connection.prepareStatement(convert.getCompleteNextBatch());
			selectRecord = connection.prepareStatement(convert.getSelectRecord());
			updateRecord = connection.prepareStatement(convert.getUpdateRecord());

			// we need some way of identifying those records that have not been convertd.
			// 1. Create a register table to map progress.
			
			createRegisterTable(connection,convert);
			

			
			// 2. select x at a time
			rs = selectNextBatch.executeQuery();
			List<String> l = new ArrayList<String>();
			while ( rs.next() ) {
				l.add(rs.getString(1));
			}
			rs.close();
			for ( String id : l) {
				markNextBatch.clearParameters();
				markNextBatch.setString(1, id);
				if ( markNextBatch.executeUpdate() != 1) {
					throw new SQLException("Failed to mark id "+id+" for processing ");
				}
			}

			for (String id : l ) {
				selectRecord.clearParameters();
				selectRecord.setString(1, id);
				rs = selectRecord.executeQuery();
				Object source = null;
				if ( rs.next() ) {
					source = convert.getSource(id,rs);
				}
				rs.close();
				if ( source != null ) {
					updateRecord.clearParameters();
					convert.convertSource(id,source,updateRecord);
					if ( updateRecord.executeUpdate() != 1 ) {
						throw new SQLException("Failed to update record "+id);
					}
				}
				completeNextBatch.clearParameters();
				completeNextBatch.setString(1, id);
				if ( completeNextBatch.executeUpdate() != 1) {
					throw new SQLException("Failed to mark id "+id+" for processing ");
				}
				
			}
			
			
			
			if ( l.size() == 0 ) {
				dropRegisterTable(connection,convert);
			}
			
		}
		catch (SQLException e)
		{
			log.error("Failed to perform migration ");
			try { connection.rollback(); } catch ( Exception ex ) {}
			
		} finally {
			
			try {
				connection.close();
				
			}catch ( Exception ex ) {
				
			}
		}
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void dropRegisterTable(Connection connection, SchemaConversionHandler convert) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			stmt.execute(convert.getDropMigrateTable());
		}finally {
			try { stmt.close(); } catch ( Exception ex ) {}
		}
	}

	/**
	 * @param connection
	 * @param convert 
	 * @throws SQLException 
	 */
	private void createRegisterTable(Connection connection, SchemaConversionHandler convert) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			long nrecords = 0;
			try {
				// select count(*) from content_migrate; 
				rs =  stmt.executeQuery(convert.getCheckMigrateTable());
				if ( rs.next() ) {
					nrecords = rs.getLong(1);
				}
			} catch ( SQLException sqle  ) {
				stmt.execute(convert.getCreateMigrateTable());
			} finally {
				try { rs.close(); } catch ( Exception ex ) {}
			}
			if ( nrecords == 0 ) {
				stmt.executeUpdate(convert.getPopulateMigrateTable());
			}
			
			
		}finally {
			try { stmt.close(); } catch ( Exception ex ) {}
		}
		
	}

}
