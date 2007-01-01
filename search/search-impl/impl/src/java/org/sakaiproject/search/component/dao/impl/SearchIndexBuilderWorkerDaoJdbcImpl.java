/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.component.dao.impl;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.hibernate.HibernateException;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchIndexBuilderWorker;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.rdf.RDFIndexException;
import org.sakaiproject.search.api.rdf.RDFSearchService;
import org.sakaiproject.search.dao.SearchIndexBuilderWorkerDao;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;

public class SearchIndexBuilderWorkerDaoJdbcImpl implements
		SearchIndexBuilderWorkerDao
{

	private static final String SEARCH_BUILDER_ITEM_FIELDS = " name, context,  searchaction, searchstate, version, id ";

	private static final String SEARCH_BUILDER_ITEM_T = "searchbuilderitem";

	private static final String SEARCH_BUILDER_ITEM_FIELDS_PARAMS = " ?, ?, ?,  ?, ?, ? ";

	private static final String SEARCH_BUILDER_ITEM_FIELDS_UPDATE = " name = ?, context = ?,  searchaction = ?, searchstate = ?, version = ? where id = ? ";

	private static Log log = LogFactory
			.getLog(SearchIndexBuilderWorkerDaoJdbcImpl.class);

	/**
	 * sync object
	 */
	// private Object threadStartLock = new Object();
	/**
	 * dependency: the search index builder that is accepting new items
	 */
	private SearchIndexBuilder searchIndexBuilder = null;

	/**
	 * The number of items to process in a batch, default = 100
	 */
	private int indexBatchSize = 100;

	private boolean enabled = false;

	private EntityManager entityManager;

	private RDFSearchService rdfSearchService = null;

	private IdentifierGenerator idgenerator = new VersionFourGenerator();

	/**
	 * injected to abstract the storage impl
	 */
	private IndexStorage indexStorage = null;

	private DataSource dataSource = null;


	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		entityManager = (EntityManager) load(cm, EntityManager.class.getName(),
				true);
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName(), true);
		rdfSearchService = (RDFSearchService) load(cm, RDFSearchService.class
				.getName(), false);

		enabled = "true".equals(ServerConfigurationService.getString(
				"search.experimental", "false"));
		try
		{
			if (searchIndexBuilder == null)
			{
				log.error("Search Index Worker needs searchIndexBuilder ");
			}
			if (entityManager == null)
			{
				log.error("Search Index Worker needs EntityManager ");
			}
			if (indexStorage == null)
			{
				log.error("Search Index Worker needs indexStorage ");
			}
			if (rdfSearchService == null)
			{
				log
						.info("No RDFSearchService has been defined, RDF Indexing not enabled");
			}
			else
			{
				log
						.warn("Experimental RDF Search Service is enabled using implementation "
								+ rdfSearchService);
			}

		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}
	}

	private Object load(ComponentManager cm, String name, boolean aserror)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			if (aserror)
			{
				log.error("Cant find Spring component named " + name);
			}
		}
		return o;
	}

	private void processDeletes(SearchIndexBuilderWorker worker,
			Connection connection, List runtimeToDo) throws SQLException,
			IOException
	{

		if (indexStorage.indexExists())
		{
			IndexReader indexReader = null;
			try
			{
				indexReader = indexStorage.getIndexReader();

				// Open the index
				for (Iterator tditer = runtimeToDo.iterator(); worker
						.isRunning()
						&& tditer.hasNext();)
				{
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					if (!SearchBuilderItem.STATE_LOCKED.equals(sbi
							.getSearchstate()))
					{
						// should only be getting pending
						// items
						log.warn(" Found Item that was not pending "
								+ sbi.getName());
						continue;
					}
					if (SearchBuilderItem.ACTION_UNKNOWN.equals(sbi
							.getSearchaction()))
					{
						sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
						updateOrSave(connection, sbi);
						connection.commit();

						continue;
					}
					// remove document
					// if this is mult segment it might not work.
					try
					{
						indexReader.deleteDocuments(new Term(
								SearchService.FIELD_REFERENCE, sbi.getName()));
						if (SearchBuilderItem.ACTION_DELETE.equals(sbi
								.getSearchaction()))
						{
							sbi
									.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
							updateOrSave(connection, sbi);
							connection.commit();
						}
						else
						{
							sbi
									.setSearchstate(SearchBuilderItem.STATE_PENDING_2);
						}

					}
					catch (IOException ex)
					{
						log.warn("Failed to delete Page ", ex);
					}
				}
			}
			finally
			{
				if (indexReader != null)
				{
					indexStorage.closeIndexReader(indexReader);
					indexReader = null;
				}
			}
		}

	}

	private void processAdd(SearchIndexBuilderWorker worker,
			Connection connection, List runtimeToDo) throws Exception
	{
		IndexWriter indexWrite = null;
		try
		{
			if (worker.isRunning())
			{
				indexWrite = indexStorage.getIndexWriter(false);
			}
			for (Iterator tditer = runtimeToDo.iterator(); worker.isRunning()
					&& tditer.hasNext();)
			{
				Reader contentReader = null;
				try
				{
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					// only add adds, that have been deleted
					// sucessfully
					if (!SearchBuilderItem.STATE_PENDING_2.equals(sbi
							.getSearchstate()))
					{
						continue;
					}
					Reference ref = entityManager.newReference(sbi.getName());

					if (ref == null)
					{
						log
								.error("Unrecognised trigger object presented to index builder "
										+ sbi);
					}

					long startDocIndex = System.currentTimeMillis();
					worker.setStartDocIndex(startDocIndex);
					worker.setNowIndexing(ref.getReference());

					try
					{
						try
						{
							Entity entity = ref.getEntity();
							EntityContentProducer sep = searchIndexBuilder
									.newEntityContentProducer(ref);
							boolean indexDoc = true;
							if (searchIndexBuilder.isOnlyIndexSearchToolSites())
							{
								try
								{
									String siteId = sep
											.getSiteId(sbi.getName());
									Site s = SiteService.getSite(siteId);
									ToolConfiguration t = s
											.getToolForCommonId("sakai.search");
									if (t == null)
									{
										indexDoc = false;
										log.debug("Not indexing "
												+ sbi.getName()
												+ " as it has no search tool");
									}
								}
								catch (Exception ex)
								{
									indexDoc = false;
									log.debug("Not indexing  " + sbi.getName()
											+ " as it has no site", ex);

								}
							}
							if (indexDoc && sep != null && sep.isForIndex(ref)
									&& ref.getContext() != null)
							{

								Document doc = new Document();
								String container = ref.getContainer();
								if (container == null) container = "";
								doc.add(new Field(SearchService.DATE_STAMP,
										String.valueOf(System
												.currentTimeMillis()),
										Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(
										SearchService.FIELD_CONTAINER,
										container, Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_ID, ref
										.getId(), Field.Store.COMPRESS,
										Field.Index.NO));
								doc.add(new Field(SearchService.FIELD_TYPE, ref
										.getType(), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_SUBTYPE,
										ref.getSubType(), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(
										SearchService.FIELD_REFERENCE, ref
												.getReference(),
										Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));

								doc.add(new Field(SearchService.FIELD_CONTEXT,
										sep.getSiteId(ref),
										Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								if (sep.isContentFromReader(entity))
								{
									contentReader = sep
											.getContentReader(entity);
									doc
											.add(new Field(
													SearchService.FIELD_CONTENTS,
													contentReader,
													Field.TermVector.YES));
								}
								else
								{
									doc.add(new Field(
											SearchService.FIELD_CONTENTS, sep
													.getContent(entity),
											Field.Store.NO,
											Field.Index.TOKENIZED,
											Field.TermVector.YES));
								}
								doc.add(new Field(SearchService.FIELD_TITLE,
										sep.getTitle(entity),
										Field.Store.COMPRESS,
										Field.Index.TOKENIZED,
										Field.TermVector.YES));
								doc.add(new Field(SearchService.FIELD_TOOL, sep
										.getTool(), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_URL, sep
										.getUrl(entity), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_SITEID,
										sep.getSiteId(ref),
										Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));

								// add the custom properties

								Map m = sep.getCustomProperties();
								if (m != null)
								{
									for (Iterator cprops = m.keySet()
											.iterator(); cprops.hasNext();)
									{
										String key = (String) cprops.next();
										Object value = m.get(key);
										String[] values = null;
										if (value instanceof String)
										{
											values = new String[1];
											values[0] = (String) value;
										}
										if (value instanceof String[])
										{
											values = (String[]) value;
										}
										if (values == null)
										{
											log
													.info("Null Custom Properties value has been suppled by "
															+ sep
															+ " in index "
															+ key);
										}
										else
										{
											for (int i = 0; i < values.length; i++)
											{
												doc
														.add(new Field(
																key,
																values[i],
																Field.Store.COMPRESS,
																Field.Index.UN_TOKENIZED));
											}
										}
									}
								}

								log.debug("Indexing Document " + doc);

								indexWrite.addDocument(doc);

								log.debug("Done Indexing Document " + doc);

								processRDF(sep);

							}
							else
							{
								log.debug("Ignored Document " + ref.getId());
							}
						}
						catch (Exception e1)
						{
							log.info(" Failed to index document cause: "
									+ e1.getMessage());
						}
						sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
						updateOrSave(connection, sbi);
						connection.commit();
					}
					catch (Exception e1)
					{
						log.debug(" Failed to index document cause: "
								+ e1.getMessage());
					}
					long endDocIndex = System.currentTimeMillis();
					worker.setLastIndex(endDocIndex - startDocIndex);
					if ((endDocIndex - startDocIndex) > 60000L)
					{
						log
								.warn("Slow index operation "
										+ String
												.valueOf((endDocIndex - startDocIndex) / 1000)
										+ " seconds to index "
										+ ref.getReference());
					}
					// update this node lock to indicate its
					// still alove, no document should
					// take more than 2 mins to process
					if (!worker.getLockTransaction(15L * 60L * 1000L, true))
					{
						throw new Exception(
								"Transaction Lock Expired while indexing "
										+ ref.getReference());
					}

				}
				finally
				{
					if (contentReader != null)
					{
						try
						{
							contentReader.close();
						}
						catch (IOException ioex)
						{
						}
					}
				}

			}
			worker.setStartDocIndex(System.currentTimeMillis());
			worker.setNowIndexing("-idle-");
		}
		finally
		{
			if (indexWrite != null)
			{
				indexStorage.closeIndexWriter(indexWrite);
			}
		}

	}

	private int completeUpdate(SearchIndexBuilderWorker worker,
			Connection connection, List runtimeToDo) throws Exception
	{
		try
		{

			for (Iterator tditer = runtimeToDo.iterator(); worker.isRunning()
					&& tditer.hasNext();)
			{
				SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
				if (SearchBuilderItem.STATE_COMPLETED.equals(sbi
						.getSearchstate()))
				{
					if (SearchBuilderItem.ACTION_DELETE.equals(sbi
							.getSearchaction()))
					{
						delete(connection, sbi);
						connection.commit();
					}
					else
					{
						updateOrSave(connection, sbi);
						connection.commit();
					}

				}
			}
			return runtimeToDo.size();
		}
		catch (Exception ex)
		{
			log
					.warn("Failed to update state in database due to "
							+ ex.getMessage()
							+ " this will be corrected on the next run of the IndexBuilder, no cause for alarm");
		}
		return 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.dao.impl.SearchIndexBuilderWorkerDao#processToDoListTransaction()
	 */
	public void processToDoListTransaction(SearchIndexBuilderWorker worker)
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			long startTime = System.currentTimeMillis();
			int totalDocs = 0;

			// Load the list

			List runtimeToDo = findPending(indexBatchSize, connection, worker);

			totalDocs = runtimeToDo.size();

			log.debug("Processing " + totalDocs + " documents");

			if (totalDocs > 0)
			{
				indexStorage.doPreIndexUpdate();

				// get lock

				// this needs to be exclusive
				processDeletes(worker, connection, runtimeToDo);

				// upate and release lock
				// after a process Deletes the index needs to updated

				// can be parallel
				processAdd(worker, connection, runtimeToDo);

				completeUpdate(worker, connection, runtimeToDo);

				// get lock
				try
				{
					indexStorage.doPostIndexUpdate();
				}
				catch (IOException e)
				{
					log.error("Failed to do Post Index Update", e);
				}
				// release lock

			}

			if (worker.isRunning())
			{
				long endTime = System.currentTimeMillis();
				float totalTime = endTime - startTime;
				float ndocs = totalDocs;
				if (totalDocs > 0)
				{
					float docspersec = 1000 * ndocs / totalTime;
					log.info("Completed Process List of " + totalDocs + " at "
							+ docspersec + " documents/per second");
				}
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to perform index cycle " + ex.getMessage());
			log.debug("Traceback is ", ex);
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

	private void processRDF(EntityContentProducer sep) throws RDFIndexException
	{
		if (rdfSearchService != null)
		{
			String s = sep.getCustomRDF();
			if (s != null)
			{
				rdfSearchService.addData(s);
			}
		}
	}

	private List getSiteMasterItems(Connection connection) throws SQLException
	{
		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{
			pst = connection.prepareStatement("select "
					+ SEARCH_BUILDER_ITEM_FIELDS + " from "
					+ SEARCH_BUILDER_ITEM_T + " where name like  ?  "
					+ "   and   context <> ?  ");
			pst.clearParameters();
			pst.setString(1, SearchBuilderItem.SITE_MASTER_PATTERN);
			pst.setString(2, SearchBuilderItem.GLOBAL_CONTEXT);
			rst = pst.executeQuery();
			ArrayList a = new ArrayList();
			while (rst.next())
			{
				SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
				populateSearchBuilderItem(rst, sbi);
				a.add(sbi);
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
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * get the Instance Master
	 * 
	 * @return
	 * @throws HibernateException
	 */
	private SearchBuilderItem getMasterItem(Connection connection)
			throws SQLException
	{
		log.debug("get Master Items with " + connection);

		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{
			pst = connection.prepareStatement("select "
					+ SEARCH_BUILDER_ITEM_FIELDS + " from "
					+ SEARCH_BUILDER_ITEM_T + " where name = ? ");
			pst.clearParameters();
			pst.setString(1, SearchBuilderItem.GLOBAL_MASTER);
			rst = pst.executeQuery();
			SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
			if (rst.next())
			{
				populateSearchBuilderItem(rst, sbi);
			}
			else
			{
				sbi.setName(SearchBuilderItem.INDEX_MASTER);
				sbi.setContext(SearchBuilderItem.GLOBAL_CONTEXT);
				sbi.setSearchaction(SearchBuilderItem.ACTION_UNKNOWN);
				sbi.setSearchstate(SearchBuilderItem.STATE_UNKNOWN);
			}
			return sbi;
		}
		finally
		{
			try
			{
				rst.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	private void populateSearchBuilderItem(ResultSet rst,
			SearchBuilderItemImpl sbi) throws SQLException
	{
		sbi.setName(rst.getString(1));
		sbi.setContext(rst.getString(2));
		sbi.setSearchaction(new Integer(rst.getInt(3)));
		sbi.setSearchstate(new Integer(rst.getInt(4)));
		sbi.setVersion(rst.getDate(5));
		sbi.setId(rst.getString(6));
	}

	private int populateStatement(PreparedStatement pst, SearchBuilderItem sbi)
			throws SQLException
	{
		pst.setString(1, sbi.getName());
		pst.setString(2, sbi.getContext());
		pst.setInt(3, sbi.getSearchaction().intValue());
		pst.setInt(4, sbi.getSearchstate().intValue());
		pst.setDate(5, new Date(sbi.getVersion().getTime()));
		pst.setString(6, sbi.getId());
		return 6;

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

				pst = connection.prepareStatement("update "
						+ SEARCH_BUILDER_ITEM_T + " set "
						+ SEARCH_BUILDER_ITEM_FIELDS_UPDATE);
				populateStatement(pst, sbi);
				pst.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			log.warn("Failed ", ex);
			throw ex;
		}
		finally
		{
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	private void save(Connection connection, SearchBuilderItem sbi)
			throws SQLException
	{
		PreparedStatement pst = null;
		try
		{
			pst = connection.prepareStatement(" insert into "
					+ SEARCH_BUILDER_ITEM_T + " ( "
					+ SEARCH_BUILDER_ITEM_FIELDS + " ) values ( "
					+ SEARCH_BUILDER_ITEM_FIELDS_PARAMS + " ) ");
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
			}
		}

	}

	private void delete(Connection connection, SearchBuilderItem sbi)
			throws SQLException
	{
		PreparedStatement pst = null;
		try
		{
			pst = connection.prepareStatement(" delete from "
					+ SEARCH_BUILDER_ITEM_T + " where id = ? ");
			pst.clearParameters();
			pst.setString(1, sbi.getId());
			pst.execute();
		}
		catch (SQLException ex)
		{
			log.warn("Failed ", ex);
			throw ex;
		}
		finally
		{
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
			}
		}

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
				&& !SearchBuilderItem.GLOBAL_CONTEXT.equals(siteMaster
						.getContext()))
		{
			if (SearchBuilderItem.STATE_PENDING.equals(siteMaster
					.getSearchstate()))
			{
				return siteMaster.getSearchaction();
			}
		}
		return SearchBuilderItem.STATE_UNKNOWN;
	}

	/**
	 * Get the site that the siteMaster references
	 * 
	 * @param siteMaster
	 * @return
	 */
	private String getSiteMasterSite(SearchBuilderItem siteMaster)
	{
		if (siteMaster.getName().startsWith(SearchBuilderItem.INDEX_MASTER)
				&& !SearchBuilderItem.GLOBAL_CONTEXT.equals(siteMaster
						.getContext()))
		{
			// this depends on the pattern, perhapse it should be a parse
			return siteMaster.getName().substring(
					SearchBuilderItem.INDEX_MASTER.length() + 1);
		}
		return null;

	}

	private Integer getMasterAction(Connection connection) throws SQLException
	{
		return getMasterAction(getMasterItem(connection));
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
			if (SearchBuilderItem.STATE_PENDING.equals(master.getSearchstate()))
			{
				return master.getSearchaction();
			}
		}
		return SearchBuilderItem.STATE_UNKNOWN;
	}

	private List findPending(int batchSize, Connection connection,
			SearchIndexBuilderWorker worker) throws SQLException
	{
		// Pending is the first 100 items
		// State == PENDING
		// Action != Unknown
		long start = System.currentTimeMillis();
		try
		{
			log.debug("TXFind pending with " + connection);

			SearchBuilderItem masterItem = getMasterItem(connection);
			Integer masterAction = getMasterAction(masterItem);
			log.debug(" Master Item is " + masterItem.getName() + ":"
					+ masterItem.getSearchaction() + ":"
					+ masterItem.getSearchstate() + "::"
					+ masterItem.getVersion());
			if (SearchBuilderItem.ACTION_REFRESH.equals(masterAction))
			{
				log.debug(" Master Action is " + masterAction);
				log.debug("  REFRESH = " + SearchBuilderItem.ACTION_REFRESH);
				log.debug("  RELOAD = " + SearchBuilderItem.ACTION_REBUILD);
				// get a complete list of all items, before the master
				// action version
				// if there are none, update the master action action to
				// completed
				// and return a blank list

				refreshIndex(connection, masterItem);

			}
			else if (SearchBuilderItem.ACTION_REBUILD.equals(masterAction))
			{
				rebuildIndex(connection, masterItem, worker);
			}
			else
			{
				// get all site masters and perform the required action.
				List siteMasters = getSiteMasterItems(connection);
				for (Iterator i = siteMasters.iterator(); i.hasNext();)
				{
					SearchBuilderItem siteMaster = (SearchBuilderItem) i.next();
					Integer action = getSiteMasterAction(siteMaster);
					if (SearchBuilderItem.ACTION_REBUILD.equals(action))
					{
						rebuildIndex(connection, siteMaster, worker);
					}
					else if (SearchBuilderItem.ACTION_REFRESH.equals(action))
					{
						refreshIndex(connection, siteMaster);
					}
				}
			}
			PreparedStatement pst = null;
			PreparedStatement lockedPst = null;
			ResultSet rst = null;
			try
			{
				pst = connection
						.prepareStatement("select "
								+ SEARCH_BUILDER_ITEM_FIELDS
								+ " from "
								+ SEARCH_BUILDER_ITEM_T
								+ " where searchstate = ? and    searchaction <> ? and "
								+ "        not ( name like ? )  order by version ");
				lockedPst = connection.prepareStatement("update "
						+ SEARCH_BUILDER_ITEM_T + " set searchstate = ? "
						+ " where id = ?  and  searchstate = ? ");
				pst.clearParameters();
				pst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
				pst.setInt(2, SearchBuilderItem.ACTION_UNKNOWN.intValue());
				pst.setString(3, SearchBuilderItem.SITE_MASTER_PATTERN);
				rst = pst.executeQuery();
				ArrayList a = new ArrayList();
				while (rst.next() && a.size() < batchSize)
				{

					SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
					populateSearchBuilderItem(rst, sbi);
					lockedPst.clearParameters();
					lockedPst.setInt(1, SearchBuilderItem.STATE_LOCKED
							.intValue());
					lockedPst.setString(2, sbi.getId());
					lockedPst.setInt(3, SearchBuilderItem.STATE_PENDING
							.intValue());
					if (lockedPst.executeUpdate() == 1)
					{
						sbi.setSearchstate(SearchBuilderItem.STATE_LOCKED);
						a.add(sbi);
					}
					connection.commit();

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
				}
				try
				{
					pst.close();
				}
				catch (Exception ex)
				{
				}
			}

		}
		finally
		{
			long finish = System.currentTimeMillis();
			log.debug(" findPending took " + (finish - start) + " ms");
		}
	}

	public int countPending(Connection connection)
	{

		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{
			pst = connection.prepareStatement("select count(*) from "
					+ SEARCH_BUILDER_ITEM_T
					+ " where searchstate = ? and searchaction <> ?");
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
			pst.setInt(2, SearchBuilderItem.ACTION_UNKNOWN.intValue());
			rst = pst.executeQuery();
			if (rst.next())
			{
				return rst.getInt(1);
			}
			return 0;
		}
		catch (SQLException sqlex)
		{
			return 0;
		}
		finally
		{
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
			};
		}

	}

	private void rebuildIndex(Connection connection,
			SearchBuilderItem controlItem, SearchIndexBuilderWorker worker)
			throws SQLException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("DELETE ALL RECORDS ==========================================================");
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{
				stm.execute("delete from searchbuilderitem where name <> '"
						+ SearchBuilderItem.GLOBAL_MASTER + "' ");
			}
			else
			{
				stm.execute("delete from searchbuilderitem where context = '"
						+ controlItem.getContext() + "' and name <> '"
						+ controlItem.getName() + "' ");

			}

			log
					.debug("DONE DELETE ALL RECORDS ===========================================================");
			connection.commit();
			log
					.debug("ADD ALL RECORDS ===========================================================");
			long lastupdate = System.currentTimeMillis();
			List contextList = new ArrayList();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{

				for (Iterator i = SiteService.getSites(SelectionType.ANY, null,
						null, null, SortType.NONE, null).iterator(); i
						.hasNext();)
				{
					Site s = (Site) i.next();
					if (!SiteService.isSpecialSite(s.getId())
							|| SiteService.isUserSite(s.getId()))
					{
						if (searchIndexBuilder.isOnlyIndexSearchToolSites())
						{
							ToolConfiguration t = s
									.getToolForCommonId("sakai.search");
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
				log.info("Rebuild for " + siteContext);
				for (Iterator i = searchIndexBuilder.getContentProducers()
						.iterator(); i.hasNext();)
				{
					EntityContentProducer ecp = (EntityContentProducer) i
							.next();

					Iterator contentIterator = null;
					contentIterator = ecp.getSiteContentIterator(siteContext);
					log.debug("Using ECP " + ecp);

					int added = 0;
					for (; contentIterator.hasNext();)
					{
						if ((System.currentTimeMillis() - lastupdate) > 60000L)
						{
							lastupdate = System.currentTimeMillis();
							if (!worker.getLockTransaction(15L * 60L * 1000L,
									true))
							{
								throw new RuntimeException(
										"Transaction Lock Expired while Rebuilding Index ");
							}
						}
						String resourceName = (String) contentIterator.next();
						log.debug("Checking " + resourceName);
						if (resourceName == null || resourceName.length() > 255)
						{
							log
									.warn("Entity Reference Longer than 255 characters, ignored: Reference="
											+ resourceName);
							continue;
						}
						SearchBuilderItem sbi = new SearchBuilderItemImpl();
						sbi.setName(resourceName);
						sbi.setSearchaction(SearchBuilderItem.ACTION_ADD);
						sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
						sbi.setId(idgenerator.nextIdentifier().toString());
						sbi.setVersion(new Date(System.currentTimeMillis()));
						String context = null;
						try
						{
							context = ecp.getSiteId(resourceName);
						}
						catch (Exception ex)
						{
							log.info("No context for resource " + resourceName
									+ " defaulting to none");
						}
						if (context == null || context.length() == 0)
						{
							context = "none";
						}
						sbi.setContext(context);
						try
						{
							updateOrSave(connection, sbi);
						}
						catch (SQLException sqlex)
						{
							log.error("Failed to update " + sqlex.getMessage());
						}
						connection.commit();

					}
					log.debug(" Added " + added);
				}
			}
			log
					.debug("DONE ADD ALL RECORDS ===========================================================");
			controlItem.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
			updateOrSave(connection, controlItem);
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
			}
		}

	}

	private void refreshIndex(Connection connection,
			SearchBuilderItem controlItem) throws SQLException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("UPDATE ALL RECORDS ==========================================================");
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{
				stm.execute("update searchbuilderitem set searchstate = "
						+ SearchBuilderItem.STATE_PENDING
						+ " where name not like '"
						+ SearchBuilderItem.SITE_MASTER_PATTERN
						+ "' and name <> '" + SearchBuilderItem.GLOBAL_MASTER
						+ "' ");

			}
			else
			{
				stm.execute("update searchbuilderitem set searchstate = "
						+ SearchBuilderItem.STATE_PENDING
						+ " where context = '" + controlItem.getContext()
						+ "' and name <> '" + controlItem.getName() + "'");

			}
			controlItem.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
			updateOrSave(connection, controlItem);
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
			};
		}
	}

	/**
	 * @return Returns the indexStorage.
	 */
	public IndexStorage getIndexStorage()
	{
		return indexStorage;
	}

	/**
	 * @param indexStorage
	 *        The indexStorage to set.
	 */
	public void setIndexStorage(IndexStorage indexStorage)
	{
		this.indexStorage = indexStorage;
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource()
	{
		return dataSource;
	}

	/**
	 * @param dataSource
	 *        The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean isLockRequired()
	{
		return !indexStorage.isMultipleIndexers();
	}

	
}
