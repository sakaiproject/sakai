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
import org.sakaiproject.search.journal.impl.JournalSettings;
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

	private JournalSettings journalSettings;

	private ServerConfigurationService serverConfigurationService;

	public void destroy()
	{

	}

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
		Connection connection = null;
		PreparedStatement success = null;
		PreparedStatement updateTarget = null;
		try
		{
			// set the target to committed and then delete the rest
			connection = datasource.getConnection();
			updateTarget = connection
					.prepareStatement("update search_journal set status = 'commited', txts = ? where txid = ?  ");
			updateTarget.clearParameters();
			updateTarget.setLong(1, System.currentTimeMillis());
			updateTarget.setLong(2, ojms.oldestSavePoint);
			int i = updateTarget.executeUpdate();

			// clear out all others
			success = connection
					.prepareStatement("delete from search_journal where indexwriter = ? and status = 'merging-prepare'  ");
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
				updateTarget.close();
			}
			catch (Exception ex)
			{
			}
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
	 * @see org.sakaiproject.search.journal.api.JournalManager#getNextSavePoint(long)
	 */
	public long getNextSavePoint(long savePoint) throws JournalErrorException
	{
		return 0;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#prepareSave(long)
	 */
	public JournalManagerState prepareSave(long transactionId)
			throws IndexJournalException
	{
		PreparedStatement getJournalSavePointPst = null;
		PreparedStatement lockEarlierSavePoints = null;
		PreparedStatement listMergeSet = null;
		PreparedStatement listJournal = null;
		OptimizeJournalManagerStateImpl jms = new OptimizeJournalManagerStateImpl();
		ResultSet rs = null;
		Connection connection = null;
		try
		{

			connection = datasource.getConnection();
			getJournalSavePointPst = connection
					.prepareStatement("select serverid, jid from search_node_status order by jid asc ");

			jms.indexWriter = serverId + ":" + transactionId;
			jms.transactionId = transactionId;
			jms.oldestSavePoint = 0;

			List<String> servers = clusterService.getServers();

			getJournalSavePointPst.clearParameters();
			rs = getJournalSavePointPst.executeQuery();
			while (jms.oldestSavePoint == 0 && rs.next())
			{
				String server = rs.getString(1);
				log.debug("Got Server " + server + " with savePoint " + rs.getLong(2));
				for (String s : servers)
				{
					int dash = s.lastIndexOf('-');
					if ( dash > 0  ) {
						s = s.substring(0,dash);
					} 
					if (server.equals(s))
					{
						jms.oldestSavePoint = rs.getLong(2);
						log.debug("	Match against "+s);
						break;
					} else {
						log.debug("No Match against "+s);
					}
				}

			}
			jms.oldestSavePoint--;
			if (jms.oldestSavePoint <= 0)
			{
				connection.rollback();
				throw new NoOptimizationRequiredException("Oldest savePoint is 0");
			}
			rs.close();

			log.info("Optimizing shared Journal Storage to savepoint "
					+ jms.oldestSavePoint);

			// this requires read committed transaction issolation and WILL NOT
			// work on HSQL
			lockEarlierSavePoints = connection
					.prepareStatement("update search_journal set indexwriter = ?, status = 'merging-prepare', txts = ? where txid <= ? and  status = 'commited' ");
			lockEarlierSavePoints.clearParameters();
			lockEarlierSavePoints.setString(1, jms.indexWriter);
			lockEarlierSavePoints.setLong(2, System.currentTimeMillis());
			lockEarlierSavePoints.setLong(3, jms.oldestSavePoint);
			int i = lockEarlierSavePoints.executeUpdate();

			listJournal = connection
					.prepareStatement("select txid, indexwriter, status, txts  from search_journal");
			listJournal.clearParameters();
			rs = listJournal.executeQuery();
			while (rs.next())
			{
				log.info("TX[" + rs.getLong(1) + "];indexwriter[" + rs.getString(2)
						+ "];status[" + rs.getString(3) + "];timestamp[" + rs.getLong(4)
						+ "]");
			}
			rs.close();

			if (i < journalSettings.getMinimumOptimizeSavePoints())
			{
				connection.rollback();
				throw new NoOptimizationRequiredException(
						"Insuficient Journal Entries prior to savepoint "
								+ jms.oldestSavePoint + " to optimize, found " + i);
			}

			log.info("Locked " + i + " savePoints ");

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
			log.info("Retrieved " + jms.mergeList.size() + " locked savePoints ");

			connection.commit();
		}
		catch (IndexJournalException ijex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
			}
			throw ijex;
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
			throw new IndexJournalException("Failed to lock savePoints to this node ", ex);
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
				getJournalSavePointPst.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				lockEarlierSavePoints.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				listJournal.close();
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
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
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
			long nSavePoints = 0;
			if (rs.next())
			{
				nSavePoints = rs.getLong(1);
			}
			rs.close();
			if (nSavePoints < journalSettings.getMinimumOptimizeSavePoints())
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

	/**
	 * @return the journalSettings
	 */
	public JournalSettings getJournalSettings()
	{
		return journalSettings;
	}

	/**
	 * @param journalSettings
	 *        the journalSettings to set
	 */
	public void setJournalSettings(JournalSettings journalSettings)
	{
		this.journalSettings = journalSettings;
	}

}
