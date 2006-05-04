package org.sakaiproject.search.component.dao.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
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
import org.sakaiproject.search.dao.SearchIndexBuilderWorkerDao;
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
	 * dependency: the current search service, used to get the location of the
	 * index
	 */

	private String searchIndexDirectory = null;

	/**
	 * The number of items to process in a batch, default = 100
	 */
	private int indexBatchSize = 100;

	private boolean enabled = false;

	private EntityManager entityManager;

	private EventTrackingService eventTrackingService;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		eventTrackingService = (EventTrackingService) load(cm,
				EventTrackingService.class.getName());
		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName());

		enabled = "true".equals(ServerConfigurationService.getString(
				"search.experimental", "true"));
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
		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
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
					log.debug("Starting process List on "
							+ searchIndexDirectory);
					File f = new File(searchIndexDirectory);
					if (!f.exists())
					{
						f.mkdirs();
						log.debug("Indexing in " + f.getAbsolutePath());
					}

					IndexWriter indexWrite = null;

					// Load the list

					List runtimeToDo = findPending(indexBatchSize, session);
					log
							.debug("Processing " + runtimeToDo.size()
									+ " documents");
					totalDocs = runtimeToDo.size();

					if (totalDocs > 0)
					{
						try
						{
							if (IndexReader.isLocked(searchIndexDirectory))
							{
								// this could be dangerous, I am assuming that
								// the locking mechanism implemented here is
								// robust and
								// already prevents multiple modifiers.
								// A more

								IndexReader.unlock(FSDirectory.getDirectory(
										searchIndexDirectory, true));
								log
										.warn("Unlocked Lucene Directory for update, hope this is Ok");
							}
							if (IndexReader.indexExists(searchIndexDirectory))
							{
								IndexReader indexReader = null;
								try
								{
									indexReader = IndexReader
											.open(searchIndexDirectory);

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
													.delete(new Term(
															"reference", sbi
																	.getName()));
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
									indexWrite = new IndexWriter(
											searchIndexDirectory,
											new StandardAnalyzer(), false);
								}
							}
							else
							{
								// create for update
								if (worker.isRunning())
								{
									indexWrite = new IndexWriter(
											searchIndexDirectory,
											new StandardAnalyzer(), true);
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
									Entity entity = ref.getEntity();

									Document doc = new Document();
									if (ref.getContext() == null)
									{
										log.warn("Context is null for " + sbi);
									}
									String container = ref.getContainer();
									if (container == null) container = "";
									doc.add(Field.Keyword("container",
											container));
									doc.add(Field.UnIndexed("id", ref.getId()));
									doc.add(Field
											.Keyword("type", ref.getType()));
									doc.add(Field.Keyword("subtype", ref
											.getSubType()));
									doc.add(Field.Keyword("reference", ref
											.getReference()));
									Collection c = ref.getRealms();
									for (Iterator ic = c.iterator(); ic
											.hasNext();)
									{
										String realm = (String) ic.next();
										doc.add(Field.Keyword("realm", realm));
									}
									try
									{
										EntityContentProducer sep = searchIndexBuilder
												.newEntityContentProducer(ref);
										if (sep != null)
										{
											doc.add(Field.Keyword("context",
													sep.getSiteId(ref)));
											if (sep.isContentFromReader(entity))
											{
												contentReader = sep
														.getContentReader(entity);
												doc.add(Field.Text("contents",
														contentReader, true));
											}
											else
											{
												doc.add(Field.Text("contents",
														sep.getContent(entity),
														true));
											}
											doc.add(Field.Text("title", sep
													.getTitle(entity), true));
											doc.add(Field.Keyword("tool", sep
													.getTool()));
											doc.add(Field.Keyword("url", sep
													.getUrl(entity)));
											doc.add(Field.Keyword("siteid", sep
													.getSiteId(ref)));

										}
										else
										{
											doc.add(Field.Keyword("context",
													ref.getContext()));
											doc.add(Field.Text("title", ref
													.getReference(), true));
											doc.add(Field.Keyword("tool", ref
													.getType()));
											doc.add(Field.Keyword("url", ref
													.getUrl()));
											doc.add(Field.Keyword("siteid", ref
													.getContext()));
										}
									}
									catch (Exception e1)
									{
										e1.printStackTrace();
									}

									log.debug("Indexing Document " + doc);
									indexWrite.addDocument(doc);
									sbi
											.setSearchstate(SearchBuilderItem.STATE_COMPLETED);

									log.debug("Done Indexing Document " + doc);
									// update this node lock to indicate its
									// still alove, no document should
									// take more than 2 mins to process
									worker.updateNodeLock(null);
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

						for (Iterator tditer = runtimeToDo.iterator(); worker
								.isRunning()
								&& tditer.hasNext();)
						{
							SearchBuilderItem sbi = (SearchBuilderItem) tditer
									.next();
							if (SearchBuilderItem.STATE_COMPLETED.equals(sbi
									.getSearchstate()))
							{
								if (SearchBuilderItem.ACTION_DELETE.equals(sbi
										.getSearchaction()))
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
			totalDocs = ((Integer) getHibernateTemplate().execute(callback))
					.intValue();
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
					sbi.setContext(ecp.getSiteId(resourceName));
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
	 * @return Returns the searchIndexDirectory.
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	/**
	 * @param searchIndexDirectory
	 *        The searchIndexDirectory to set.
	 */
	public void setSearchIndexDirectory(String searchIndexDirectory)
	{
		this.searchIndexDirectory = searchIndexDirectory;
	}

}
