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

package org.sakaiproject.content.impl.serialize.impl.conversion;

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
 */
public class SchemaConversionController
{

	private static final Log log = LogFactory.getLog(SchemaConversionController.class);

	public boolean migrate(DataSource datasource, SchemaConversionHandler convert)
	{
		boolean alldone = false;
		Connection connection = null;
		PreparedStatement selectNextBatch = null;
		PreparedStatement markNextBatch = null;
		PreparedStatement completeNextBatch = null;
		PreparedStatement selectRecord = null;
		PreparedStatement updateRecord = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			selectNextBatch = connection.prepareStatement(convert.getSelectNextBatch());
			markNextBatch = connection.prepareStatement(convert.getMarkNextBatch());
			completeNextBatch = connection.prepareStatement(convert
					.getCompleteNextBatch());
			selectRecord = connection.prepareStatement(convert.getSelectRecord());
			updateRecord = connection.prepareStatement(convert.getUpdateRecord());

			// we need some way of identifying those records that have not been
			// convertd.
			// 1. Create a register table to map progress.

			createRegisterTable(connection, convert);

			// 2. select x at a time
			rs = selectNextBatch.executeQuery();
			List<String> l = new ArrayList<String>();
			while (rs.next())
			{
				l.add(rs.getString(1));
			}
			rs.close();
			
			log.info("Migrating "+l.size()+" records ");
			
			for (String id : l)
			{
				
				markNextBatch.clearParameters();
				markNextBatch.setString(1, id);
				if (markNextBatch.executeUpdate() != 1)
				{
					log.warn("Failed to mark id [" + id + "][" +id.length()+"] for processing ");
				}
			}

			for (String id : l)
			{
				selectRecord.clearParameters();
				selectRecord.setString(1, id);
				rs = selectRecord.executeQuery();
				Object source = null;
				if (rs.next())
				{
					source = convert.getSource(id, rs);
				}
				rs.close();
				if (source != null)
				{
					updateRecord.clearParameters();
					if (convert.convertSource(id, source, updateRecord))
					{
						if (updateRecord.executeUpdate() != 1)
						{
							log.warn("Failed to update record " + id);
						}
					}
					else
					{
						log.warn("Did not update record " + id);
					}
				}
				completeNextBatch.clearParameters();
				completeNextBatch.setString(1, id);
				if (completeNextBatch.executeUpdate() != 1)
				{
					log.warn("Failed to mark id " + id + " for processing ");
				}

			}
			log.info("Done ");

			if (l.size() == 0)
			{
				dropRegisterTable(connection, convert);
				alldone = true;
			}
			connection.commit();

		}
		catch (SQLException e)
		{
			log.error("Failed to perform migration ",e);
			try
			{
				connection.rollback();
			}
			catch (Exception ex)
			{
			}

		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				selectNextBatch.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				markNextBatch.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				completeNextBatch.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				selectRecord.close();
			}
			catch (Exception ex)
			{

			}
			try
			{
				updateRecord.close();
			}
			catch (Exception ex)
			{

			}

			try
			{
				connection.close();

			}
			catch (Exception ex)
			{

			}

		}
		return alldone;
	}

	/**
	 * @throws SQLException
	 */
	private void dropRegisterTable(Connection connection, SchemaConversionHandler convert)
			throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			String sql = convert.getDropMigrateTable();
			stmt.execute(sql);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * @param connection
	 * @param convert
	 * @throws SQLException
	 */
	private void createRegisterTable(Connection connection,
			SchemaConversionHandler convert) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			long nrecords = 0;
			try
			{
				// select count(*) from content_migrate;
				String sql = convert.getCheckMigrateTable();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					nrecords = rs.getLong(1);
				}
			}
			catch (SQLException sqle)
			{
				String sql = convert.getCreateMigrateTable();
				stmt.execute(sql);
			}
			finally
			{
				try
				{
					rs.close();
				}
				catch (Exception ex)
				{
				}
			}
			if (nrecords == 0)
			{
				String sql = convert.getPopulateMigrateTable();
				stmt.executeUpdate(sql);
			}

		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

}
