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

package org.sakaiproject.search.component.dao.impl;

import java.io.File;
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
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.hibernate.HibernateException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchIndexBuilderWorker;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.rdf.RDFIndexException;
import org.sakaiproject.search.api.rdf.RDFSearchService;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.dao.SearchIndexBuilderWorkerDao;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;

public class SearchIndexBuilderWorkerDaoJdbcImpl implements SearchIndexBuilderWorkerDao
{

	private static final String SEARCH_BUILDER_ITEM_FIELDS = " name, context,  searchaction, searchstate, version, itemscope, id "; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_T = "searchbuilderitem"; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_FIELDS_PARAMS = " ?, ?, ?,  ?, ?, ?, ? "; //$NON-NLS-1$

	private static final String SEARCH_BUILDER_ITEM_FIELDS_UPDATE = " name = ?, context = ?,  searchaction = ?, searchstate = ?, version = ?, itemscope = ? where id = ? "; //$NON-NLS-1$

	private static Log log = LogFactory.getLog(SearchIndexBuilderWorkerDaoJdbcImpl.class);

	/**
	 * sync object
	 */
	// private Object threadStartLock = new Object();
	/**
	 * dependency: the search index builder that is accepting new items
	 */
	private SearchIndexBuilder searchIndexBuilder = null;

	private boolean enabled = false;

	// private EntityManager entityManager;

	private RDFSearchService rdfSearchService = null;

	/**
	 * injected to abstract the storage impl
	 */
	private IndexStorage indexStorage = null;

	private DataSource dataSource = null;

	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		

		enabled = "true".equals(serverConfigurationService.getString("search.enable",
				"false"));

