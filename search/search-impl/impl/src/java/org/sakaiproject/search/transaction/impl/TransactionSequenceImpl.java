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

package org.sakaiproject.search.transaction.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.transaction.api.TransactionSequence;

/**
 * @author ieb
 * Unit test @see org.sakaiproject.search.indexer.impl.test.SequenceGeneratorTest
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
	
	/**
	 * Loads the first transaction to initialize
	 *
	 */
	public void init() {
		log.debug("Transaction Sequece "+getName()+" Started at "+getNextId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionSequence#getNextId()
	 */
	public long getNextId()
	{
		Connection connection = null;
		PreparedStatement selectpst = null;
		PreparedStatement updatepst = null;
		PreparedStatement insertpst = null;
		ResultSet rs = null;

		try
		{
			connection = datasource.getConnection();
			selectpst = connection
					.prepareStatement("select txid from search_transaction where txname = '"+name +"'");
			insertpst = connection
					.prepareStatement("insert into search_transaction ( txid, txname ) values (0,'"+name+"')");
			updatepst = connection
					.prepareStatement("update search_transaction set txid = ? where txid = ? and txname = '"+name+"'");
			boolean success = false;
			long txid = 0;
			long retries = 0;
			while (!success)
			{
				rs = selectpst.executeQuery();
				if (!rs.next())
				{
					log.debug("Adding Seed transaction");
					insertpst.executeUpdate();
					rs.close();
					rs = selectpst.executeQuery();
					if ( !rs.next() ) {
						log.error("Failed to seed transaction counter ");
					}
				}
				txid = rs.getLong(1);
				rs.close();
				txid++;
				updatepst.clearParameters();
				updatepst.setLong(1, txid);
				updatepst.setLong(2, txid - 1);
				success = (updatepst.executeUpdate() == 1);
				if (retries > 100)
				{
					throw new RuntimeException(
							"Failed to get a transaction, retried 100 times ");
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
			}
			try
			{
				selectpst.close();
			}
			catch (Exception ex2)
			{
			}
			try
			{
				updatepst.close();
			}
			catch (Exception ex2)
			{
			}
			try
			{
				insertpst.close();
			}
			catch (Exception ex2)
			{
			}
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
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
	 * @param datasource the datasource to set
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
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.indexer.api.TransactionSequence#getLocalId()
	 */
	public long getLocalId()
	{
		// this should be attomic
		long next = localId++;
		return next;
	}

}
