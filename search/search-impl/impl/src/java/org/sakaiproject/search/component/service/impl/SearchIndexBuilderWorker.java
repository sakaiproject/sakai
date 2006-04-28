package org.sakaiproject.search.component.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.UUID;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.SearchWriterLock;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.search.model.impl.SearchWriterLockImpl;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class SearchIndexBuilderWorker extends HibernateDaoSupport implements
		Runnable
{

	/**
	 * The lock we use to ensure single search index writer
	 */
	public static final String LOCKKEY = "searchlockkey";

	protected static final Object GLOBAL_CONTEXT = null;

	private static final String NO_NODE = "none";

	private static Log log = LogFactory.getLog(SearchIndexBuilderWorker.class);

	private final int numThreads = 10;

	/**
	 * The maximum sleep time for the wait/notify semaphore
	 */
	public long sleepTime = 5L * 60000L;

	/**
	 * The currently running index Builder thread
	 */
	private Thread indexBuilderThread[] = new Thread[numThreads];

	/**
	 * sync object
	 */
	private Object threadStartLock = new Object();

	/**
	 * dependency: the search index builder that is accepting new items
	 */
	private SearchIndexBuilderImpl searchIndexBuilder = null;

	/**
	 * dependency: the current search service, used to get the location of the
	 * index
	 */
	private SearchService searchService = null;

	private DataSource dataSource = null;

	private IdentifierGenerator idgenerator = new VersionFourGenerator();

	/**
	 * Semaphore
	 */
	private Object sem = new Object();

	/**
	 * The number of items to process in a batch, default = 100
	 */
	private int indexBatchSize = 100;

	private boolean enabled = false;

	private SessionManager sessionManager;

	private UserDirectoryService userDirectoryService;

	private EntityManager entityManager;

	private EventTrackingService eventTrackingService;


	private boolean runThreads = true;

	private ThreadLocal nodeIDHolder = new ThreadLocal();;

	private static String lockedTo = null;

	private static String SELECT_LOCK_SQL = "select id, nodename, "
			+ "lockkey, expires from searchwriterlock where lockkey = ?";

	private static String UPDATE_LOCK_SQL = "update searchwriterlock set "
			+ "nodename = ?, expires = ? where id = ? "
			+ "and nodename = ? and lockkey = ? ";

	private static String INSERT_LOCK_SQL = "insert into searchwriterlock "
			+ "( id,nodename,lockkey, expires ) values ( ?, ?, ?, ? )";

	private static String COUNT_WORK_SQL = " select count(*) "
			+ "from searchbuilderitem where searchstate = ? and searchaction <> ? ";

	private static String CLEAR_LOCK_SQL = "update searchwriterlock "
			+ "set nodename = ?, expires = ? where nodename = ? and lockkey = ? ";

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		eventTrackingService = (EventTrackingService) load(cm,
				EventTrackingService.class.getName());
		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
		userDirectoryService = (UserDirectoryService) load(cm,
				UserDirectoryService.class.getName());
		searchIndexBuilder = (SearchIndexBuilderImpl) load(cm,
				SearchIndexBuilder.class.getName());
		searchService = (SearchService) load(cm, SearchService.class.getName());

		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());

		enabled = "true".equals(ServerConfigurationService
				.getString("search.experimental"));
		try
		{
			if (searchIndexBuilder == null)
			{
				log.error("Search Index Worker needs searchIndexBuilder ");
			}
			if (searchService == null)
			{
				log.error("Search Index Worker needs searchService ");
			}
			if (getSessionFactory() == null)
			{
				log.error("Search Index Worker needs a session factory ");
			}
			log.debug("init start");
			for (int i = 0; i < indexBuilderThread.length; i++)
			{
				indexBuilderThread[i] = new Thread(this);
				indexBuilderThread[i].setName(String.valueOf(i) + "::"
						+ this.getClass().getName());
				indexBuilderThread[i].start();
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

	/**
	 * Main run target of the worker thread {@inheritDoc}
	 */
	public void run()
	{
		if (!enabled) return;
		String threadName = Thread.currentThread().getName();
		String tn = threadName.substring(0, 1);
		log.debug("Index Builder Run " + tn + "_" + threadName);
		int threadno = Integer.parseInt(tn);

		String nodeID = getNodeID();

		org.sakaiproject.component.cover.ComponentManager.waitTillConfigured();

		try
		{

			while (runThreads)
			{
				log.debug("Run Processing Thread");
				boolean locked = false;
				org.sakaiproject.tool.api.Session s = null;
				if (s == null)
				{
					s = sessionManager.startSession();
					User u = userDirectoryService.getUser("admin");
					s.setUserId(u.getId());
				}

				while (runThreads)
				{
					sessionManager.setCurrentSession(s);
					try
					{
						if (getLockTransaction())
						{

							log.info("===" + nodeID
									+ "=============PROCESSING ");
							if (lockedTo != null && lockedTo.equals(nodeID))
							{
								log
										.error("+++++++++++++++Local Lock Collision+++++++++++++");
							}
							lockedTo = nodeID;
							processToDoListTransaction();
							if (lockedTo.equals(nodeID))
							{
								lockedTo = null;
							}
							else
							{
								log
										.error("+++++++++++++++++++++++++++Lost Local Lock+++++++++++");
							}
							log
									.info("===" + nodeID
											+ "=============COMPLETED ");

						}
						else
						{
							break;
						}
					}
					finally
					{
						clearLockTransaction();
					}
				}
				if (!runThreads)
				{
					break;
				}
				try
				{
					log.debug("Sleeping Processing Thread");
					synchronized (sem)
					{
						log.debug("++++++WAITING " + nodeID);
						sem.wait(sleepTime);

						log.debug("+++++ALIVE " + nodeID);
					}
					log.debug("Wakey Wakey Processing Thread");

					if (org.sakaiproject.component.cover.ComponentManager
							.hasBeenClosed())
					{
						runThreads = false;
						break;
					}
				}
				catch (InterruptedException e)
				{
					log.debug(" Exit From sleep " + e.getMessage());
					break;
				}
			}
		}
		catch (Throwable t)
		{
			log.warn("Failed in IndexBuilder ", t);
		}
		finally
		{

			log.debug("IndexBuilder run exit " + threadName);
			indexBuilderThread[threadno] = null;
		}
	}

	private String getNodeID()
	{
		String nodeID = (String) nodeIDHolder.get();
		if (nodeID == null)
		{
			UUID uuid = (UUID) idgenerator.nextIdentifier();
			nodeID = uuid.toString();
			nodeIDHolder.set(nodeID);
		}
		return nodeID;
	}

	/**
	 * @return
	 * @throws HibernateException
	 */
	private boolean getLockTransaction()
	{
		String nodeID = getNodeID();
		Connection connection = null;
		boolean locked = false;
		boolean autoCommit = false;
		PreparedStatement selectLock = null;
		PreparedStatement updateLock = null;
		PreparedStatement insertLock = null;
		PreparedStatement countWork = null;
		ResultSet resultSet = null;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp expiryDate = new Timestamp(now.getTime()
				+ (10L * 60L * 1000L));

		try
		{

			// I need to go direct to JDBC since its just too awful to
			// try and do this in Hibernate.

			connection = dataSource.getConnection();
			autoCommit = connection.getAutoCommit();
			if (autoCommit)
			{
				connection.setAutoCommit(false);
			}

			selectLock = connection.prepareStatement(SELECT_LOCK_SQL);
			updateLock = connection.prepareStatement(UPDATE_LOCK_SQL);
			insertLock = connection.prepareStatement(INSERT_LOCK_SQL);
			countWork = connection.prepareStatement(COUNT_WORK_SQL);

			SearchWriterLock swl = null;
			selectLock.clearParameters();
			selectLock.setString(1, LOCKKEY);
			resultSet = selectLock.executeQuery();
			if (resultSet.next())
			{
				swl = new SearchWriterLockImpl();
				swl.setId(resultSet.getString(1));
				swl.setNodename(resultSet.getString(2));
				swl.setLockkey(resultSet.getString(3));
				swl.setExpires(resultSet.getTimestamp(4));
				log.debug("GOT Lock Record " + swl.getId() + "::"
						+ swl.getNodename() + "::" + swl.getExpires());

			}

			resultSet.close();
			resultSet = null;

			boolean takelock = false;
			if (swl == null)
			{
				log.debug("_-------------NO Lock Record");
				takelock = true;
			}
			else if ("none".equals(swl.getNodename()))
			{
				takelock = true;
				log.debug(nodeID + "_-------------no lock");
			}
			else if (nodeID.equals(swl.getNodename()))
			{
				takelock = true;
				log.debug(nodeID + "_------------matched threadid ");
			}
			else if (swl.getExpires() == null || swl.getExpires().before(now))
			{
				takelock = true;
				log.debug(nodeID + "_------------thread dead ");
			}

			if (takelock)
			{
				// any work ?
				countWork.clearParameters();
				countWork.setInt(1, SearchBuilderItem.STATE_PENDING.intValue());
				countWork
						.setInt(2, SearchBuilderItem.ACTION_UNKNOWN.intValue());
				resultSet = countWork.executeQuery();
				int nitems = 0;
				if (resultSet.next())
				{
					nitems = resultSet.getInt(1);
				}
				resultSet.close();
				resultSet = null;
				if (nitems > 0)
				{
					if (swl == null)
					{
						insertLock.clearParameters();
						insertLock.setString(1, nodeID);
						insertLock.setString(2, nodeID);
						insertLock.setString(3, LOCKKEY);
						insertLock.setTimestamp(4, expiryDate);

						if (insertLock.executeUpdate() == 1)
						{
							log.debug("INSERT Lock Record " + nodeID + "::"
									+ nodeID + "::" + expiryDate);

							locked = true;
						}

					}
					else
					{
						updateLock.clearParameters();
						updateLock.setString(1, nodeID);
						updateLock.setTimestamp(2, expiryDate);
						updateLock.setString(3, swl.getId());
						updateLock.setString(4, swl.getNodename());
						updateLock.setString(5, swl.getLockkey());
						if (updateLock.executeUpdate() == 1)
						{
							log.debug("UPDATED Lock Record " + swl.getId()
									+ "::" + nodeID + "::" + expiryDate);
							locked = true;
						}

					}

				}

			}
			connection.commit();

		}
		catch (Exception ex)
		{
			if (connection != null)
			{
				try
				{
					connection.rollback();
				}
				catch (SQLException e)
				{
				}
			}
			log.error("Failed to get lock " + ex.getMessage());
			locked = false;
		}
		finally
		{
			if (resultSet != null)
			{
				try
				{
					resultSet.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (selectLock != null)
			{
				try
				{
					selectLock.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (updateLock != null)
			{
				try
				{
					updateLock.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (insertLock != null)
			{
				try
				{
					insertLock.close();
				}
				catch (SQLException e)
				{
				}
			}
			if (countWork != null)
			{
				try
				{
					countWork.close();
				}
				catch (SQLException e)
				{
				}
			}

			if (connection != null)
			{
				try
				{
					connection.setAutoCommit(autoCommit);
				}
				catch (SQLException e)
				{
				}
				try
				{
					connection.close();
					log.debug("Connection Closed ");
				}
				catch (SQLException e)
				{
					log.error("Error Closing Connection ", e);
				}
				connection = null;
			}
		}
		return locked;

	}

	/**
	 * Count the number of pending documents waiting to be indexed on this
	 * cluster node. All nodes will potentially perform the index in a cluster,
	 * however only one must be doing the index, hence this method attampts to
	 * grab a lock on the writer. If sucessfull it then gets the real number of
	 * pending documents. There is a timeout, such that if the witer has not
	 * been seen for 10 minutes, it is assumed that something has gon wrong with
	 * it, and a new writer is elected on a first grab basis. Every time the
	 * elected writer comes back, it updates its record to say its still active.
	 * We could do some round robin timeout, or allow deployers to select a pool
	 * of index writers in Sakai properties. {@inheritDoc}
	 */

	private void clearLockTransaction()
	{
		String nodeID = getNodeID();

		Connection connection = null;
		PreparedStatement clearLock = null;
		try
		{
			connection = dataSource.getConnection();

			clearLock = connection.prepareStatement(CLEAR_LOCK_SQL);
			clearLock.clearParameters();
			clearLock.setString(1, NO_NODE);
			clearLock
					.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			clearLock.setString(3, nodeID);
			clearLock.setString(4, LOCKKEY);
			if (clearLock.executeUpdate() == 1)
			{
				log.info("UNLOCK - OK    ?::" + nodeID + "::now");

			}
			else
			{
				log.debug("UNLOCK - Failed?::" + nodeID + "::now");
			}
			connection.commit();

		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (SQLException e)
			{
			}
			log.error("Failed to clear lock" + ex.getMessage());
		}
		finally
		{
			if (connection != null)
			{
				try
				{
					connection.close();
					log.debug("Connection Closed");
				}
				catch (SQLException e)
				{
					log.error("Error Closing Connection", e);
				}
			}
		}

	}

	/**
	 * This method processes the list of document modifications in the list
	 * 
	 * @param runtimeToDo
	 * @throws IOException
	 * @throws HibernateException
	 */
	protected void processToDoListTransaction()
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
					String indexDirectory = ((SearchServiceImpl) searchService)
							.getIndexDirectory();
					log.debug("Starting process List on " + indexDirectory);
					File f = new File(indexDirectory);
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
							if (IndexReader.indexExists(indexDirectory))
							{
								IndexReader indexReader = null;
								try
								{
									indexReader = IndexReader
											.open(indexDirectory);

									// Open the index
									for (Iterator tditer = runtimeToDo
											.iterator(); runThreads
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
								// open for update
								if (runThreads)
								{
									indexWrite = new IndexWriter(
											indexDirectory,
											new StandardAnalyzer(), false);
								}
							}
							else if (runThreads)
							{
								// create for update
								indexWrite = new IndexWriter(indexDirectory,
										new StandardAnalyzer(), true);
							}
							for (Iterator tditer = runtimeToDo.iterator(); runThreads
									&& tditer.hasNext();)
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
								Reference ref = entityManager.newReference(sbi
										.getName());

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
								doc.add(Field.Keyword("container", container));
								doc.add(Field.UnIndexed("id", ref.getId()));
								doc.add(Field.Keyword("type", ref.getType()));
								doc.add(Field.Keyword("subtype", ref
										.getSubType()));
								doc.add(Field.Keyword("reference", ref
										.getReference()));
								Collection c = ref.getRealms();
								for (Iterator ic = c.iterator(); ic.hasNext();)
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
										doc.add(Field.Keyword("context", sep
												.getSiteId(ref)));
										if (sep.isContentFromReader(entity))
										{
											doc.add(Field.Text("contents", sep
													.getContentReader(entity),
													true));
										}
										else
										{
											doc.add(Field.Text("contents", sep
													.getContent(entity), true));
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
										doc.add(Field.Keyword("context", ref
												.getContext()));
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

						for (Iterator tditer = runtimeToDo.iterator(); runThreads
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
					}
					return new Integer(totalDocs);
				}
				catch (IOException ex)
				{
					throw new HibernateException(" Failed to create index ", ex);
				}
			}

		};
		int totalDocs = 
		((Integer)getHibernateTemplate().execute(callback)).intValue();

		if (runThreads)
		{

			eventTrackingService.post(eventTrackingService.newEvent(
					SearchService.EVENT_TRIGGER_INDEX_RELOAD,
					"/searchindexreload", true,
					NotificationService.PREF_IMMEDIATE));
		}
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

				return refreshIndex(session, masterItem, batchSize);

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
						return refreshIndex(session, siteMaster, batchSize);
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
						"delete from searchbuilderitem  ");
			}
			else
			{
				session.connection().createStatement().execute(
						"delete from searchbuilderitem where context = '"
								+ controlItem.getContext() + "' ");

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
				List lx = session.find(" from "
						+ SearchBuilderItemImpl.class.getName()
						+ " where name = ?  ", resourceName, Hibernate.STRING);
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
	}

	private List refreshIndex(Session session, SearchBuilderItem controlItem,
			int batchSize) throws HibernateException
	{
		log.debug("Refresh Index with " + session);

		List l = null;
		if (SearchBuilderItem.GLOBAL_CONTEXT.equals(controlItem.getContext()))
		{
			l = session.createCriteria(SearchBuilderItemImpl.class).add(
					Expression.lt("version", controlItem.getVersion()))
					.setMaxResults(batchSize).list();
		}
		else
		{
			l = session.createCriteria(SearchBuilderItemImpl.class).add(
					Expression.lt("version", controlItem.getVersion())).add(
					Expression.eq("context", controlItem.getContext()))
					.setMaxResults(batchSize).list();

		}
		if (l == null || l.size() == 0)
		{
			controlItem.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
			session.saveOrUpdate(controlItem);
		}
		log
				.debug("RESET NEXT 100 RECORDS ===========================================================");

		for (Iterator i = l.iterator(); i.hasNext();)
		{
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
		}
		log
				.debug("DONE RESET NEXT 100 RECORDS ===========================================================");

		return l;
	}

	/**
	 * Check running, and ping the thread if in a wait state
	 */
	public void checkRunning()
	{
		if (!enabled) return;
		runThreads = true;
		synchronized (threadStartLock)
		{
			for (int i = 0; i < indexBuilderThread.length; i++)
			{
				if (indexBuilderThread[i] == null)
				{
					indexBuilderThread[i] = new Thread(this);
					indexBuilderThread[i].setName(String.valueOf(i) + "::"
							+ this.getClass().getName());
					indexBuilderThread[i].start();
				}
			}
		}
		synchronized (sem)
		{
			log.debug("_________NOTIFY");
			sem.notify();
			log.debug("_________NOTIFY COMPLETE");
		}

	}

	public void destroy()
	{
		if (!enabled) return;

		log.debug("Destroy SearchIndexBuilderWorker ");
		runThreads = false;

		synchronized (sem)
		{
			sem.notifyAll();
		}
	}

	/**
	 * @return Returns the sleepTime.
	 */
	public long getSleepTime()
	{
		return sleepTime;
	}

	/**
	 * @param sleepTime
	 *        The sleepTime to set.
	 */
	public void setSleepTime(long sleepTime)
	{
		this.sleepTime = sleepTime;
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

}
