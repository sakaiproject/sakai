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

package org.sakaiproject.search.optimize.shared.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.journal.impl.DbJournalManager.JournalManagerStateImpl;
import org.sakaiproject.search.optimize.api.NoOptimizationRequiredException;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * @author ieb
 */
public class DbJournalOptimizationManager implements JournalManager
{
	/***************************************************************************
	 * @author ieb
	 */

	private static final Log log = LogFactory.getLog(DbJournalOptimizationManager.class);

	private DataSource datasource;

	private ClusterService clusterService;

	private String serverId;

	private long journalOptimizeLimit;

	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		serverId = serverConfigurationService.getServerId();

	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#commitSave(org.sakaiproject.search.journal.api.JournalManagerState)
	 */
	public void commitSave(JournalManagerState jms) throws IndexJournalException
	{
		OptimizeJournalManagerStateImpl ojms = (OptimizeJournalManagerStateImpl) jms;
		Connection connection = ojms.connection;
		PreparedStatement success = null;
		try
		{
			success = connection
					.prepareStatement("delete from search_journal where indexwriter = ?  ");
			success.clearParameters();
			success.setString(1, ojms.indexWriter);
			success.executeUpdate();
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
		finally
		{
			try
			{
				success.close();
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
	 * @see org.sakaiproject.search.journal.api.JournalManager#getNextVersion(long)
	 */
	public long getNextVersion(long version) throws JournalErrorException
	{
		return 0;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#prepareSave(long)
	 */
	public JournalManagerState prepareSave(long transactionId)
			throws IndexJournalException
	{
		PreparedStatement getJournalVersionPst = null;
		PreparedStatement lockEarlierVersions = null;
		PreparedStatement listMergeSet = null;
		OptimizeJournalManagerStateImpl jms = new OptimizeJournalManagerStateImpl();
		ResultSet rs = null;
		Connection connection = null;
		boolean closeConnection = false;
		try
		{

			connection = datasource.getConnection();
			getJournalVersionPst = connection
					.prepareStatement("select serverid, jid from search_node_status order by jid asc ");

			jms.connection = connection;
			jms.indexWriter = serverId + ":" + transactionId;
			jms.transactionId = transactionId;
			jms.oldestVersion = 0;

			List<String> servers = clusterService.getServers();

			getJournalVersionPst.clearParameters();
			rs = getJournalVersionPst.executeQuery();
			while (jms.oldestVersion != 0 && rs.next())
			{
				String server = rs.getString(1);
				for (String s : servers)
				{
					if (server.equals(s))
					{
						jms.oldestVersion = rs.getLong(2);
						break;
					}
				}

			}

			if (jms.oldestVersion == 0)
			{
				throw new NoOptimizationRequiredException("Oldest version is 0");
			}
			rs.close();

			// this requires read committed transaction issolation and WILL NOT
			// work on HSQL
			lockEarlierVersions = connection
					.prepareStatement("update search_journal set indexwriter = ?, status = 'merging-prepare', txts = ? where txid <= ? and  status = 'commited' ");
			lockEarlierVersions.clearParameters();
			lockEarlierVersions.setString(1, jms.indexWriter);
			lockEarlierVersions.setLong(2, System.currentTimeMillis());
			lockEarlierVersions.setLong(3, jms.transactionId);
			lockEarlierVersions.executeUpdate();

			jms.mergeList = new ArrayList<Long>();

			listMergeSet = connection
					.prepareStatement("select txid from search_journal where indexwriter = ?  order by txid asc ");
			listMergeSet.clearParameters();
			listMergeSet.setString(1, jms.indexWriter);
			rs = listMergeSet.executeQuery();
			while (rs.next())
			{
				jms.mergeList.add(rs.getLong(1));
			}

		}
		catch (IndexJournalException ijex)
		{
			closeConnection = true;
			throw ijex;
		}
		catch (Exception ex)
		{
			closeConnection = true;
			throw new IndexJournalException("Failed to transfer index ", ex);
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
				getJournalVersionPst.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				lockEarlierVersions.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				listMergeSet.close();
			}
			catch (Exception ex)
			{
			}
			if ( closeConnection ) {
				try { connection.close(); } catch ( Exception ex ) {} 
			}
		}
		return jms;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#rollbackSave(org.sakaiproject.search.journal.api.JournalManagerState)
	 */
	public void rollbackSave(JournalManagerState jms)
	{
		if (jms != null)
		{
			Connection connection = ((JournalManagerStateImpl) jms).connection;
			try
			{

				connection.rollback();

			}
			catch (Exception ex)
			{
				log.error("Failed to Rollback");
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

	/**
	 * @throws IndexJournalException
	 * @throws IndexTransactionException
	 * @see org.sakaiproject.search.journal.api.JournalManager#doOpenTransaction(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void doOpenTransaction(IndexTransaction transaction)
			throws IndexJournalException
	{
		Statement countJournals = null;
		Connection connection = null;
		ResultSet rs = null;
		try
		{

			connection = datasource.getConnection();
			countJournals = connection.createStatement();
			rs = countJournals
					.executeQuery("select count(*) from  search_journal where  status = 'commited' ");
			long nVersions = 0;
			if (rs.next())
			{
				nVersions = rs.getLong(1);
			}
			rs.close();
			if (nVersions < journalOptimizeLimit)
			{
				throw new NoOptimizationRequiredException("Insufficient items to optimze");
			}
		}
		catch (NoOptimizationRequiredException nop)
		{
			throw nop;
		}
		catch (Exception ex)
		{
			log.warn("Failed to count available journals for optimization ", ex);
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
				countJournals.close();
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
	 * @return the clusterService
	 */
	public ClusterService getClusterService()
	{
		return clusterService;
	}

	/**
	 * @param clusterService
	 *        the clusterService to set
	 */
	public void setClusterService(ClusterService clusterService)
	{
		this.clusterService = clusterService;
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
	 * @return the journalOptimizeLimit
	 */
	public long getJournalOptimizeLimit()
	{
		return journalOptimizeLimit;
	}

	/**
	 * @param journalOptimizeLimit
	 *        the journalOptimizeLimit to set
	 */
	public void setJournalOptimizeLimit(long journalOptimizeLimit)
	{
		this.journalOptimizeLimit = journalOptimizeLimit;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService
	 *        the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

}
