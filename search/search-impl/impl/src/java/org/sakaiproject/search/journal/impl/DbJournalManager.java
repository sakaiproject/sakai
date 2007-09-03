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

package org.sakaiproject.search.journal.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalExhausetedException;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;

/**
 * A database backed Journal Manager
 * 
 * @author ieb
 */
public class DbJournalManager implements JournalManager
{

	/**
	 * @author ieb
	 */
	public class JournalManagerStateImpl implements JournalManagerState
	{

		public Connection connection;

	}

	private static final Log log = LogFactory.getLog(DbJournalManager.class);

	private DataSource datasource;

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
	 * @throws JournalErrorException
	 * @see org.sakaiproject.search.journal.api.JournalManager#getLaterVersions(long)
	 */
	public long getNextVersion(long version) throws JournalErrorException
	{
		Connection connection = null;
		PreparedStatement listLaterVersions = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			listLaterVersions = connection
					.prepareStatement("select txid from search_journal where txid > ? order by txid asc ");
			listLaterVersions.clearParameters();
			listLaterVersions.setLong(1, version);
			rs = listLaterVersions.executeQuery();
			if (rs.next())
			{
				return rs.getLong(1);
			}
			throw new JournalExhausetedException("No More versions available");
		}
		catch (SQLException ex)
		{
			log.error("Failed to retrieve list of journal items ", ex);
			throw new JournalErrorException("Journal Error ", ex);
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
				listLaterVersions.close();
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
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#prepareSave(long)
	 */
	public JournalManagerState prepareSave(long transactionId)
			throws IndexJournalException
	{
		PreparedStatement insertPst = null;
		JournalManagerStateImpl jms = new JournalManagerStateImpl();
		try
		{

			Connection connection = datasource.getConnection();
			jms.connection = connection;

			insertPst = connection
					.prepareStatement("insert into search_journal (txid, txts, indexwriter) values ( ?,?,?)");
			insertPst.clearParameters();
			insertPst.setLong(1, transactionId);
			insertPst.setLong(2, System.currentTimeMillis());
			insertPst.setString(3, String.valueOf(Thread.currentThread().getName()));
			if (insertPst.executeUpdate() != 1)
			{
				throw new IndexJournalException("Failed to update index journal");
			}

		}
		catch (IndexJournalException ijex)
		{
			throw ijex;
		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to transfer index ", ex);
		}
		finally
		{
			try
			{
				insertPst.close();
			}
			catch (Exception ex)
			{
			}
		}
		return jms;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#commitSave()
	 */
	public void commitSave(JournalManagerState jms) throws IndexJournalException
	{
		Connection connection = ((JournalManagerStateImpl) jms).connection;
		try
		{
			connection.commit();
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
			throw new IndexJournalException("Failed to commit index ", ex);
		}

	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#rollbackSave(org.sakaiproject.search.journal.api.JournalManagerState)
	 */
	public void rollbackSave(JournalManagerState jms)
	{
		Connection connection = ((JournalManagerStateImpl) jms).connection;
		try
		{

			connection.commit();

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

		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
			}

		}
	}

}
