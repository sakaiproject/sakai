/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/conversion/SchemaConversionController.java $
 * $Id: SchemaConversionController.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class SchemaConversionController
{
	private boolean reportErrorsInTable = false;

	private long nrecords = 0;

	public void init(DataSource datasource, SchemaConversionHandler convert,
			SchemaConversionDriver driver) throws SchemaConversionException
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
			createErrorTable(connection, convert, driver);
			connection.commit();
		}
		catch (Exception e)
		{
			log.error("Failed to perform migration setup ", e);
			try
			{
				connection.rollback();
				log.error("Rollback Sucessfull ", e);
			}
			catch (Exception ex)
			{
				log.error("Rollback Failed ", e);
			}
			throw new SchemaConversionException(
					"Schema Conversion has been aborted due to earlier errors, please investigate ");

		}
		finally
		{

			try
			{
				connection.close();

			}
			catch (Exception ex)
			{
				log.debug("exception closing connection " + ex);
			}

		}

	}

	private void createErrorTable(Connection connection,
			SchemaConversionHandler convert, SchemaConversionDriver driver) 
	{
		String errorReportSql = driver.getErrorReportSql();
		String verifyErrorTable = driver.getVerifyErrorTable();
		String createErrorTable = driver.getCreateErrorTable();
		
		if(createErrorTable != null && errorReportSql != null && verifyErrorTable != null)
		{
			PreparedStatement verifyTable = null;
			PreparedStatement createTable = null;
			ResultSet rs = null;
			try 
			{
				// reportErrorsInTable should be true if table already exists or is created 
				verifyTable = connection.prepareStatement(verifyErrorTable);
				rs = verifyTable.executeQuery();
				boolean tableExists = rs.next();

				if(!tableExists)
				{

					createTable = connection.prepareStatement(createErrorTable);
					createTable.execute();
				}
				reportErrorsInTable = true;
			} 
			catch (SQLException e) 
			{
				log.error(e.getMessage(), e);
			}
			finally {
				if (rs != null)
				{
					try
					{
						rs.close();
					}
					catch (SQLException e)
					{
					}
				}

				if (verifyTable != null)
				{
					try
					{
						verifyTable.close();
					}
					catch (SQLException e)
					{
					}
				}

				if (createTable != null)
				{
					try
					{
						createTable.close();
					}
					catch (SQLException e)
					{
					}
				}
			} //END Finally
		} //END if
	}

	public boolean migrate(DataSource datasource, SchemaConversionHandler convert,
			SchemaConversionDriver driver) throws SchemaConversionException
	{
		// issues:
		// Data size bigger than max size for this type?
		// Failure may cause rest of set to fail?
		
		boolean alldone = false;
		Connection connection = null;
		PreparedStatement selectNextBatch = null;
		PreparedStatement markNextBatch = null;
		PreparedStatement completeNextBatch = null;
		PreparedStatement selectRecord = null;
		PreparedStatement selectValidateRecord = null;
		PreparedStatement updateRecord = null;
		PreparedStatement reportError = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			connection.setAutoCommit(false);
			selectNextBatch = connection.prepareStatement(driver.getSelectNextBatch());
			markNextBatch = connection.prepareStatement(driver.getMarkNextBatch());
			completeNextBatch = connection
					.prepareStatement(driver.getCompleteNextBatch());
			String selectRecordStr = driver.getSelectRecord();
			selectRecord = connection.prepareStatement(selectRecordStr);
			selectValidateRecord = connection.prepareStatement(driver
					.getSelectValidateRecord());
			updateRecord = connection.prepareStatement(driver.getUpdateRecord());
			if(reportErrorsInTable)
			{
				reportError = connection.prepareStatement(driver.getErrorReportSql());
			}
			// log.info("  +++ updateRecord == " + driver.getUpdateRecord());

			// 2. select x at a time
			rs = selectNextBatch.executeQuery();
			List<String> l = new ArrayList<String>();
			while (rs.next())
			{
				l.add(rs.getString(1));
			}
			rs.close();
			log.info("Migrating " + l.size() + " records of " + nrecords);

			for (String id : l)
			{

				markNextBatch.clearParameters();
				markNextBatch.clearWarnings();
				markNextBatch.setString(1, id);
				if (markNextBatch.executeUpdate() != 1)
				{
					log.warn("  --> Failed to mark id [" + id + "][" + id.length()
							+ "] for processing ");
					insertErrorReport(reportError, id, driver.getHandler(), "Unable to mark this record for processing");
				}
			}

			int count = 1;
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
				else
				{
					log.warn("  --> Result-set is empty for id: " + id + " [" + count + " of " + l.size() + "]");
					insertErrorReport(reportError, id, driver.getHandler(), "Result set empty getting source");
				}
				rs.close();
				if (source == null)
				{
					log.warn("  --> Source is null for id: " + id + " [" + count + " of " + l.size() + "]");
					insertErrorReport(reportError, id, driver.getHandler(), "Source null");
				}
				else
				{
					try
					{
						updateRecord.clearParameters();
						if (convert.convertSource(id, source, updateRecord))
						{
							if (updateRecord.executeUpdate() == 1)
							{
								selectValidateRecord.clearParameters();
								selectValidateRecord.setString(1, id);
								rs = selectValidateRecord.executeQuery();
								Object result = null;
								if (rs.next())
								{
									result = convert.getValidateSource(id, rs);
								}
								
								convert.validate(id, source, result);
							}
							else
							{
								log.warn("  --> Failed to update record " + id + " [" + count + " of " + l.size() + "]");
								insertErrorReport(reportError, id, driver.getHandler(), "Failed to update record");
							}
						}
						else
						{
							log.warn("  --> Did not update record " + id + " [" + count + " of " + l.size() + "]");
							insertErrorReport(reportError, id, driver.getHandler(), "Failed to write update to db");
						}
						rs.close();
					}
					catch(SQLException e)
					{
						String msg = "  --> Failure converting or validating item " + id + " [" + count + " of " + l.size() + "] \n";
						insertErrorReport(reportError, id, driver.getHandler(), "Exception while updating, converting or verifying item");
						SQLWarning warnings = updateRecord.getWarnings();
						while(warnings != null)
						{
							msg += "\t\t\t" + warnings.getErrorCode() + "\t" + warnings.getMessage() + "\n";
							warnings = warnings.getNextWarning();
						}
						log.warn(msg,e);
						updateRecord.clearWarnings();
						updateRecord.clearParameters();
					}

				}
				completeNextBatch.clearParameters();
				completeNextBatch.setString(1, id);
				if (completeNextBatch.executeUpdate() != 1)
				{
					log.warn("  --> Failed to mark id " + id + " for processing [" + count + " of " + l.size() + "]");
					insertErrorReport(reportError, id, driver.getHandler(), "Unable to complete next batch");
				}
				count++;
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
			log.error("Failed to perform migration ", e);
			try
			{
				connection.rollback();
				log.error("  ==> Rollback Sucessful ", e);
			}
			catch (Exception ex)
			{
				log.error("  ==> Rollback Failed ", e);
			}
			throw new SchemaConversionException(
					"Schema Conversion has been aborted due to earlier errors, please investigate ");

		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing rs " + ex);
			}
			try
			{
				selectNextBatch.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing selectNextBatch " + ex);
			}
			try
			{
				markNextBatch.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing markNextBatch " + ex);
			}
			try
			{
				completeNextBatch.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing completeNextBatch " + ex);
			}
			try
			{
				selectRecord.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing selectRecord " + ex);
			}
			try
			{
				selectValidateRecord.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing selectValidateRecord " + ex);
			}
			try
			{
				updateRecord.close();
			}
			catch (Exception ex)
			{
				log.debug("exception closing updateRecord " + ex);
			}
			if(reportError != null)
			{
				try
				{
					reportError.close();
				}
				catch (Exception ex)
				{
					log.debug("exception closing reportError " + ex);
				}
			}

			try
			{
				connection.close();

			}
			catch (Exception ex)
			{
				log.debug("Exception closing connection " + ex);
			}

		}
		return !alldone;
	}

	private void insertErrorReport(PreparedStatement reportError, String id,
			String handler, String description) 
	{
		if(reportError != null)
		{
			try 
			{
				reportError.clearParameters();
				reportError.setString(1, id);
				reportError.setString(2, handler);
				reportError.setString(3, description);
				reportError.execute();
			} 
			catch (SQLException e) 
			{
				log.warn("Unable to insert error report [" + id + " " + handler + " \"" + description + "\" " + e);
			}
		}	
	}

	/**
	 * @throws SQLException
	 */
	private void dropRegisterTable(Connection connection,
			SchemaConversionHandler convert, SchemaConversionDriver driver)
			throws SQLException
	{
		Statement stmt = null;
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
							log.info("Cleaning up: " + statement);
							stmt.execute(statement);
						}
						catch(Exception e)
						{
							log.info("Unable to execute SQL while dropping register table: " + statement);
						}
					}
				}
				log.info("Done cleaning up conversion step");
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
				log.debug("exception closing stmt " + ex);
			}
		}
	}

	private void addColumns(Connection connection, SchemaConversionHandler convert,
			SchemaConversionDriver driver) throws SQLException
	{
		String[] names = driver.getNewColumnNames();
		String[] types = driver.getNewColumnTypes();
		String[] qualifiers = driver.getNewColumnQualifiers();

		if (names == null)
		{
			// do nothing
		}
		else
		{
			for (int i = 0; i < names.length; i++)
			{
				if (names[i] == null || names[i].trim().equals(""))
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
					if (!rs.next())
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
						log.debug("exception closing smt " + ex);
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
								log.info("Unable to execute SQL while creating register table: " + statement);
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
					log.debug("exception closing rs " + ex);
				}
			}
			if (nrecords == 0)
			{
				String sql = driver.getPopulateMigrateTable();
				if(sql == null)
				{
					log.info("No SQL to populate register table");					
				}
				else
				{
					log.info("Populating register table: " + sql);
					int count = stmt.executeUpdate(sql);
					log.info("Inserted " + count + " rows into register table");
				}
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
				log.info("Counted " + nrecords + " rows in register table");
			}
			catch(Exception e)
			{
				log.debug("Unable to verify number of rows in register table");
			}
			finally
			{
				try
				{
					rs.close();
				}
				catch (Exception ex)
				{
					log.debug("exception closing rs " + ex);
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
				log.debug("exception closing stmt " + ex);
			}
		}

	}

}
