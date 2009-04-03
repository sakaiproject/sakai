/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.transaction.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.transaction.api.TransactionSequence;

/**
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.SequenceGeneratorDisabled
 */
public class TransactionSequenceImpl implements TransactionSequence
{

	private static final Log log = LogFactory.getLog(TransactionSequenceImpl.class);

	/**
	 * dependency
	 */
	private DataSource datasource;

	private long localId = System.currentTimeMillis();

	/**
	 * dependency
	 */
	private String name = "indexupdate";

	private boolean checked  = false;

	private boolean wrap =  false;

	private long maxValue = -1;

	private long minValue = 0;

	public void destroy()
	{

	}

	/**
	 * Loads the first transaction to initialize
	 */
	public void init()
	{
		
	}
	private void check() {
		if ( checked ) {
			return;
		}
		checked = true;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		long txid = minValue;
		try
		{
			connection = datasource.getConnection();
			stmt = connection.createStatement();
		
			rs = stmt.executeQuery("select txid " + " from search_transaction "
					+ " where txname = '" + name + "'");
			if (!rs.next())
			{
				stmt.executeUpdate("insert into "
						+ "search_transaction ( txid, txname ) " + "values ("+minValue+",'" + name
						+ "')");
				txid = minValue;
			} else {
				txid = rs.getLong(1);
			}
			connection.commit();
		}
		catch (SQLException ex)
		{
			log.error("Failed to check transaction table ", ex);
		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				stmt.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
		log.debug("Transaction Sequence " + getName() + " Started at " + txid);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionSequence#getNextId()
	 */
	public long getNextId()
	{
		check();
		Connection connection = null;
		PreparedStatement selectpst = null;
		PreparedStatement updatepst = null;
		PreparedStatement resetpst = null;
		ResultSet rs = null;

		try
		{
			connection = datasource.getConnection();
			selectpst = connection
					.prepareStatement("select txid from search_transaction where txname = '"
							+ name + "'");

			resetpst = connection
			.prepareStatement("update search_transaction set txid = "+minValue+"  where  txname = '"
					+ name + "'");
			updatepst = connection
					.prepareStatement("update search_transaction set txid = txid + 1  where  txname = '"
							+ name + "'");

			boolean success = false;
			long txid = 0;
			long retries = 0;
			while (!success)
			{
				updatepst.clearParameters();
				success = (updatepst.executeUpdate() == 1);
				if (!success)
				{
					connection.rollback();
					retries++;
				}
				else
				{
					// this works in a transaction since we read what we just
					// updated.
					// if the DB is non transactional this will not work
					rs = selectpst.executeQuery();
					if (rs.next())
					{
						txid = rs.getLong(1);
						if ( wrap && txid > maxValue  ) {
							resetpst.clearParameters();
							success = (resetpst.executeUpdate() == 1);
							if ( !success ) {
								throw new RuntimeException(
								"Failed to reset ");
							}
							success = false;
							
						}
					}
					else
					{
						log.error("Transaction Record has been removed");
					}
					rs.close();
				}
				if (retries > 10)
				{
					throw new RuntimeException(
							"Failed to get a transaction, retried 10 times ");
				}
			}
			connection.commit();
			return txid;

		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
			}
			log.error("Failed to get a transaction id ", ex);
			return -1;
		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				selectpst.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				updatepst.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				resetpst.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
		}
	}

	/**
	 * @return the datasource
	 */
	public DataSource getDatasource()
	{
		return datasource;
	}

	/**
	 * @param datasource
	 *        the datasource to set
	 */
	public void setDatasource(DataSource datasource)
	{
		this.datasource = datasource;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *        the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.indexer.api.TransactionSequence#getLocalId()
	 */
	public long getLocalId()
	{
		// this should be attomic
		long next = localId++;
		return next;
	}

	/**
	 * @return the maxValue
	 */
	public long getMaxValue()
	{
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(long maxValue)
	{
		this.maxValue = maxValue;
		wrap = true;
	}

	/**
	 * @return the minValue
	 */
	public long getMinValue()
	{
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(long minValue)
	{
		this.minValue = minValue;
	}


}
