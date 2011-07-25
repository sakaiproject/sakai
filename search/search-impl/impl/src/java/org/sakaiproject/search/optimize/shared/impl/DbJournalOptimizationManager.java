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

package org.sakaiproject.search.optimize.shared.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.LockTimeoutException;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.journal.impl.JournalSettings;
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
		PreparedStatement success = null;
		PreparedStatement updateTarget = null;
		Connection connection = null;
		try
		{
			System.err.println("+++++++++++++++++++++COMMIT+++++++++++++++");
			connection = datasource.getConnection();
			// set the target to committed and then delete the rest
			// so the final segment becomes commtted with a writer id of
			// this+txiD,
			// and all merging-prepare states in this transaction are removed.
			updateTarget = connection
					.prepareStatement("update search_journal set status = 'committed', txts = ? where txid = ?  ");
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
			log.info("Shared Journal Mege Committed into SavePoint "
					+ ojms.oldestSavePoint);
		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
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
				log.debug(ex);
			}
			try
			{
				success.close();
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

	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#getNextSavePoint(long)
	 */
	public long getNextSavePoint(long savePoint) throws JournalErrorException
	{
		return 0;
	}

	/**
	 * @throws LockTimeoutException
	 * @see org.sakaiproject.search.journal.api.JournalManager#prepareSave(long)
	 */
	public JournalManagerState prepareSave(long transactionId)
			throws IndexJournalException
	{
		PreparedStatement getJournalSavePointPst = null;
		PreparedStatement getEarlierSavePoint = null;
		PreparedStatement getEarlierSavePoint2 = null;
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
			getEarlierSavePoint = connection
					.prepareStatement("select serverid, jid from search_node_status order by jid asc ");

			jms.indexWriter = serverId + ":" + transactionId;
			jms.transactionId = transactionId;
			jms.oldestSavePoint = 0;

			List<String> servers = clusterService.getServers();

			// workout the oldest SavePoint that has not yet been merged
			// by any running cluster node.
			// this assumes that all the cluster nodes are running.
			// Any that are not running will have to be restarted in a clean
			// state
			// so that they update from scratch.

			getJournalSavePointPst.clearParameters();
			rs = getJournalSavePointPst.executeQuery();
			long oldestActiveSavepoint = 0;
			while (oldestActiveSavepoint == 0 && rs.next())
			{
				String server = rs.getString(1);
				log.debug("Got Server " + server + " with savePoint " + rs.getLong(2));
				for (String s : servers)
				{
					int dash = s.lastIndexOf('-');
					if (dash > 0)
					{
						s = s.substring(0, dash);
					}
					if (server.equals(s))
					{
						oldestActiveSavepoint = rs.getLong(2);
						log.debug("	Match against " + s);
						break;
					}
					else
					{
						log.debug("No Match against " + s);
					}
				}

			}
			rs.close();
			//SRCh-38 we will also select on the old mispelled value for backward compatability
			getEarlierSavePoint2 = connection
					.prepareStatement("select min(txid),max(txid) from search_journal where txid < ? and  (status = 'commited' or status = 'committed') ");
			getEarlierSavePoint2.clearParameters();
			getEarlierSavePoint2.setLong(1, oldestActiveSavepoint);
			rs = getEarlierSavePoint2.executeQuery();
			jms.oldestSavePoint = 0;
			long earliestSavePoint = 0;
			if (rs.next())
			{
				earliestSavePoint = rs.getLong(1);
				jms.oldestSavePoint = rs.getLong(2);
			}

			if (jms.oldestSavePoint <= 0)
			{
				throw new NoOptimizationRequiredException("Oldest savePoint is 0");
			}
			rs.close();
			
			long nshared = jms.oldestSavePoint - earliestSavePoint;
			
			// no point in going further there are not enough segments.
			if (nshared < journalSettings.getMinimumOptimizeSavePoints())
			{
				throw new NoOptimizationRequiredException(
						"Insuficient Journal Entries prior to savepoint "
								+ jms.oldestSavePoint + " to optimize, found " + nshared);
			}
			
			// too many ?
			if ( nshared > 2*journalSettings.getMinimumOptimizeSavePoints() ) {
				// adjust the oldestSavePoint
				// the number will be less than this if there are holes
				jms.oldestSavePoint = earliestSavePoint + 2*journalSettings.getMinimumOptimizeSavePoints();
				// adjust for a potential hole
				getEarlierSavePoint2.setLong(1, jms.oldestSavePoint);
				rs = getEarlierSavePoint2.executeQuery();
				jms.oldestSavePoint = 0;
				earliestSavePoint = 0;
				if (rs.next())
				{
					earliestSavePoint = rs.getLong(1);
					jms.oldestSavePoint = rs.getLong(2);
				}
				if (jms.oldestSavePoint <= 0)
				{
					throw new NoOptimizationRequiredException("Oldest savePoint is 0");
				}
				rs.close();
				
				
			}

			log.debug("Optimizing shared Journal Storage to savepoint "
					+ jms.oldestSavePoint);

			// this requires read committed transaction issolation and WILL NOT
			// work on HSQL
			lockEarlierSavePoints = connection
					.prepareStatement("update search_journal set indexwriter = ?, status = 'merging-prepare', txts = ? where txid <= ? and  (status = 'commited' or status = 'committed' ) ");
			lockEarlierSavePoints.clearParameters();
			lockEarlierSavePoints.setString(1, jms.indexWriter);
			lockEarlierSavePoints.setLong(2, System.currentTimeMillis());
			lockEarlierSavePoints.setLong(3, jms.oldestSavePoint);
			int i = 0;
			try
			{
				i = lockEarlierSavePoints.executeUpdate();
			}
			catch (SQLException lockTimepout)
			{
				throw new LockTimeoutException(lockTimepout.getMessage(), lockTimepout);
			}
			listJournal = connection
					.prepareStatement("select txid, indexwriter, status, txts  from search_journal");

			if (log.isDebugEnabled())
			{
				listJournal.clearParameters();
				rs = listJournal.executeQuery();
				while (rs.next())
				{
					log.debug("TX[" + rs.getLong(1) + "];indexwriter[" + rs.getString(2)
							+ "];status[" + rs.getString(3) + "];timestamp["
							+ rs.getLong(4) + "]");
				}
				rs.close();
			}

			if (i < journalSettings.getMinimumOptimizeSavePoints())
			{
				throw new NoOptimizationRequiredException(
						"Insuficient Journal Entries prior to savepoint "
								+ jms.oldestSavePoint + " to optimize, found " + i);
			}

			log.info("Locked " + i + " savePoints ");
			listJournal.clearParameters();
			rs = listJournal.executeQuery();
			while (rs.next())
			{
				log.info("TX[" + rs.getLong(1) + "];indexwriter[" + rs.getString(2)
						+ "];status[" + rs.getString(3) + "];timestamp[" + rs.getLong(4)
						+ "]");
			}
			rs.close();

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
				log.debug(ex2);
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
				log.debug(ex);
			}
			if (ex instanceof LockTimeoutException)
			{
				throw (LockTimeoutException) ex;
			}
			else
			{
				throw new IndexJournalException(
						"Failed to lock savePoints to this node ", ex);
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
				log.debug(ex);
			}
			try
			{
				getJournalSavePointPst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				getEarlierSavePoint.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				getEarlierSavePoint2.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}

			try
			{
				if (lockEarlierSavePoints != null) lockEarlierSavePoints.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				if (listJournal != null) listJournal.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				if (listMergeSet != null) listMergeSet.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
		return jms;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.JournalManager#rollbackSave(org.sakaiproject.search.journal.api.JournalManagerState)
	 */
	public void rollbackSave(JournalManagerState jms)
	{
		OptimizeJournalManagerStateImpl ojms = (OptimizeJournalManagerStateImpl) jms;
		PreparedStatement updateTarget = null;
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			// set the target to committed and then delete the rest
			// so the final segment becomes commtted with a writer id of
			// this+txiD,
			// and all merging-prepare states in this transaction are removed.
			updateTarget = connection
					.prepareStatement("update search_journal set status = 'committed', txts = ? where indexwriter = ? and status = 'merging-prepare'  ");
			updateTarget.clearParameters();
			updateTarget.setLong(1, System.currentTimeMillis());
			updateTarget.setString(2, ojms.indexWriter);
			int i = updateTarget.executeUpdate();

			connection.commit();
			log
					.info("Rolled Back Failed Shared Index operation a retry will happen on annother node soon ");
		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
				log.error("Rollback Of shared Journal Merge Failed ", ex);
			}
		}
		finally
		{
			try
			{
				updateTarget.close();
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

			long nSavePoints = 0;
			Map<String, String> mergingMap = new HashMap<String, String>();
			long transactionId = transaction.getTransactionId();
			String thisWriter = serverId + ":" + transactionId;
			try
			{
				rs = countJournals
						.executeQuery("select txid, indexwriter, status, txts  from search_journal");
				while (rs.next())
				{
					long txid = rs.getLong(1);
					String indexwriter = rs.getString(2);
					String status = rs.getString(3);
					long ts = rs.getLong(4);
					if ("merging-prepare".equals(status))
					{
						mergingMap.put(indexwriter, indexwriter);
					}
					else if ("commited".equals(status) || "committed".equals(status))
					{
						nSavePoints++;
					}
				}

				rs.close();
			}
			catch (Exception ex)
			{
				log
						.info("Optimzation of central journal is in progress on annother node, no optimization possible on this node ");
			}
			if (mergingMap.size() > 1)
			{
				StringBuilder sb = new StringBuilder();
				sb
						.append("\tMore than One shares segments merge appears to be active in the \n");
				sb.append("\tcluster, you Must investigate the search_journal table\n");
				sb.append("\tA list of index Writers Follows\n\t===================\n");
				for (String iw : mergingMap.values())
				{
					sb.append("\t").append(iw);
					if (iw.equals(thisWriter))
					{
						sb
								.append("\tThis node is currently optimizing the shared segments,");
						sb
								.append("\tThis is an error as only one copy of this node should be ");
						sb.append("\tActive in the cluster");
						sb.append("see http://jira.sakaiproject.org/browse/SRCH-38");
					}
					else if (iw.startsWith(serverId))
					{
						sb
								.append("\tThis node is currently optimizing the shared segments,");
						sb
								.append("\tThis is an error as only one copy of this node should be ");
						sb.append("\tActive in the cluster");
						sb.append("see http://jira.sakaiproject.org/browse/SRCH-38");
					}
				}
				sb.append("\t==========================\n");
				log.error(sb.toString());
				throw new NoOptimizationRequiredException(
						"Merge already in progress, possible error");
			}
			else if (mergingMap.size() == 1)
			{
				StringBuilder sb = new StringBuilder();
				for (String iw : mergingMap.values())
				{
					if (iw.equals(thisWriter))
					{
						sb
								.append("This node already merging shared segments, index writer "
										+ iw);
						sb
								.append("\tThis node is currently optimizing the shared segments,");
						sb
								.append("\tThis is an error as only one copy of this node should be ");
						sb.append("\tActive in the cluster");
						sb.append("see http://jira.sakaiproject.org/browse/SRCH-38");
					}
					else if (iw.startsWith(serverId))
					{
						sb
								.append("This node already merging shared segments, index writer "
										+ iw);
						sb
								.append("\tThis node is currently optimizing the shared segments,");
						sb
								.append("\tThis is an error as only one copy of this node should be ");
						sb.append("\tActive in the cluster");
						sb.append("see http://jira.sakaiproject.org/browse/SRCH-38");
					}
				}
				if (sb.length() == 0)
				{
					log
							.info("There is annother node performing shared index merge, this node will continue with other operations ");
					throw new NoOptimizationRequiredException(
							"Merge already in progress, normal");
				}
				else
				{
					log.error(sb.toString());
					throw new NoOptimizationRequiredException(
							"Merge already in progress, possible error");
				}

			}
			else if (nSavePoints < journalSettings.getMinimumOptimizeSavePoints())
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
				log.debug(ex);
			}
			try
			{
				countJournals.close();
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