		try
		{
			if (rdfSearchService == null)
			{
				log
						.info("No RDFSearchService has been defined, RDF Indexing not enabled"); //$NON-NLS-1$
			}
			else
			{
				log
						.warn("Experimental RDF Search Service is enabled using implementation " //$NON-NLS-1$
								+ rdfSearchService);
			}

			if (enabled)
			{
				Connection con = null;
				Statement stmt = null;
				ResultSet rs = null;
				try
				{
					con = dataSource.getConnection();
					stmt = con.createStatement();
					int nglobal = 0;
					try
					{
						rs = stmt.executeQuery("select count(*) from "
								+ SEARCH_BUILDER_ITEM_T + " where itemscope = "
								+ SearchBuilderItem.ITEM_GLOBAL_MASTER);
						if (rs.next())
						{
							nglobal = rs.getInt(1);
						}
					}
					catch (Exception ex)
					{
						log
								.error("Schema Has not been updated to include itemscope column, please run SAK-9865.sql script on this database ");
						System.exit(-1);
					}
					if (nglobal == 0)
					{
						// no global items found, almost certainly because data
						// has not been indexed with itemscope
						stmt.execute("update searchbuilderitem set itemscope= "
								+ SearchBuilderItem.ITEM);
						stmt.execute("update searchbuilderitem set itemscope= "
								+ SearchBuilderItem.ITEM_SITE_MASTER
								+ " where name like '"
								+ SearchBuilderItem.SITE_MASTER_PATTERN + "'");
						stmt.execute("update searchbuilderitem set itemscope= "
								+ SearchBuilderItem.ITEM_GLOBAL_MASTER
								+ " where context = '" + SearchBuilderItem.GLOBAL_CONTEXT
								+ "'");
						log.info("Exiting search item builder records have been optimized for itemscope access ");
					}
					con.commit();
				}
				catch (Exception ex)
				{
					try
					{
						con.rollback();
					}
					catch (Exception e)
					{
						log.debug(e);
					}
					log
							.error("Failed to migrate indexes to itemscope based storage, search cant startup, please investigate immediately ");
					System.exit(-1);
				}
				finally
				{
					try
					{
						rs.close();
					}
					catch (Exception e)
					{
						log.debug(e);
					}
					try
					{
						stmt.close();
					}
					catch (Exception e)
					{
						log.debug(e);
					}
					try
					{
						con.close();
					}
					catch (Exception e)
					{
						log.debug(e);
					}
				}
			}

		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t); //$NON-NLS-1$
		}
	}


	private void processDeletes(SearchIndexBuilderWorker worker, Connection connection,
			List runtimeToDo) throws SQLException, IOException
	{

		if (indexStorage.indexExists())
		{
			IndexReader indexReader = null;
			try
			{
				indexReader = indexStorage.getIndexReader();

				// Open the index
				for (Iterator tditer = runtimeToDo.iterator(); worker.isRunning()
						&& tditer.hasNext();)
				{
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					if (!SearchBuilderItem.STATE_LOCKED.equals(sbi.getSearchstate()))
					{
						// should only be getting pending
						// items
						log.warn(" Found Item that was not pending " //$NON-NLS-1$
								+ sbi.getName());
						continue;
					}
					if (SearchBuilderItem.ACTION_UNKNOWN.equals(sbi.getSearchaction()))
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
						if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
						{
							sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
							updateOrSave(connection, sbi);
							connection.commit();
						}
						else
						{
							sbi.setSearchstate(SearchBuilderItem.STATE_PENDING_2);
						}

					}
					catch (IOException ex)
					{
						log.warn("Failed to delete Page ", ex); //$NON-NLS-1$
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

	private void processAdd(SearchIndexBuilderWorker worker, Connection connection,
			List runtimeToDo) throws Exception
	{
		IndexWriter indexWrite = null;
		try
		{
			if (worker.isRunning())
			{
				indexWrite = indexStorage.getIndexWriter(false);
			}
			long last = System.currentTimeMillis();

			for (Iterator tditer = runtimeToDo.iterator(); worker.isRunning()
					&& tditer.hasNext();)
			{

				Reader contentReader = null;
				try
				{
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					// only add adds, that have been deleted or are locked
					// sucessfully
					if (!SearchBuilderItem.STATE_PENDING_2.equals(sbi.getSearchstate())
							&& !SearchBuilderItem.STATE_LOCKED.equals(sbi
									.getSearchstate()))
					{
						continue;
					}
					// Reference ref =
					// entityManager.newReference(sbi.getName());
					String ref = sbi.getName();
					if (ref == null)
					{
						log
								.error("Unrecognised trigger object presented to index builder " //$NON-NLS-1$
										+ sbi);
					}

					long startDocIndex = System.currentTimeMillis();
					worker.setStartDocIndex(startDocIndex);
					worker.setNowIndexing(ref);

					try
					{
						try
						{
							// Entity entity = ref.getEntity();
							EntityContentProducer sep = searchIndexBuilder
									.newEntityContentProducer(ref);
							boolean indexDoc = true;
							if (searchIndexBuilder.isOnlyIndexSearchToolSites())
							{
								try
								{
									String siteId = sep.getSiteId(sbi.getName());
									Site s = SiteService.getSite(siteId);
									ToolConfiguration t = s
											.getToolForCommonId("sakai.search"); //$NON-NLS-1$
									if (t == null)
									{
										indexDoc = false;
										log.debug("Not indexing " //$NON-NLS-1$
												+ sbi.getName()
												+ " as it has no search tool"); //$NON-NLS-1$
									}
								}
								catch (Exception ex)
								{
									indexDoc = false;
									log.debug("Not indexing  " + sbi.getName() //$NON-NLS-1$
											+ " as it has no site", ex); //$NON-NLS-1$

								}
							}
							if (indexDoc && sep != null && sep.isForIndex(ref)
									&& sep.getSiteId(ref) != null)
							{

								Document doc = new Document();
								Reference r;
								String container = sep.getContainer(ref);
								if (container == null) container = ""; //$NON-NLS-1$
								doc.add(new Field(SearchService.DATE_STAMP, String
										.valueOf(System.currentTimeMillis()),
										Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_CONTAINER,
										filterNull(container), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_ID, filterNull(sep
										.getId(ref)), Field.Store.COMPRESS,
										Field.Index.NO));
								doc.add(new Field(SearchService.FIELD_TYPE,
										filterNull(sep.getType(ref)),
										Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_SUBTYPE,
										filterNull(sep.getSubType(ref)),
										Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_REFERENCE,
										filterNull(ref), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));

								doc.add(new Field(SearchService.FIELD_CONTEXT,
										filterNull(sep.getSiteId(ref)),
										Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
								if (sep.isContentFromReader(ref))
								{
									contentReader = sep.getContentReader(ref);
									if ( log.isDebugEnabled() ) {
										log.debug("Adding Content for "+ref+" using "+contentReader);
									}
									doc.add(new Field(SearchService.FIELD_CONTENTS,
											contentReader, Field.TermVector.YES));
								}
								else
								{
									String content = sep.getContent(ref);
									if ( log.isDebugEnabled() ) {
										log.debug("Adding Content for "+ref+" as ["+content+"]");
									}
									doc.add(new Field(SearchService.FIELD_CONTENTS,
											filterNull(content),
											Field.Store.NO, Field.Index.TOKENIZED,
											Field.TermVector.YES));
								}

								doc.add(new Field(SearchService.FIELD_TITLE,
										filterNull(sep.getTitle(ref)),
										Field.Store.COMPRESS, Field.Index.TOKENIZED,
										Field.TermVector.YES));
								doc.add(new Field(SearchService.FIELD_TOOL,
										filterNull(sep.getTool()), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_URL, filterUrl(filterNull(sep
										.getUrl(ref))), Field.Store.COMPRESS,
										Field.Index.UN_TOKENIZED));
								doc.add(new Field(SearchService.FIELD_SITEID,
										filterNull(sep.getSiteId(ref)),
										Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));

								// add the custom properties

								Map m = sep.getCustomProperties(ref);
								if (m != null)
								{
									for (Iterator cprops = m.keySet().iterator(); cprops
											.hasNext();)
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
													.info("Null Custom Properties value has been suppled by " //$NON-NLS-1$
															+ sep + " in index " //$NON-NLS-1$
															+ key);
										}
										else
										{
											for (int i = 0; i < values.length; i++)
											{
												if (key.startsWith("T"))
												{
													key = key.substring(1);
													doc.add(new Field(key,
															filterNull(values[i]),
															Field.Store.COMPRESS,
															Field.Index.TOKENIZED,Field.TermVector.YES));
												}
												else
												{
													doc.add(new Field(key,
															filterNull(values[i]),
															Field.Store.COMPRESS,
															Field.Index.UN_TOKENIZED));
												}
											}
										}
									}
								}

								log.debug("Indexing Document " + doc); //$NON-NLS-1$

								indexWrite.addDocument(doc);

								log.debug("Done Indexing Document " + doc); //$NON-NLS-1$

								processRDF(ref, sep);

							}
							else
							{
								if (log.isDebugEnabled())
								{
									if (!indexDoc)
									{
										log
												.debug("Ignored Document: Fileteed out by site " + ref); //$NON-NLS-1$
									}
									else if (sep == null)
									{
										log
												.debug("Ignored Document: No EntityContentProducer " + ref); //$NON-NLS-1$

									}
									else if (!sep.isForIndex(ref))
									{
										log
												.debug("Ignored Document: Marked as Ignore " + ref); //$NON-NLS-1$

									}
									else if (sep.getSiteId(ref) == null)
									{
										log.debug("Ignored Document: No Site ID " + ref); //$NON-NLS-1$

									}
									else
									{
										log
												.debug("Ignored Document: Reason Unknown " + ref); //$NON-NLS-1$

									}
								}
							}
						}
						catch (Exception e1)
						{
							log.debug(" Failed to index document for " + ref + " cause: " //$NON-NLS-1$
									+ e1.getMessage(), e1);
						}
						sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
						updateOrSave(connection, sbi);
						connection.commit();
					}
					catch (Exception e1)
					{
						log.debug(" Failed to index document cause: " //$NON-NLS-1$
								+ e1.getMessage());
					}
					long endDocIndex = System.currentTimeMillis();
					worker.setLastIndex(endDocIndex - startDocIndex);
					if ((endDocIndex - startDocIndex) > 60000L)
					{
						log.warn("Slow index operation " //$NON-NLS-1$
								+ String.valueOf((endDocIndex - startDocIndex) / 1000)
								+ " seconds to index " //$NON-NLS-1$
								+ ref);
					}
					// update this node lock to indicate its
					// still alove, no document should
					// take more than 2 mins to process
					// ony do this check once every minute
					long now = System.currentTimeMillis();
					if ((now - last) > (60L * 1000L))
					{
						last = System.currentTimeMillis();
						if (!worker.getLockTransaction(15L * 60L * 1000L, true))
						{
							throw new Exception(
									"Transaction Lock Expired while indexing " //$NON-NLS-1$
											+ ref);
						}
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
							log.debug(ioex);
						}
					}
				}

			}
			worker.setStartDocIndex(System.currentTimeMillis());
			worker.setNowIndexing(Messages
					.getString("SearchIndexBuilderWorkerDaoJdbcImpl.33")); //$NON-NLS-1$
		}
		catch (Exception ex)
		{
			log.error("Failed to Add Documents ", ex);
			throw new Exception(ex);
		}
		finally
		{
			if (indexWrite != null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Closing Index Writer With " + indexWrite.docCount()
							+ " documents");
					Directory d = indexWrite.getDirectory();
					String[] s = d.list();
					log.debug("Directory Contains ");
					for (int i = 0; i < s.length; i++)
					{
						File f = new File(s[i]);
						log.debug("\t" + String.valueOf(f.length()) + "\t"
								+ new Date(f.lastModified()) + "\t" + s[i]);
					}
				}
				indexStorage.closeIndexWriter(indexWrite);
			}
		}

	}

	/**
	 * @param string
	 * @return
	 */
	private String filterUrl(String url)
	{
		String serverURL = serverConfigurationService.getServerUrl();
		if ( url != null && url.startsWith(serverURL) ) {
			String absUrl = url.substring(serverURL.length());
			if ( !absUrl.startsWith("/") ) {
				absUrl = "/" + absUrl;
			}
			return absUrl;
		}
		return url;
	}

	/**
	 * @param title
	 * @return
	 */
	private String filterNull(String s)
	{
		if (s == null)
		{
			return "";
		}
		return s;
	}

	private int completeUpdate(SearchIndexBuilderWorker worker, Connection connection,
			List runtimeToDo) throws Exception
	{
		try
		{

			for (Iterator tditer = runtimeToDo.iterator(); worker.isRunning()
					&& tditer.hasNext();)
			{
				SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
				if (SearchBuilderItem.STATE_COMPLETED.equals(sbi.getSearchstate()))
				{
					if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
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
					.warn("Failed to update state in database due to " //$NON-NLS-1$
							+ ex.getMessage()
							+ " this will be corrected on the next run of the IndexBuilder, no cause for alarm"); //$NON-NLS-1$
		}
		return 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.dao.impl.SearchIndexBuilderWorkerDao#processToDoListTransaction()
	 */
	public void processToDoListTransaction(SearchIndexBuilderWorker worker,
			int indexBatchSize)
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

			log.debug("Processing " + totalDocs + " documents"); //$NON-NLS-1$ //$NON-NLS-2$

			if (totalDocs > 0)
			{
				log.debug("Preupdate Start");
				indexStorage.doPreIndexUpdate();
				log.debug("Preupdate End");

				// get lock

				// this needs to be exclusive
				log.debug("Process Deletes Start");

				processDeletes(worker, connection, runtimeToDo);
				log.debug("Process Deletes End");

				// upate and release lock
				// after a process Deletes the index needs to updated

				// can be parallel
				log.debug("Process Add Start");

				processAdd(worker, connection, runtimeToDo);
				log.debug("Process Add End");
				log.debug("Complete Update Start");

				completeUpdate(worker, connection, runtimeToDo);
				log.debug("Complete Update End");

				// get lock
				try
				{
					log.debug("Post update Start");
					indexStorage.doPostIndexUpdate();
					log.debug("Post update End");
				}
				catch (IOException e)
				{
					log.error("Failed to do Post Index Update", e); //$NON-NLS-1$
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
					log.info("Completed Process List of " + totalDocs + " at " //$NON-NLS-1$ //$NON-NLS-2$
							+ docspersec + " documents/per second "); //$NON-NLS-1$
				}
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to perform index cycle " + ex.getMessage()); //$NON-NLS-1$
			log.debug("Traceback is ", ex); //$NON-NLS-1$
		}
		finally
		{
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.dao.SearchIndexBuilderWorkerDao#createIndexTransaction(org.sakaiproject.search.api.SearchIndexBuilderWorker)
	 */
	public void createIndexTransaction(SearchIndexBuilderWorker worker)
	{
		Connection connection = null;
		try
		{
			connection = dataSource.getConnection();
			long startTime = System.currentTimeMillis();
			int totalDocs = 0;

			log.debug("Preupdate Start");
			indexStorage.doPreIndexUpdate();
			log.debug("Preupdate End");

			createIndex(worker, connection);

			// get lock
			try
			{
				log.debug("Post update Start");
				indexStorage.doPostIndexUpdate();
				log.debug("Post update End");
			}
			catch (IOException e)
			{
				log.error("Failed to do Post Index Update", e); //$NON-NLS-1$
			}
			log.info("Created Index"); //$NON-NLS-1$
		}
		catch (Exception ex)
		{
			log.warn("Failed to create Index " + ex.getMessage()); //$NON-NLS-1$
			log.debug("Traceback is ", ex); //$NON-NLS-1$
		}
		finally
		{
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
	 * @param worker
	 * @param connection
	 * @throws Exception
	 */
	private void createIndex(SearchIndexBuilderWorker worker, Connection connection)
			throws Exception
	{
		IndexWriter indexWrite = null;
		try
		{
			if (worker.isRunning())
			{
				indexWrite = indexStorage.getIndexWriter(false);
			}
			long last = System.currentTimeMillis();

			Document doc = new Document();
			doc
					.add(new Field(SearchService.DATE_STAMP, String.valueOf(System
							.currentTimeMillis()), Field.Store.COMPRESS,
							Field.Index.UN_TOKENIZED));
			doc.add(new Field(SearchService.FIELD_ID, "---INDEX-CREATED---",
					Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
			indexWrite.addDocument(doc);

		}
		catch (Exception ex)
		{
			log.error("Failed to Add Documents ", ex);
			throw new Exception(ex);
		}
		finally
		{
			if (indexWrite != null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Closing Index Writer With " + indexWrite.docCount()
							+ " documents");
					Directory d = indexWrite.getDirectory();
					String[] s = d.list();
					log.debug("Directory Contains ");
					for (int i = 0; i < s.length; i++)
					{
						File f = new File(s[i]);
						log.debug("\t" + String.valueOf(f.length()) + "\t"
								+ new Date(f.lastModified()) + "\t" + s[i]);
					}
				}
				indexStorage.closeIndexWriter(indexWrite);
			}
		}
	}

	private void processRDF(String ref, EntityContentProducer sep) throws RDFIndexException
	{
		if (rdfSearchService != null)
		{
			String s = sep.getCustomRDF(ref);
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
			pst = connection.prepareStatement("select " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_FIELDS + " from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " where itemscope =   ?  "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.ITEM_SITE_MASTER.intValue());
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
				log.debug(ex);
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
	}

	/**
	 * get the Instance Master
	 * 
	 * @return
	 * @throws HibernateException
	 */
	private SearchBuilderItem getMasterItem(Connection connection) throws SQLException
	{
		log.debug("get Master Items with " + connection); //$NON-NLS-1$

		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{
			pst = connection.prepareStatement("select " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_FIELDS + " from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " where itemscope = ? "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.ITEM_GLOBAL_MASTER.intValue());
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
				sbi.setItemscope(SearchBuilderItem.ITEM_GLOBAL_MASTER);
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
				log.debug(ex);
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}
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
				pst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
				log.debug(ex);
			}
		}

	}

	private void delete(Connection connection, SearchBuilderItem sbi) throws SQLException
	{
		PreparedStatement pst = null;
		try
		{
			pst = connection.prepareStatement(" delete from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " where id = ? "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setString(1, sbi.getId());
			pst.execute();
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
				pst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
				&& !SearchBuilderItem.GLOBAL_CONTEXT.equals(siteMaster.getContext()))
		{
			if (SearchBuilderItem.STATE_PENDING.equals(siteMaster.getSearchstate()))
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
				&& !SearchBuilderItem.GLOBAL_CONTEXT.equals(siteMaster.getContext()))
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
			log.debug("TXFind pending with " + connection); //$NON-NLS-1$

			SearchBuilderItem masterItem = getMasterItem(connection);
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
				ArrayList a = new ArrayList();
				while (rst.next() && a.size() < batchSize)
				{

					SearchBuilderItemImpl sbi = new SearchBuilderItemImpl();
					populateSearchBuilderItem(rst, sbi);
					if (!SearchBuilderItem.ACTION_UNKNOWN.equals(sbi.getSearchaction()))
					{
						lockedPst.clearParameters();
						lockedPst.setInt(1, SearchBuilderItem.STATE_LOCKED.intValue());
						lockedPst.setString(2, sbi.getId());
						lockedPst.setInt(3, SearchBuilderItem.STATE_PENDING.intValue());
						if (lockedPst.executeUpdate() == 1)
						{
							sbi.setSearchstate(SearchBuilderItem.STATE_LOCKED);
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
					rst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
				try
				{
					pst.close();
				}
				catch (Exception ex)
				{
					log.debug(ex);
				}
			}

		}
		finally
		{
			long finish = System.currentTimeMillis();
			log.debug(" findPending took " + (finish - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public int countPending(Connection connection)
	{

		PreparedStatement pst = null;
		ResultSet rst = null;
		try
		{

			pst = connection.prepareStatement("select count(*) from " //$NON-NLS-1$
					+ SEARCH_BUILDER_ITEM_T + " where searchstate = ? "); //$NON-NLS-1$
			pst.clearParameters();
			pst.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
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
				rst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				pst.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
		}

	}

	private void rebuildIndex(Connection connection, SearchBuilderItem controlItem,
			SearchIndexBuilderWorker worker) throws SQLException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.info("DELETE ALL RECORDS =========================================================="); //$NON-NLS-1$
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
				stm.execute("delete from searchbuilderitem where itemscope = "
						+ SearchBuilderItem.ITEM);
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
			List contextList = new ArrayList();
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem.getContext()))
			{

				for (Iterator i = SiteService.getSites(SelectionType.ANY, null, null,
						null, SortType.NONE, null).iterator(); i.hasNext();)
				{
					Site s = (Site) i.next();
					if (!SiteService.isSpecialSite(s.getId()))
					{
						if (searchIndexBuilder.isOnlyIndexSearchToolSites())
						{
							ToolConfiguration t = s.getToolForCommonId("sakai.search"); //$NON-NLS-1$
							if (t != null)
							{
								contextList.add(s.getId());
							}
						}
						else if (!(searchIndexBuilder.isExcludeUserSites() && SiteService.isUserSite(s.getId())))
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
				log.debug("Rebuild for " + siteContext); //$NON-NLS-1$
				for (Iterator i = searchIndexBuilder.getContentProducers().iterator(); i
						.hasNext();)
				{
					EntityContentProducer ecp = (EntityContentProducer) i.next();

					Iterator contentIterator = null;
					contentIterator = ecp.getSiteContentIterator(siteContext);
					log.debug("Using ECP " + ecp); //$NON-NLS-1$

					int added = 0;
					for (; contentIterator.hasNext();)
					{
						if ((System.currentTimeMillis() - lastupdate) > 60000L)
						{
							lastupdate = System.currentTimeMillis();
							if (!worker.getLockTransaction(15L * 60L * 1000L, true))
							{
								throw new RuntimeException(
										"Transaction Lock Expired while Rebuilding Index "); //$NON-NLS-1$
							}
						}
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
			}
			log
					.info("DONE ADD ALL RECORDS ==========================================================="); //$NON-NLS-1$
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
				log.debug(ex);
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
				log.debug(ex);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.dao.SearchIndexBuilderWorkerDao#indexExists()
	 */
	public boolean indexExists()
	{
		return indexStorage.centralIndexExists();
	}


	/**
	 * @return the rdfSearchService
	 */
	public RDFSearchService getRdfSearchService()
	{
		return rdfSearchService;
	}


	/**
	 * @param rdfSearchService the rdfSearchService to set
	 */
	public void setRdfSearchService(RDFSearchService rdfSearchService)
	{
		this.rdfSearchService = rdfSearchService;
	}


	/**
	 * @return the searchIndexBuilder
	 */
	public SearchIndexBuilder getSearchIndexBuilder()
	{
		return searchIndexBuilder;
	}


	/**
	 * @param searchIndexBuilder the searchIndexBuilder to set
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
	}


	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}


	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

}
