/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

package org.sakaiproject.assignment.impl.conversion.impl;

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
import org.sakaiproject.util.conversion.SchemaConversionHandler;
import org.sakaiproject.util.conversion.SchemaConversionException;

/**
 * @author ieb
 */
public class SchemaConversionController
{

	private static final Log log = LogFactory.getLog(SchemaConversionController.class);
	private long nrecords = 0;
	
	public void init(DataSource datasource, SchemaConversionHandler convert, SchemaConversionDriver driver) throws SchemaConversionException 
	{
		// we need some way of identifying those records that have not been
		// convertd.
		// 1. Create a register table to map progress.
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			addColumns(connection, convert, driver);
			createRegisterTable(connection, convert, driver);
			connection.commit();
		}
		catch (Exception e)
		{
			log.error(this + ":init Failed to perform migration setup ",e);
			try
			{
				connection.rollback();
				log.error(this + ":Rollback Sucessfull ",e);
			}
			catch (Exception ex)
			{
				log.error(this + ":Rollback Failed ",e);
			}
			throw new SchemaConversionException("Schema Conversion has been aborted due to earlier errors, please investigate ");

		}
		finally
		{

			try
			{
				connection.close();

			}
			catch (Exception ex)
			{
				log.error(this + ":init close " + ex.getMessage());
			}

		}

	}

	public boolean migrate(DataSource datasource, SchemaConversionHandler convert, SchemaConversionDriver driver) throws SchemaConversionException
	{
		boolean alldone = false;
		Connection connection = null;
		PreparedStatement selectNextBatch = null;
		PreparedStatement markNextBatch = null;
		PreparedStatement completeNextBatch = null;
		PreparedStatement selectRecord = null;
		PreparedStatement selectValidateRecord = null;
		PreparedStatement updateRecord = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			selectNextBatch = connection.prepareStatement(driver.getSelectNextBatch());
			markNextBatch = connection.prepareStatement(driver.getMarkNextBatch());
			completeNextBatch = connection.prepareStatement(driver
					.getCompleteNextBatch());
			selectRecord = connection.prepareStatement(driver.getSelectRecord());
			selectValidateRecord = connection.prepareStatement(driver.getSelectValidateRecord());
			updateRecord = connection.prepareStatement(driver.getUpdateRecord());


			// 2. select x at a time
			rs = selectNextBatch.executeQuery();
			List<String> l = new ArrayList<String>();
			while (rs.next())
			{
				l.add(rs.getString(1));
			}
			rs.close();
			log.info("Migrating "+l.size()+" records of "+nrecords);
			
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
				source = convert.getSource(id, rs);
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
					selectValidateRecord.clearParameters();
					selectValidateRecord.setString(1, id);
					rs = selectValidateRecord.executeQuery();
					Object result = null;
					if (rs.next())
					{
						result = convert.getValidateSource(id, rs);
					}
					rs.close();
					
					convert.validate(id, source, result);
					
				}
				completeNextBatch.clearParameters();
				completeNextBatch.setString(1, id);
				if (completeNextBatch.executeUpdate() != 1)
				{
					log.warn("Failed to mark id " + id + " for processing ");
				}

			}

			if (l.size() == 0)
			{
				dropRegisterTable(connection, convert, driver);
				alldone = true;
			}
			connection.commit();
			nrecords -= l.size();

		}
		catch (Exception e)
		{
			log.error(this + ":Failed to perform migration ",e);
			try
			{
				connection.rollback();
				log.error("Rollback Sucessfull ",e);
			}
			catch (Exception ex)
			{
				log.error(this + ":Rollback Failed ",e);
			}
			throw new SchemaConversionException("Schema Conversion has been aborted due to earlier errors, please investigate ");

		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate rs.close " + ex.getMessage());
			}
			try
			{
				selectNextBatch.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate selectNextBatch.close " + ex.getMessage());
			}
			try
			{
				markNextBatch.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate markNextBatch.close " + ex.getMessage());
			}
			try
			{
				completeNextBatch.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate completeNextBatch.close " + ex.getMessage());
			}
			try
			{
				selectRecord.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate selectRecord.close " + ex.getMessage());
			}
			try
			{
				selectValidateRecord.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate selectValidateRecord.close " + ex.getMessage());
			}
			try
			{
				updateRecord.close();
			}
			catch (Exception ex)
			{
				log.error(this + ":migrate updateRecord.close " + ex.getMessage());
			}

			try
			{
				connection.close();

			}
			catch (Exception ex)
			{
				log.error(this + ":migrate connection.close " + ex.getMessage());
			}

		}
		return !alldone;
	}


	/**
	 * @throws SQLException
	 */
	private void dropRegisterTable(Connection connection,
			SchemaConversionHandler convert, SchemaConversionDriver driver)
			throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			String[] sql = driver.getDropMigrateTable();
			if(sql == null)
			{
				log.info("No SQL provided to drop register table");											
			}
			else
			{
				for(String statement : sql)
				{
					if(statement == null || statement.trim().equals(""))
					{
						log.info("Encountered null SQL while dropping register table: " + statement);						
					}
					else
					{
						try
						{
							stmt.execute(statement);
						}
						catch(Exception e)
						{
							log.info(this + ":dropRegisterTable Unable to execute SQL while dropping register table: " + statement + e.getMessage());
						}
					}
				}
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
				log.error(this + ":dropRegisterTable " + ex.getMessage());
			}
		}
	}
	
	private void addColumns(Connection connection, SchemaConversionHandler convert, SchemaConversionDriver driver) throws SQLException
	{
		String[] names = driver.getNewColumnNames();
		String[] types = driver.getNewColumnTypes();
		String[] qualifiers = driver.getNewColumnQualifiers();
		
		if(names == null)
		{
			// do nothing
		}
		else
		{
			for(int i = 0; i < names.length; i++)
			{
				if(names[i] == null || names[i].trim().equals(""))
				{
					continue;
				}
				Statement stmt = null;
				ResultSet rs = null;
				try
				{
					stmt = connection.createStatement();
					String sql = driver.getTestNewColumn(names[i]);
					rs = stmt.executeQuery(sql);
					if(!rs.next())
					{
						stmt = connection.createStatement();
						sql = driver.getAddNewColumn(names[i], types[i], qualifiers[i]);
						stmt.execute(sql);
						log.info("added column: " + sql);
					}
					else
					{
						log.info("column exists: " + sql);
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
						log.error(this + ":addColumns " + ex.getMessage());
					}
				}
			}
		}
	}

	/**
	 * @param connection
	 * @param convert
	 * @throws SQLException
	 */
	private void createRegisterTable(Connection connection,
			SchemaConversionHandler convert, SchemaConversionDriver driver)
			throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			nrecords = 0;
			try
			{
				// select count(*) from content_migrate;
				String sql = driver.getCheckMigrateTable();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					nrecords = rs.getLong(1);
				}
			}
			catch (SQLException sqle)
			{
				log.error(this + ":crateRegisterTable SQLException " + sqle.getMessage());
				String[] sql = driver.getCreateMigrateTable();
				if(sql == null)
				{
					log.info("No SQL provided to create  register table");											
				}
				else
				{
					for(String statement : sql)
					{
						if(statement == null || statement.trim().equals(""))
						{
							log.info("Encountered null SQL while creating register table: " + statement);						
						}
						else
						{
							try
							{
								stmt.execute(statement);
								log.info("Created register table: " + statement);
							}
							catch(Exception e)
							{
								log.info(this+":createRegisterTable Unable to execute SQL while creating register table: " + statement);
							}
						}
					}
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
					log.error(this + ":createRegisterTable rs.close " + ex.getMessage());
				}
			}
			if (nrecords == 0)
			{
				String sql = driver.getPopulateMigrateTable();
				stmt.executeUpdate(sql);
			}

			try
			{
				// select count(*) from content_migrate;
				String sql = driver.getCheckMigrateTable();
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					nrecords = rs.getLong(1);
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
					log.error(this + ":createRegisterTable 2 rs.close " + ex.getMessage());
				}
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
				log.error(this + ":createRegisterTable stmt.close " + ex.getMessage());
			}
		}

	}

}


