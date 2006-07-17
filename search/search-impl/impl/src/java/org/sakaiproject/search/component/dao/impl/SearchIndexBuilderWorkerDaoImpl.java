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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.type.Type;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SearchIndexBuilderWorkerDaoImpl extends HibernateDaoSupport
		implements SearchIndexBuilderWorkerDao
{

	private static Log log = LogFactory
			.getLog(SearchIndexBuilderWorkerDaoImpl.class);

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

	private EventTrackingService eventTrackingService;

	private RDFSearchService rdfSearchService = null;

	
	/**
	 * injected to abstract the storage impl
	 */
	private IndexStorage indexStorage = null;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		eventTrackingService = (EventTrackingService) load(cm,
				EventTrackingService.class.getName(),true);
		entityManager = (EntityManager) load(cm, EntityManager.class.getName(),true);
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName(),true);
		rdfSearchService = (RDFSearchService) load(cm, RDFSearchService.class.getName(),false);

		enabled = "true".equals(ServerConfigurationService.getString(
				"search.experimental", "false"));
		try
		{
			if (searchIndexBuilder == null)
			{
				log.error("Search Index Worker needs searchIndexBuilder ");
			}
			if (eventTrackingService == null)
			{
				log.error("Search Index Worker needs EventTrackingService ");
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
				log.info("No RDFSearchService has been defined, RDF Indexing not enabled");
			} else {
				log.warn("Experimental RDF Search Service is enabled using implementation "+rdfSearchService);
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
			if ( aserror ) {
				log.error("Cant find Spring component named " + name);
			}
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.dao.impl.SearchIndexBuilderWorkerDao#processToDoListTransaction()
	 */
	public void processToDoListTransaction(final SearchIndexBuilderWorker worker)
	{
		long startTime = System.currentTimeMillis();
		HibernateCallback callback = new HibernateCallback()
		{

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException
			{

				int totalDocs = 0;
				try
				{

					IndexWriter indexWrite = null;

					// Load the list

					List runtimeToDo = findPending(indexBatchSize, session);

					totalDocs = runtimeToDo.size();

					log.debug("Processing " + totalDocs + " documents");

					if (totalDocs > 0)
					{
						try
						{
							indexStorage.doPreIndexUpdate();
							if (indexStorage.indexExists())
							{
								IndexReader indexReader = null;
								try
								{
									indexReader = indexStorage.getIndexReader();

									// Open the index
									for (Iterator tditer = runtimeToDo
											.iterator(); worker.isRunning()
											&& tditer.hasNext();)
									{
										SearchBuilderItem sbi = (SearchBuilderItem) tditer
												.next();
										if (!SearchBuilderItem.STATE_PENDING
												.equals(sbi.getSearchstate()))
										{
											// should only be getting pending
											// items
											log
													.warn(" Found Item that was not pending "
															+ sbi.getName());
											continue;
										}
										if (SearchBuilderItem.ACTION_UNKNOWN
												.equals(sbi.getSearchaction()))
										{
											sbi
													.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
											
											continue;
										}
										// remove document
										try
										{
											indexReader
													.deleteDocuments(new Term(
															SearchService.FIELD_REFERENCE,
															sbi.getName()));
											if (SearchBuilderItem.ACTION_DELETE
													.equals(sbi
															.getSearchaction()))
											{
												sbi
														.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
									
											}
											else
											{
												sbi
														.setSearchstate(SearchBuilderItem.STATE_PENDING_2);
											}

										}
										catch (IOException ex)
										{
											log.warn("Failed to delete Page ",
													ex);
										}
									}
								}
								finally
								{
									if (indexReader != null)
									{

										indexReader.close();
										indexReader = null;
									}
								}
								if (worker.isRunning())
								{
									indexWrite = indexStorage
											.getIndexWriter(false);
								}
							}
							else
							{
								// create for update
								if (worker.isRunning())
								{
									indexWrite = indexStorage
											.getIndexWriter(true);
								}
							}
							for (Iterator tditer = runtimeToDo.iterator(); worker
									.isRunning()
									&& tditer.hasNext();)
							{
								Reader contentReader = null;
								try
								{
									SearchBuilderItem sbi = (SearchBuilderItem) tditer
											.next();
									// only add adds, that have been deleted
									// sucessfully
									if (!SearchBuilderItem.STATE_PENDING_2
											.equals(sbi.getSearchstate()))
									{
										continue;
									}
									Reference ref = entityManager
											.newReference(sbi.getName());

									if (ref == null)
									{
										log
												.error("Unrecognised trigger object presented to index builder "
														+ sbi);
									}

									long startDocIndex = System.currentTimeMillis();
									log.info("Indexing "+ref.getReference());
									
									try
									{
										Entity entity = ref.getEntity();
										EntityContentProducer sep = searchIndexBuilder
												.newEntityContentProducer(ref);
										if (sep != null && sep.isForIndex(ref) && ref.getContext() != null)
										{

											Document doc = new Document();
											String container = ref
													.getContainer();
											if (container == null)
												container = "";
											doc
											.add(new Field(
													SearchService.DATE_STAMP,
													String.valueOf(System.currentTimeMillis()),
													Field.Store.YES,
													Field.Index.UN_TOKENIZED));
											doc
													.add(new Field(
															SearchService.FIELD_CONTAINER,
															container,
															Field.Store.YES,
															Field.Index.UN_TOKENIZED));
											doc.add(new Field(
													SearchService.FIELD_ID, ref
															.getId(),
													Field.Store.YES,
													Field.Index.NO));
											doc.add(new Field(
													SearchService.FIELD_TYPE,
													ref.getType(),
													Field.Store.YES,
													Field.Index.UN_TOKENIZED));
											doc
													.add(new Field(
															SearchService.FIELD_SUBTYPE,
															ref.getSubType(),
															Field.Store.YES,
															Field.Index.UN_TOKENIZED));
											doc
													.add(new Field(
															SearchService.FIELD_REFERENCE,
															ref.getReference(),
															Field.Store.YES,
															Field.Index.UN_TOKENIZED));

											doc
													.add(new Field(
															SearchService.FIELD_CONTEXT,
															sep.getSiteId(ref),
															Field.Store.YES,
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
												doc
														.add(new Field(
																SearchService.FIELD_CONTENTS,
																sep
																		.getContent(entity),
																Field.Store.YES,
																Field.Index.TOKENIZED,
																Field.TermVector.YES));
											}
											doc.add(new Field(
													SearchService.FIELD_TITLE,
													sep.getTitle(entity),
													Field.Store.YES,
													Field.Index.TOKENIZED,
													Field.TermVector.YES));
											doc.add(new Field(
													SearchService.FIELD_TOOL,
													sep.getTool(),
													Field.Store.YES,
													Field.Index.UN_TOKENIZED));
											doc.add(new Field(
													SearchService.FIELD_URL,
													sep.getUrl(entity),
													Field.Store.YES,
													Field.Index.UN_TOKENIZED));
											doc.add(new Field(
													SearchService.FIELD_SITEID,
													sep.getSiteId(ref),
													Field.Store.YES,
													Field.Index.UN_TOKENIZED));

											// add the custom properties

											Map m = sep.getCustomProperties();
											if (m != null)
											{
												for (Iterator cprops = m
														.keySet().iterator(); cprops
														.hasNext();)
												{
													String key = (String) cprops
															.next();
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
																			Field.Store.YES,
																			Field.Index.UN_TOKENIZED));
														}
													}
												}
											}

											log.debug("Indexing Document "
													+ doc);
											
											indexWrite.addDocument(doc);
											
											log.debug("Done Indexing Document "
													+ doc);
											
											processRDF(sep);
											
										}
										else
										{
											log.debug("Ignored Document "
													+ ref.getId());
										}
										sbi
												.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
									}
									catch (Exception e1)
									{
										log
												.debug(" Failed to index document cause: "
														+ e1.getMessage());
									}
									long endDocIndex = System.currentTimeMillis();
									if ( (endDocIndex - startDocIndex) > 60000L) {
										log.warn("Slow index operation "+String.valueOf((endDocIndex-startDocIndex)/1000)+" seconds to index "+ref.getReference());
									}
									// update this node lock to indicate its
									// still alove, no document should
									// take more than 2 mins to process
									// refresh the lock
									if ( !worker.getLockTransaction(15L * 60L * 1000L,true) ) {
										throw new HibernateException("Transaction Lock Expired while indexing "+ref.getReference());
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
						}
						finally
						{
							if (indexWrite != null)
							{
								indexWrite.close();
								indexWrite = null;
							}
						}
						totalDocs = 0;
						try
						{

							for (Iterator tditer = runtimeToDo.iterator(); worker
									.isRunning()
									&& tditer.hasNext();)
							{
								SearchBuilderItem sbi = (SearchBuilderItem) tditer
										.next();
								if (SearchBuilderItem.STATE_COMPLETED
										.equals(sbi.getSearchstate()))
								{
									if (SearchBuilderItem.ACTION_DELETE
											.equals(sbi.getSearchaction()))
									{
										session.delete(sbi);
									}
									else
									{
										session.saveOrUpdate(sbi);
									}

								}
							}
							session.flush();
							totalDocs = runtimeToDo.size();
						}
						catch (Exception ex)
						{
							log
									.warn("Failed to update state in database due to "
											+ ex.getMessage()
											+ " this will be corrected on the next run of the IndexBuilder, no cause for alarm");
						}
					}

					return new Integer(totalDocs);
				}
				catch (IOException ex)
				{
					throw new HibernateException(" Failed to create index ", ex);
				}
			}


		};
		int totalDocs = 0;
		if (worker.isRunning())
		{
			Integer nprocessed = (Integer) getHibernateTemplate().execute(
					callback);
			if (nprocessed == null)
			{
				return;
			}
			totalDocs = nprocessed.intValue();
		}

		try
		{
			indexStorage.doPostIndexUpdate();
		}
		catch (IOException e)
		{
			log.error("Failed to do Post Index Update", e);
		}

		if (worker.isRunning())
		{

			eventTrackingService.post(eventTrackingService.newEvent(
					SearchService.EVENT_TRIGGER_INDEX_RELOAD,
					"/searchindexreload", true,
					NotificationService.PREF_IMMEDIATE));
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
	private void processRDF(EntityContentProducer sep) throws RDFIndexException
	{
		if ( rdfSearchService != null ) {
			String s = sep.getCustomRDF();
			if ( s != null ) {
				rdfSearchService.addData(s);
			}
		}
	}

	/**
	 * Gets a list of all SiteMasterItems
	 * 
	 * @return
	 * @throws HibernateException
	 */
	private List getSiteMasterItems(Session session) throws HibernateException
	{
		log.debug("Site Master Items with " + session);
		List masterList = (List) session.createCriteria(
				SearchBuilderItemImpl.class).add(
				Expression.like("name", SearchBuilderItem.SITE_MASTER_PATTERN))
				.add(
						Expression.not(Expression.eq("context",
								SearchBuilderItem.GLOBAL_CONTEXT))).list();

		if (masterList == null || masterList.size() == 0)
		{
			return new ArrayList();
		}
		else
		{
			return masterList;
		}

	}

	/**
	 * get the Instance Master
	 * 
	 * @return
	 * @throws HibernateException
	 */
	private SearchBuilderItem getMasterItem(Session session)
			throws HibernateException
	{
		log.debug("get Master Items with " + session);

		List master = (List) session
				.createCriteria(SearchBuilderItemImpl.class).add(
						Expression.eq("name", SearchBuilderItem.GLOBAL_MASTER))
				.list();

		if (master != null && master.size() != 0)
		{
			return (SearchBuilderItem) master.get(0);
		}
		SearchBuilderItem sbi = new SearchBuilderItemImpl();
		sbi.setName(SearchBuilderItem.INDEX_MASTER);
		sbi.setContext(SearchBuilderItem.GLOBAL_CONTEXT);
		sbi.setSearchaction(SearchBuilderItem.ACTION_UNKNOWN);
		sbi.setSearchstate(SearchBuilderItem.STATE_UNKNOWN);
		return sbi;
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

	/**
	 * get the action of the master item
	 * 
	 * @return
	 */
	private Integer getMasterAction(Session session) throws HibernateException
	{
		return getMasterAction(getMasterItem(session));
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

	/**
	 * get the next x pending items If there is a master record with index
	 * refresh, the list will come back with only items existing before the
	 * index refresh was requested, those requested after the index refresh will
	 * be processed once the refresh has been completed. If a rebuild is
	 * request, then the index queue will be deleted, and all the
	 * entitiescontentproviders polled to get all their entities
	 * 
	 * @return
	 * @throws HibernateException
	 */
	private List findPending(int batchSize, Session session)
			throws HibernateException
	{
		// Pending is the first 100 items
		// State == PENDING
		// Action != Unknown
		long start = System.currentTimeMillis();
		try
		{
			log.debug("TXFind pending with " + session);

			SearchBuilderItem masterItem = getMasterItem(session);
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

				refreshIndex(session, masterItem);

			}
			else if (SearchBuilderItem.ACTION_REBUILD.equals(masterAction))
			{
				rebuildIndex(session, masterItem);
			}
			else
			{
				// get all site masters and perform the required action.
				List siteMasters = getSiteMasterItems(session);
				for (Iterator i = siteMasters.iterator(); i.hasNext();)
				{
					SearchBuilderItem siteMaster = (SearchBuilderItem) i.next();
					Integer action = getSiteMasterAction(siteMaster);
					if (SearchBuilderItem.ACTION_REBUILD.equals(action))
					{
						rebuildIndex(session, siteMaster);
					}
					else if (SearchBuilderItem.ACTION_REFRESH.equals(action))
					{
						refreshIndex(session, siteMaster);
					}
				}
			}
			return session.createCriteria(SearchBuilderItemImpl.class).add(
					Expression.eq("searchstate",
							SearchBuilderItem.STATE_PENDING)).add(
					Expression.not(Expression.eq("searchaction",
							SearchBuilderItem.ACTION_UNKNOWN))).add(
					Expression.not(Expression.like("name",
							SearchBuilderItem.SITE_MASTER_PATTERN))).addOrder(
					Order.asc("version")).setMaxResults(batchSize).list();

		}
		finally
		{
			long finish = System.currentTimeMillis();
			log.debug(" findPending took " + (finish - start) + " ms");
		}
	}

	public int countPending()
	{
		HibernateCallback callback = new HibernateCallback()
		{

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException
			{
				List l = session
						.createQuery(
								"select count(*) from "
										+ SearchBuilderItemImpl.class.getName()
										+ " where searchstate = ? and searchaction <> ?")
						.setParameters(
								new Object[] { SearchBuilderItem.STATE_PENDING,
										SearchBuilderItem.ACTION_UNKNOWN },
								new Type[] { Hibernate.INTEGER,
										Hibernate.INTEGER }).list();
				if (l == null || l.size() == 0)
				{
					return new Integer(0);
				}
				else
				{
					log.debug("Found " + l.get(0) + " Pending Documents ");
					return l.get(0);
				}
			}

		};

		Integer np = (Integer) getHibernateTemplate().execute(callback);
		return np.intValue();

	}

	private void rebuildIndex(Session session, SearchBuilderItem controlItem)
			throws HibernateException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("DELETE ALL RECORDS ==========================================================");
		session.flush();
		try
		{
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{
				session.connection().createStatement().execute(
						"delete from searchbuilderitem where name <> '"
								+ SearchBuilderItem.GLOBAL_MASTER + "' ");
			}
			else
			{
				session.connection().createStatement().execute(
						"delete from searchbuilderitem where context = '"
								+ controlItem.getContext() + "' and name <> '"
								+ controlItem.getName() + "' ");

			}
		}
		catch (SQLException e)
		{
			throw new HibernateException("Failed to perform delete ", e);
		}

		// THIS DOES NOT WORK IN H 2.1 session.delete("from
		// "+SearchBuilderItemImpl.class.getName());
		log
				.debug("DONE DELETE ALL RECORDS ===========================================================");
		log
				.debug("ADD ALL RECORDS ===========================================================");
		for (Iterator i = searchIndexBuilder.getContentProducers().iterator(); i
				.hasNext();)
		{
			EntityContentProducer ecp = (EntityContentProducer) i.next();
			List contentList = null;
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{
				contentList = ecp.getAllContent();
			}
			else
			{
				contentList = ecp.getSiteContent(controlItem.getContext());
			}
			int added = 0;
			for (Iterator ci = contentList.iterator(); ci.hasNext();)
			{
				String resourceName = (String) ci.next();
				if ( resourceName == null || resourceName.length() > 255 ) {
					log.warn("Entity Reference Longer than 255 characters, ignored: Reference="+resourceName);
					continue;
				}
				List lx = session.createQuery(
						" from " + SearchBuilderItemImpl.class.getName()
								+ " where name = ?  ").setParameter(0,
						resourceName, Hibernate.STRING).list();
				if (lx == null || lx.size() == 0)
				{
					added++;
					SearchBuilderItem sbi = new SearchBuilderItemImpl();
					sbi.setName(resourceName);
					sbi.setSearchaction(SearchBuilderItem.ACTION_ADD);
					sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
					String context = null;
					try {
						context = ecp.getSiteId(resourceName);
					} catch ( Exception ex ) {
						log.info("No context for resource "+resourceName+" defaulting to none");
					}
					if ( context == null || context.length() == 0 ) {
						context = "none";
					}
					sbi.setContext(context);
					session.saveOrUpdate(sbi);
				}
			}
			log.debug(" Added " + added);
		}
		log
				.debug("DONE ADD ALL RECORDS ===========================================================");
		controlItem.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
		session.saveOrUpdate(controlItem);

	}

	private void refreshIndex(Session session, SearchBuilderItem controlItem)
			throws HibernateException
	{
		// delete all and return the master action only
		// the caller will then rebuild the index from scratch
		log
				.debug("UPDATE ALL RECORDS ==========================================================");
		session.flush();
		try
		{
			if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem
					.getContext()))
			{
				session.connection().createStatement().execute(
						"update searchbuilderitem set searchstate = "
								+ SearchBuilderItem.STATE_PENDING
								+ " where name not like '"
								+ SearchBuilderItem.SITE_MASTER_PATTERN
								+ "' and name <> '"
								+ SearchBuilderItem.GLOBAL_MASTER + "' ");

			}
			else
			{
				session.connection().createStatement().execute(
						"update searchbuilderitem set searchstate = "
								+ SearchBuilderItem.STATE_PENDING
								+ " where context = '"
								+ controlItem.getContext() + "' and name <> '"
								+ controlItem.getName() + "'");

			}
		}
		catch (SQLException e)
		{
			throw new HibernateException("Failed to perform delete ", e);
		}
		controlItem.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
		session.saveOrUpdate(controlItem);
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

}
