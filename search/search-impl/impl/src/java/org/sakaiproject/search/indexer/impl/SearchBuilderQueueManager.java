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

package org.sakaiproject.search.indexer.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.indexer.api.IndexUpdateTransactionListener;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.search.transaction.api.IndexItemsTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.api.TransactionSequence;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;

/**
 * This class manages the Search Build Queue, it retrieves the
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class SearchBuilderQueueManager implements IndexUpdateTransactionListener
{

	private static final Log log = LogFactory.getLog(SearchBuilderQueueManager.class);

	private static final String SEARCH_BUILDER_ITEM_FIELDS = " name, context,  searchaction, searchstate, version, itemscope, id "; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_T = "searchbuilderitem"; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_FIELDS_PARAMS = " ?, ?, ?,  ?, ?, ?, ? "; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_FIELDS_UPDATE = " name = ?, context = ?,  searchaction = ?, searchstate = ?, version = ?, itemscope = ? where id = ? "; //$NON-NLS-1$

	public static final String BATCH_SIZE = "batch-size";

	/**
	 * dependency
	 */
	private SearchIndexBuilder searchIndexBuilder;

	/**
	 * dependency
	 */
	private DataSource datasource;

	private int nodeLock;

	private TransactionSequence sequence;

	/** Configuration: to run the ddl on init or not. */
	protected boolean autoDdl = false;

	public void init()
	{
		try
		{
			if (autoDdl)
			{
				SqlService.getInstance().ddl(this.getClass().getClassLoader(),
						"sakai_search_parallel");
			}
		}
		catch (Exception ex)
		{
			log.error("Perform additional SQL setup", ex);
		}

		nodeLock = (int) sequence.getNextId();
	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexTransaction transaction)
	{
		// At the moment I dont think that we need to do anything here, we could
		// brign the work of the
		// commit phase in here leaving the final commit to the last method,
		// but that would mean taking the connection over more than one cycle.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionListener#commit(org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction)
	 */
	public void commit(IndexTransaction transaction)
	{
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			commitPendingAndUnLock(((IndexItemsTransaction) transaction).getItems(),
					connection);
			connection.commit();
		}
		catch (Exception ex)
		{
			try
			{
				if (connection != null) connection.rollback();
			}
			catch (Exception ex2)
			{
				log.warn("error during rollback in commit", ex2);
			}
		}
		finally
		{
			try
			{
				if (connection != null) connection.close();
			}
			catch (Exception ex2)
			{
				log.warn("error closing connection in commit", ex2);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionListener#rollback(org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction)
	{
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			rollbackPendingAndUnLock(((IndexItemsTransaction) transaction).getItems(),
					connection);
			connection.commit();
		}
		catch (Exception ex)
		{
			try
			{
				if (connection != null) connection.rollback();
			}
			catch (Exception ex2)
			{
				log.debug("Exception during rollback", ex2);
			}
		}
		finally
		{
			try
			{
				if (connection != null) connection.close();
			}
			catch (Exception ex2)
			{
				log.debug("Exception closing connection", ex2);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionListener#open(org.sakaiproject.search.component.service.index.transactional.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexTransactionException
	{
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			Integer bs = (Integer) transaction.get(BATCH_SIZE);
			int batchSize = 100;
			if (bs != null)
			{
				batchSize = bs.intValue();
			}
			List<SearchBuilderItem> items = findPendingAndLock(batchSize, connection);
			log.debug("Adding " + items.size()
					+ " items to indexing queue: batch size was " + batchSize);
			((IndexItemsTransaction) transaction).setItems(items);
			connection.commit();
		}
		catch (IndexTransactionException itex)
		{
			log.info("Rethrowing " + itex.getMessage());
			throw itex;
		}
		catch (Exception ex)
		{
			log.info("Failed to Open Transaction ", ex);
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
				log.debug("Exception during rollback", ex2);
			}
			throw new IndexTransactionException("Failed to open transaction ", ex);
		}
		finally
		{
			try
			{
				if (connection != null) connection.close();
			}
			catch (Exception ex2)
			{
				log.debug("Exception closing connection", ex2);
			}
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
	}

	private List<SearchBuilderItem> findPendingAndLock(int batchSize,
			Connection connection) throws SQLException
	{
		// Pending is the first 100 items
		// State == PENDING
		// Action != Unknown
		long start = System.currentTimeMillis();
		try
		{
			log.debug("TXFind pending with " + connection); //$NON-NLS-1$

			SearchBuilderItem masterItem = getMasterItem(connection);
			try
			{

				Integer masterAction = getMasterAction(masterItem);
				log.debug(" Master Item is " + masterItem.getName() + ":" //$NON-NLS-1$ //$NON-NLS-2$
						+ masterItem.getSearchaction() + ":" //$NON-NLS-1$
						+ masterItem.getSearchstate() + "::" //$NON-NLS-1$
						+ masterItem.getVersion());
				if (SearchBuilderItem.ACTION_REFRESH.equals(masterAction))
				{
					log.debug(" Master Action is " + masterAction); //$NON-NLS-1$
					log.debug("  REFRESH = " + SearchBuilderItem.ACTION_REFRESH); //$NON-NLS-1$
					log.debug("  RELOAD = " + SearchBuilderItem.ACTION_REBUILD); //$NON-NLS-1$
					// get a complete list of all items, before the master
					// action version
					// if there are none, update the master action action to
					// completed
					// and return a blank list

					refreshIndex(connection, masterItem);

				}
				else if (SearchBuilderItem.ACTION_REBUILD.equals(masterAction))
				{
					rebuildIndex(connection, masterItem);
				}
				else
				{
					// get all site masters and perform the required action.
					List siteMasters = getSiteMasterItems(connection);
					for (Iterator i = siteMasters.iterator(); i.hasNext();)
					{
						SearchBuilderItem siteMaster = (SearchBuilderItem) i.next();
						try
						{
							Integer action = getSiteMasterAction(siteMaster);
							if (SearchBuilderItem.ACTION_REBUILD.equals(action))
							{
								rebuildIndex(connection, siteMaster);
							}
							else if (SearchBuilderItem.ACTION_REFRESH.equals(action))
							{
								refreshIndex(connection, siteMaster);
							}
						}
						finally
						{
							// any value > 1000 is a lock
							if (siteMaster.getLock() == nodeLock )
							{
								List<SearchBuilderItem> l = new ArrayList<SearchBuilderItem>();
								l.add(siteMaster);
								commitPendingAndUnLock(l, connection);
							}
						}
					}
				}
			}
			finally
			{
				if (masterItem.getLock() == nodeLock  )
				{
					List<SearchBuilderItem> l = new ArrayList<SearchBuilderItem>();
					l.add(masterItem);
					commitPendingAndUnLock(l, connection);
				}
			}
			PreparedStatement pst = null;
			PreparedStatement lockedPst = null;
			ResultSet rst = null;
			try
			{
				pst = connection.prepareStatement("select " //$NON-NLS-1$
						+ SEARCH_BUILDER_ITEM_FIELDS + " from " //$NON-NLS-1$
						+ SEARCH_BUILDER_ITEM_T + " where searchstate = ? and     " //$NON-NLS-1$
						+ "        itemscope = ?  order by version "); //$NON-NLS-1$
				lockedPst = connection.prepareStatement("update " //$NON-NLS-1$
						+ SEARCH_BUILDER_ITEM_T + " set searchstate = ? " //$NON-NLS-1$
						+ " where id = ?  and  searchstate = ? "); //$NON-NLS-1$
				pst.clearParameters();
				pst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
				pst.setInt(2, SearchBuilderItem.ITEM.intValue());
				rst = pst.executeQuery();
				List<SearchBuilderItem> a = new ArrayList<SearchBuilderItem>();
				while (rst.next() && a.size() < batchSize)
				{

					SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
					populateSearchBuilderItem(rst, sbi);
					if (!SearchBuilderItem.ACTION_UNKNOWN.equals(sbi.getSearchaction()))
					{
						lockedPst.clearParameters();
						lockedPst.setInt(1, nodeLock);
						lockedPst.setString(2, sbi.getId());
						lockedPst.setInt(3, SearchBuilderItem.STATE_PENDING.intValue());
						if (lockedPst.executeUpdate() == 1)
						{
							sbi.setSearchstate(SearchBuilderItem.STATE_LOCKED);
							sbi.setLock(nodeLock);
							a.add(sbi);
						}
						connection.commit();
					}

				}
				return a;
			}
			finally
			{
				try
				{
					if (rst != null) rst.close();
				}
				catch (Exception ex)
				{
					log.warn("Error closing result set", ex);
				}
				try
				{
					if (pst != null) pst.close();
				}
				catch (Exception ex)
				{
					log.warn("Error closing statement", ex);
				}
			}

		}
		finally
		{
			long finish = System.currentTimeMillis();
			log.debug(" findPending took " + (finish - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param runtimeToDo
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private void commitPendingAndUnLock(List<SearchBuilderItem> runtimeToDo,
			Connection connection) throws SQLException
	{
		PreparedStatement unLockPst = null;
		PreparedStatement deletePst = null;
		try
		{
			unLockPst = connection.prepareStatement("update " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " set searchstate = ? " //$NON-NLS-1$
					+ " where id = ?  and  searchstate = ? "); //$NON-NLS-1$

			deletePst = connection.prepareStatement(" delete from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " where id = ? "); //$NON-NLS-1$

			for (Iterator<SearchBuilderItem> isbi = runtimeToDo.iterator(); isbi
					.hasNext();)
			{

				SearchBuilderItem sbi = isbi.next();

				if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
				{
					deletePst.clearParameters();
					deletePst.setString(1, sbi.getId());
					if (deletePst.executeUpdate() != 1)
					{
						log.warn("Failed to delete " + sbi.getName() + "  ");
					}
					else
					{
						log.debug("Delete " + sbi.getName() + "  ");

					}
					connection.commit();
				}
				else
				{
					unLockPst.clearParameters();
					unLockPst.setInt(1, SearchBuilderItem.STATE_COMPLETED.intValue());
					unLockPst.setString(2, sbi.getId());
					unLockPst.setInt(3, sbi.getLock());
					if (unLockPst.executeUpdate() != 1)
					{
						log.warn("Failed to mark " + sbi + " as completed ");
					}
					else
					{
						log.debug("Marked " + sbi.getName() + " as completed ");
					}
					sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
					connection.commit();
				}
			}
		}
		finally
		{
			try
			{
				unLockPst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error unlocking pst", ex);
			}
			try
			{
				deletePst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error deleting pst", ex);
			}
		}
	}

	/**
	 * @param runtimeToDo
	 * @param connection
	 * @throws SQLException
	 */
	private void rollbackPendingAndUnLock(List<SearchBuilderItem> runtimeToDo,
			Connection connection) throws SQLException
	{
		PreparedStatement unLockPst = null;
		try
		{
			unLockPst = connection.prepareStatement("update " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " set searchstate = ? " //$NON-NLS-1$
					+ " where id = ?  and  searchstate = ? "); //$NON-NLS-1$
			for (Iterator<SearchBuilderItem> isbi = runtimeToDo.iterator(); isbi
					.hasNext();)
			{

				SearchBuilderItem sbi = isbi.next();
				unLockPst.clearParameters();
				if (SearchBuilderItem.STATE_FAILED.equals(sbi.getSearchstate()))
				{
					sbi.setSearchstate(SearchBuilderItem.STATE_FAILED);
					unLockPst.setInt(1, SearchBuilderItem.STATE_FAILED.intValue());
				}
				else
				{
					sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
					unLockPst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
				}
				unLockPst.setString(2, sbi.getId());
				unLockPst.setInt(3, sbi.getSearchstate());
				if (unLockPst.executeUpdate() == 1)
				{
					log.warn("Failed to mark " + sbi.getName() + " as pending ");
				}
				connection.commit();
			}
		}
		finally
		{
			try
			{
				unLockPst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error unlocking pst", ex);
			}
		}
	}

	/**
	 * get the Instance Master
	 * 
	 * @return
	 * @throws SQLException
	 */
	private SearchBuilderItem getMasterItem(Connection connection) throws SQLException
	{
		log.debug("get Master Items with " + connection); //$NON-NLS-1$

		PreparedStatement pst = null;
		PreparedStatement lockMaster = null;
		ResultSet rst = null;
		try
		{

			lockMaster = connection.prepareStatement("update " + SEARCH_BUILDER_ITEM_T
					+ " set searchstate = ? where itemscope = ? and searchstate = ? ");
			lockMaster.clearParameters();
			lockMaster.setInt(1, nodeLock);
			lockMaster.setInt(2, SearchBuilderItem.ITEM_GLOBAL_MASTER.intValue());
			lockMaster.setInt(3, SearchBuilderItem.STATE_PENDING.intValue());
			lockMaster.executeUpdate();

			pst = connection
					.prepareStatement("select " //$NON-NLS-1$
							+ SEARCH_BUILDER_ITEM_FIELDS
							+ " from " //$NON-NLS-1$
							+ SEARCH_BUILDER_ITEM_T
							+ " where itemscope = ? and searchstate = ? "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.ITEM_GLOBAL_MASTER.intValue());
			pst.setInt(2, nodeLock);
			rst = pst.executeQuery();
			SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
			if (rst.next())
			{
				populateSearchBuilderItem(rst, sbi);
				sbi.setLock(nodeLock);
				log.info("Locked Master item to this node "+sbi);
				rst.close();
				connection.commit();
			}
			else
			{
				rst.close();
				connection.rollback();
				sbi.setName(SearchBuilderItem.INDEX_MASTER);
				sbi.setContext(SearchBuilderItem.GLOBAL_CONTEXT);
				sbi.setSearchaction(SearchBuilderItem.ACTION_UNKNOWN);
				sbi.setSearchstate(SearchBuilderItem.STATE_UNKNOWN);
				sbi.setItemscope(SearchBuilderItem.ITEM_GLOBAL_MASTER);
			}
			return sbi;
		}
		finally
		{
			try
			{
				if (rst != null) rst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing rst", ex);
			}
			try
			{
				if (pst != null) pst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing pst", ex);
			}
			try
			{
				lockMaster.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing lockMaster", ex);
			}
		}
	}

	private List<SearchBuilderItem> getSiteMasterItems(Connection connection)
			throws SQLException
	{
		PreparedStatement pst = null;
		PreparedStatement lockMaster = null;

		ResultSet rst = null;
		try
		{

			lockMaster = connection.prepareStatement("update " + SEARCH_BUILDER_ITEM_T
					+ " set searchstate = ? where itemscope = ? and searchstate = ? ");
			lockMaster.clearParameters();
			lockMaster.setInt(1, nodeLock);
			lockMaster.setInt(2, SearchBuilderItem.ITEM_SITE_MASTER.intValue());
			lockMaster.setInt(3, SearchBuilderItem.STATE_PENDING.intValue());
			lockMaster.executeUpdate();

			pst = connection.prepareStatement("select " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_FIELDS
					+ " from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T
					+ " where itemscope =   ? and searchstate = ?  "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.ITEM_SITE_MASTER.intValue());
			pst.setInt(2, nodeLock);
			rst = pst.executeQuery();
			List<SearchBuilderItem> a = new ArrayList<SearchBuilderItem>();
			while (rst.next())
			{
				SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
				populateSearchBuilderItem(rst, sbi);
				a.add(sbi);
			}
			if (a.size() > 0)
			{
				connection.commit();
			}
			else
			{
				connection.rollback();
			}
			return a;
		}
		finally
		{
			try
			{
				rst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing rst", ex);
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing pst", ex);
			}
			try
			{
				lockMaster.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing lockMaster", ex);
			}
		}
	}

	/**
	 * get the master action of known master item
	 * 
	 * @param master
	 * @return
	 */
	private Integer getMasterAction(SearchBuilderItem master)
	{
		if (master.getName().equals(SearchBuilderItem.GLOBAL_MASTER))
		{
			if (master.getLock() == nodeLock)
			{
				return master.getSearchaction();
			}
		}
		return SearchBuilderItem.STATE_UNKNOWN;
	}

	/**
	 * get the action for the site master
	 * 
	 * @param siteMaster
	 * @return
	 */
	private Integer getSiteMasterAction(SearchBuilderItem siteMaster)
	{
		if (siteMaster.getName().startsWith(SearchBuilderItem.INDEX_MASTER)
				&& !SearchBuilderItem.GLOBAL_CONTEXT.equals(siteMaster.getContext()))
		{
			if (siteMaster.getLock() == nodeLock )
			{
				return siteMaster.getSearchaction();
			}
		}
		
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	private void populateSearchBuilderItem(ResultSet rst, SearchBuilderItemImpl sbi)
			throws SQLException
	{
		sbi.setName(rst.getString(1));
		sbi.setContext(rst.getString(2));
		sbi.setSearchaction(Integer.valueOf(rst.getInt(3)));
		sbi.setSearchstate(Integer.valueOf(rst.getInt(4)));
		sbi.setVersion(rst.getDate(5));
		sbi.setItemscope(Integer.valueOf(rst.getInt(6)));
		sbi.setId(rst.getString(7));
	}

	private void rebuildIndex(Connection connection, SearchBuilderItem controlItem)
			throws SQLException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("DELETE ALL RECORDS =========================================================="); //$NON-NLS-1$
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem.getContext()))
			{
				stm.execute("delete from searchbuilderitem where itemscope = "
						+ SearchBuilderItem.ITEM
						+ " or itemscope = " + SearchBuilderItem.ITEM_SITE_MASTER); //$NON-NLS-1$
			}
			else
			{
				stm.execute("delete from searchbuilderitem where context = '" //$NON-NLS-1$
						+ controlItem.getContext() + "' and name <> '" //$NON-NLS-1$
						+ controlItem.getName() + "' "); //$NON-NLS-1$

			}

			log
					.debug("DONE DELETE ALL RECORDS ==========================================================="); //$NON-NLS-1$
			connection.commit();
			log
					.debug("ADD ALL RECORDS ==========================================================="); //$NON-NLS-1$
			long lastupdate = System.currentTimeMillis();
			List<String> contextList = new ArrayList<String>();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem.getContext()))
			{

				for (Iterator i = SiteService.getSites(SelectionType.ANY, null, null,
						null, SortType.NONE, null).iterator(); i.hasNext();)
				{
					Site s = (Site) i.next();
					if (!SiteService.isSpecialSite(s.getId())
							|| SiteService.isUserSite(s.getId()))
					{
						if (searchIndexBuilder.isOnlyIndexSearchToolSites())
						{
							ToolConfiguration t = s.getToolForCommonId("sakai.search"); //$NON-NLS-1$
							if (t != null)
							{
								contextList.add(s.getId());
							}
						}
						else
						{
							contextList.add(s.getId());
						}
					}
				}
			}
			else
			{
				contextList.add(controlItem.getContext());
			}
			for (Iterator c = contextList.iterator(); c.hasNext();)
			{
				String siteContext = (String) c.next();
				log.info("Rebuild for " + siteContext); //$NON-NLS-1$
				for (Iterator i = searchIndexBuilder.getContentProducers().iterator(); i
						.hasNext();)
				{
					EntityContentProducer ecp = (EntityContentProducer) i.next();

					Iterator contentIterator = null;
					try
					{
						contentIterator = ecp.getSiteContentIterator(siteContext);
						log.debug("Using ECP " + ecp); //$NON-NLS-1$

						int added = 0;
						for (; contentIterator.hasNext();)
						{
							String resourceName = (String) contentIterator.next();
							log.debug("Checking " + resourceName); //$NON-NLS-1$
							if (resourceName == null || resourceName.length() > 255)
							{
								log
										.warn("Entity Reference Longer than 255 characters, ignored: Reference=" //$NON-NLS-1$
												+ resourceName);
								continue;
							}
							SearchBuilderItem sbi = new SearchBuilderItemImpl();
							sbi.setName(resourceName);
							sbi.setSearchaction(SearchBuilderItem.ACTION_ADD);
							sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
							sbi.setId(UUID.randomUUID().toString());
							sbi.setVersion(new Date(System.currentTimeMillis()));
							sbi.setItemscope(SearchBuilderItem.ITEM);
							String context = null;
							try
							{
								context = ecp.getSiteId(resourceName);
							}
							catch (Exception ex)
							{
								log.debug("No context for resource " + resourceName //$NON-NLS-1$
										+ " defaulting to none"); //$NON-NLS-1$
							}
							if (context == null || context.length() == 0)
							{
								context = "none"; //$NON-NLS-1$
							}
							sbi.setContext(context);
							try
							{
								updateOrSave(connection, sbi);
							}
							catch (SQLException sqlex)
							{
								log.error("Failed to update " + sqlex.getMessage()); //$NON-NLS-1$
							}
							connection.commit();

						}
						log.debug(" Added " + added); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						log.warn("Failed to index site " + siteContext
								+ " site has not been indexed");
					}
				}
			}
			log
					.debug("DONE ADD ALL RECORDS ==========================================================="); //$NON-NLS-1$
			connection.commit();
		}
		finally
		{
			try
			{
				stm.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing stm", ex);
			}
		}

	}

	private void refreshIndex(Connection connection, SearchBuilderItem controlItem)
			throws SQLException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("UPDATE ALL RECORDS =========================================================="); //$NON-NLS-1$
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem.getContext()))
			{
				stm.execute("update searchbuilderitem set searchstate = " //$NON-NLS-1$
						+ SearchBuilderItem.STATE_PENDING
						+ " where itemscope = " + SearchBuilderItem.ITEM); //$NON-NLS-1$

			}
			else
			{
				stm
						.execute("update searchbuilderitem set searchstate = " //$NON-NLS-1$
								+ SearchBuilderItem.STATE_PENDING
								+ " where itemscope = " + SearchBuilderItem.ITEM_SITE_MASTER + " and context = '" + controlItem.getContext() //$NON-NLS-1$
								+ "' and name <> '" + controlItem.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$

			}
			connection.commit();
		}
		finally
		{
			try
			{
				stm.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing stm", ex);
			};
		}
	}

	private void updateOrSave(Connection connection, SearchBuilderItem sbi)
			throws SQLException
	{
		PreparedStatement pst = null;
		try
		{
			try
			{
				save(connection, sbi);
			}
			catch (SQLException sqlex)
			{
				pst = connection.prepareStatement("update " //$NON-NLS-1$
						+ SEARCH_BUILDER_ITEM_T + " set " //$NON-NLS-1$
						+ SEARCH_BUILDER_ITEM_FIELDS_UPDATE);
				populateStatement(pst, sbi);
				pst.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			log.warn("Failed ", ex); //$NON-NLS-1$
			throw ex;
		}
		finally
		{
			try
			{
				if (pst != null) pst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing pst", ex);
			}
		}
	}

	private void save(Connection connection, SearchBuilderItem sbi) throws SQLException
	{
		PreparedStatement pst = null;
		try
		{
			pst = connection.prepareStatement(" insert into " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " ( " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_FIELDS + " ) values ( " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_FIELDS_PARAMS + " ) "); //$NON-NLS-1$
			pst.clearParameters();
			populateStatement(pst, sbi);
			pst.executeUpdate();
		}
		finally
		{
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				log.warn("Error closing pst", ex);
			}
		}

	}

	private int populateStatement(PreparedStatement pst, SearchBuilderItem sbi)
			throws SQLException
	{
		pst.setString(1, sbi.getName());
		pst.setString(2, sbi.getContext());
		pst.setInt(3, sbi.getSearchaction().intValue());
		pst.setInt(4, sbi.getSearchstate().intValue());
		pst.setDate(5, new Date(sbi.getVersion().getTime()));
		pst.setInt(6, sbi.getItemscope().intValue());
		pst.setString(7, sbi.getId());
		return 7;

	}

	/**
	 * @return the searchIndexBuilder
	 */
	public SearchIndexBuilder getSearchIndexBuilder()
	{
		return searchIndexBuilder;
	}

	/**
	 * @param searchIndexBuilder
	 *        the searchIndexBuilder to set
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
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
	 * @return the sequence
	 */
	public TransactionSequence getSequence()
	{
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(TransactionSequence sequence)
	{
		this.sequence = sequence;
	}
	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		autoDdl = Boolean.valueOf(value).booleanValue();
	}
	/**
	 */
	public String getAutoDdl()
	{
		return String.valueOf(autoDdl);
	}

}
