package org.sakaiproject.search.component.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.search.EntityContentProducer;
import org.sakaiproject.search.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.SearchWriterLock;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;
import org.sakaiproject.search.model.impl.SearchWriterLockImpl;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.event.cover.EventTrackingService;
import org.sakaiproject.service.legacy.id.cover.IdService;
import org.sakaiproject.service.legacy.notification.cover.NotificationService;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class SearchIndexBuilderWorker extends HibernateDaoSupport implements
		Runnable {

	/**
	 * The lock we use to ensure single search index writer
	 */
	public static final String LOCKKEY = "searchlockkey";

	private static Log dlog = LogFactory.getLog(SearchIndexBuilderWorker.class);

	private final int numThreads = 1;

	/**
	 * The maximum sleep time for the wait/notify semaphore
	 */
	public long sleepTime = 5L * 60000L;

	/**
	 * The currently running index Builder thread
	 */
	private Thread indexBuilderThread[] = new Thread[numThreads];

	/**
	 * dependency: standard logger
	 */
	private Logger logger = null;

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

	/**
	 * Semaphore
	 */
	private Object sem = new Object();

	/**
	 * The number of items to process in a batch, default = 100
	 */
	private int indexBatchSize = 100;

	private boolean enabled = false;
	public void init() {
		
		enabled = "true".equals(ServerConfigurationService
				.getString("wiki.experimental"));
		try {
			if (searchIndexBuilder == null) {
				dlog.error("Search Index Worker needs searchIndexBuilder ");
			}
			if (searchService == null) {
				dlog.error("Search Index Worker needs searchService ");
			}
			if (getSessionFactory() == null) {
				dlog.error("Search Index Worker needs a session factory ");
			}
			dlog.debug("init start");
			for (int i = 0; i < indexBuilderThread.length; i++) {
				indexBuilderThread[i] = new Thread(this);
				indexBuilderThread[i].setName(String.valueOf(i) + "::"
						+ this.getClass().getName());
				indexBuilderThread[i].start();
			}
		} catch (Throwable t) {
			dlog.error("Failed to init ", t);
		}
	}

	/**
	 * Main run target of the worker thread {@inheritDoc}
	 */
    public void run() {
    		if ( !enabled ) return;
		String threadName = Thread.currentThread().getName();
		String tn = threadName.substring(0, 1);
		dlog.debug("Index Builder Run " + tn + "_" + threadName);
		int threadno = Integer.parseInt(tn);
		String threadID = getThreadID();

		try {

			while (runThreads) {
				dlog.debug("Run Processing Thread");
				Session hsession = null;
				org.sakaiproject.api.kernel.session.Session s = null;
				try {
					while (runThreads) {
						try {
							SessionManager.setCurrentSession(s);
							hsession = getSession();
							if (s == null) {
								s = SessionManager.startSession();
								User u = UserDirectoryService.getUser("admin");
								s.setUserId(u.getId());
							}
							SessionManager.setCurrentSession(s);
							// process the list
							int pending = countPending(true);
							if (pending == 0) {
								hsession.flush();
								hsession.close();
								break;
							}
							processToDoList();
							clearLock();
							hsession.flush();
							hsession.close();
							hsession = null;

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} finally {
					try {
						if (hsession != null) {
							// make certain to bin the lock
							hsession = getSession();
							clearLock();
							hsession.flush();
							hsession.close();
						}
					} catch (Exception ex) {

					}
				}
				if ( ! runThreads ) {
					break;
				}
				try {
					dlog.debug("Sleeping Processing Thread");
					synchronized (sem) {
						dlog.debug("++++++WAITING " + threadID);
						sem.wait(sleepTime);
						
						
						
						
						dlog.debug("+++++ALIVE " + threadID);
					}
					dlog.debug("Wakey Wakey Processing Thread");
				} catch (InterruptedException e) {
					dlog.debug(" Exit From sleep " + e.getMessage());
					break;
				}
			}
		} catch (Throwable t) {
			dlog.warn("Failed in IndexBuilder ", t);
		} finally {

			dlog.debug("IndexBuilder run exit " + threadName);
			indexBuilderThread[threadno] = null;
		}
	}
	/**
	 * This method processes the list of document modifications in the list
	 * 
	 * @param runtimeToDo
	 * @throws IOException
	 */
	protected void processToDoList() throws IOException {
		txstartTransaction();
		long startTime = System.currentTimeMillis();
		int totalDocs = 0;
		try {
			String indexDirectory = ((SearchServiceImpl) searchService)
					.getIndexDirectory();
			dlog.debug("Starting process List on " + indexDirectory);
			File f = new File(indexDirectory);
			if (!f.exists()) {
				f.mkdirs();
				dlog.debug("Indexing in " + f.getAbsolutePath());
			}

			IndexWriter indexWrite = null;

			// Load the list

			List runtimeToDo = txfindPending(indexBatchSize);
			dlog.debug("Processing " + runtimeToDo.size() + " documents");
			totalDocs = runtimeToDo.size();

			if (totalDocs > 0) {
				if (IndexReader.indexExists(indexDirectory)) {
					IndexReader indexReader = IndexReader.open(indexDirectory);

					// Open the index
					for (Iterator tditer = runtimeToDo.iterator(); runThreads && tditer
							.hasNext();) {
						SearchBuilderItem sbi = (SearchBuilderItem) tditer
								.next();
						if (!SearchBuilderItem.STATE_PENDING.equals(sbi
								.getSearchstate())) {
							// should only be getting pending items
							dlog.warn(" Found Item that was not pending "
									+ sbi.getName());
							continue;
						}
						if (SearchBuilderItem.ACTION_UNKNOWN.equals(sbi
								.getSearchaction())) {
							sbi
									.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
							continue;
						}
						// remove document
						try {
							indexReader.delete(new Term("reference", sbi
									.getName()));
							if (SearchBuilderItem.ACTION_DELETE.equals(sbi
									.getSearchaction())) {
								sbi
										.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
							} else {
								sbi
										.setSearchstate(SearchBuilderItem.STATE_PENDING_2);
							}

						} catch (IOException ex) {
							dlog.warn("Failed to delete Page ", ex);
						}
					}
					indexReader.close();
					// open for update
					if ( runThreads ) {
					indexWrite = new IndexWriter(indexDirectory,
							new StandardAnalyzer(), false);
					}
				} else if ( runThreads ){
					// create for update
					indexWrite = new IndexWriter(indexDirectory,
							new StandardAnalyzer(), true);
				}
				for (Iterator tditer = runtimeToDo.iterator(); runThreads && tditer.hasNext();) {
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					// only add adds, that have been deleted sucessfully
					if (!SearchBuilderItem.STATE_PENDING_2.equals(sbi
							.getSearchstate())) {
						continue;
					}
					Reference ref = EntityManager.newReference(sbi.getName());

					if (ref == null) {
						dlog
								.error("Unrecognised trigger object presented to index builder "
										+ sbi);
					}
					Entity entity = ref.getEntity();
	
					Document doc = new Document();
					if (ref.getContext() == null) {
						dlog.warn("Context is null for " + sbi);
					}
					doc.add(Field.Keyword("context", ref.getContext()));
					String container = ref.getContainer();
					if ( container == null ) container = ""; 
					doc.add(Field.Keyword("container", container));
					doc.add(Field.UnIndexed("id", ref.getId()));
					doc.add(Field.Keyword("type", ref.getType()));
					doc.add(Field.Keyword("subtype", ref.getSubType()));
					doc.add(Field.Keyword("reference", ref.getReference()));
					Collection c = ref.getRealms();
					for (Iterator ic = c.iterator(); ic.hasNext();) {
						String realm = (String) ic.next();
						doc.add(Field.Keyword("realm", realm));
					}
					try {
						EntityContentProducer sep = searchIndexBuilder
								.newEntityContentProducer(ref);
						if (sep != null) {
							if (sep.isContentFromReader(entity)) {
								doc.add(Field.Text("contents", sep
										.getContentReader(entity), true));
							} else {
								doc.add(Field.Text("contents", sep
										.getContent(entity), true));
							}
							doc.add(Field.Text("title", sep.getTitle(entity),
									true));
							doc.add(Field.Keyword("tool", sep.getTool()));
							doc.add(Field.Keyword("url", sep.getUrl(entity)));
							doc.add(Field.Keyword("siteid", sep.getSiteId(ref)));

						} else {
							doc.add(Field.Text("title", ref.getReference(),
									true));
							doc.add(Field.Keyword("tool", ref.getType()));
							doc.add(Field.Keyword("url", ref.getUrl()));
							doc.add(Field.Keyword("siteid", ref.getContext()));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					dlog.debug("Indexing Document " + doc);
					indexWrite.addDocument(doc);
					sbi.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
					
					dlog.debug("Done Indexing Document " + doc);

				}
				if ( indexWrite != null ) {
					indexWrite.close();
				}

				for (Iterator tditer = runtimeToDo.iterator(); runThreads && tditer.hasNext();) {
					SearchBuilderItem sbi = (SearchBuilderItem) tditer.next();
					try {
						if (SearchBuilderItem.STATE_COMPLETED.equals(sbi
								.getSearchstate())) {
							if (SearchBuilderItem.ACTION_DELETE.equals(sbi
									.getSearchaction())) {
								txdelete(sbi);
							} else {
								txsave(sbi);
							}
						}
					} catch (Exception ex) {
						dlog.warn("Concurrent modification, will reindex "
								+ sbi.getName());
					}
				}
			}
			txcommitTransaction();
		} catch (Exception ex) {
			dlog.warn("Failed to Process Docs ", ex);
			txrollbackTrasaction();
			throw new RuntimeException("Failed to save State ", ex);
		}
		
		if ( runThreads ) {

		EventTrackingService.post(EventTrackingService.newEvent(
				SearchService.EVENT_TRIGGER_INDEX_RELOAD, "/searchindexreload",
				true, NotificationService.PREF_IMMEDIATE));
		}
		long endTime = System.currentTimeMillis();
		float totalTime = endTime - startTime;
		float ndocs = totalDocs;
		if (totalDocs > 0) {
			float docspersec = 1000 * ndocs / totalTime;
			dlog.info("Completed Process List of " + totalDocs + " at "
					+ docspersec + " documents/per second");
		}

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

	private ThreadLocal writerThreadIDHolder = new ThreadLocal();

	private String getThreadID() {
		String wtid = (String) writerThreadIDHolder.get();
		if (wtid == null) {
			wtid = IdService.getUniqueId();
			writerThreadIDHolder.set(wtid);
		}
		return wtid;
	}

	private void clearLock() {
		final String writerThreadID = getThreadID();

		HibernateCallback callback = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException {

				Transaction t = session.beginTransaction();
				try {
					List lockRecord = session.createCriteria(
							SearchWriterLockImpl.class).add(
							Expression.eq("lockkey", LOCKKEY)).list();
					if (lockRecord.size() == 0) {
						// no record, create one and release
						SearchWriterLock swl = new SearchWriterLockImpl();
						swl.setLockkey(LOCKKEY);
						swl.setNodename("none");
						session.saveOrUpdate(swl);
					} else {
						// there is a record
						Date threadDeathDate = new Date();
						threadDeathDate = new Date(threadDeathDate.getTime()
								- (10L * 60L * 1000L));
						SearchWriterLock swl = (SearchWriterLock) lockRecord
								.get(0);
						// Lock is granted to this node, clear it
						if (writerThreadID.equals(swl.getNodename())) {
							swl.setNodename("none");
							session.saveOrUpdate(swl);
						}
					}
					t.commit();
				} catch (Exception ex) {
					t.rollback();
					// failed to get lock , due to an optimistic locking
					// failiure
					return new Integer(0);
				}

				return null;
			}
		};
		getHibernateTemplate().execute(callback);
		return;
	}

	/**
	 * Count the number of pending items
	 * 
	 * @param withLock
	 *            if true an attempt to lock the thread as an IndexWriter will
	 *            be made. If th lock is sucessfull, the number of pending items
	 *            will be returned, if not 0 will be returned. If the withLock
	 *            is false, then no lock will be made and the number will be
	 *            returned
	 * @return
	 */
	private int countPending(final boolean withLock) {
		final String writerThreadID = getThreadID();

		HibernateCallback callback = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException {
				// first try and get and lock the writer mutex
				if (withLock) {

					Transaction t = session.beginTransaction();
					try {
						List lockRecord = session.createCriteria(
								SearchWriterLockImpl.class).add(
								Expression.eq("lockkey", LOCKKEY)).setLockMode(
								LockMode.UPGRADE).list();
						SearchWriterLock swl = null;
						if (lockRecord.size() == 0) {
							// no record, create one and grab it
							swl = new SearchWriterLockImpl();
							swl.setVersion(new Date());
						} else {
							// there is a record
							Date threadDeathDate = new Date();
							threadDeathDate = new Date(threadDeathDate
									.getTime()
									- (10L * 60L * 1000L));
							swl = (SearchWriterLock) lockRecord.get(0);
							if (swl.getVersion() == null) {
								swl.setVersion(new Date());
							}
							dlog.debug(" Lock Object " + swl.getId() + ":"
									+ swl.getLockkey() + ":"
									+ swl.getNodename() + ":"
									+ swl.getVersion());
							// if not this node and still active, dont grab
							// the none allows a node to explicitly release the
							// lock
							// giving the node with the lowest CPU load the best
							// chance of
							// getting the lock
							boolean takelock = false;
							if ("none".equals(swl.getNodename())) {
								takelock = true;
								dlog.debug(" no lock");
							} else if (writerThreadID.equals(swl.getNodename())) {
								takelock = true;
								dlog.debug(" matched threadid ");
							} else if (swl.getVersion().before(threadDeathDate)) {
								takelock = true;
								dlog.debug(" thread dead ");
							}
							if (!takelock) {
								dlog.debug("No Lock, Other Thread "
										+ swl.getNodename());
								return new Integer(0);
							}
						}

						// if we managed to get and lock the writer mutex, get
						// the
						// number
						// check the master record
						if (!SearchBuilderItem.STATE_UNKNOWN
								.equals(getMasterAction())) {
							swl.setLockkey(LOCKKEY);
							swl.setNodename(writerThreadID);
							session.saveOrUpdate(swl);
							dlog.debug(" Saved Master " + swl.getId() + ":"
									+ swl.getLockkey() + ":"
									+ swl.getNodename() + ":"
									+ swl.getVersion());
							t.commit();
							return new Integer(-1);
						}

						List l = session
								.find(
										"select count(*) from "
												+ SearchBuilderItemImpl.class
														.getName()
												+ " where searchstate = ? and searchaction <> ?",
										new Object[] {
												SearchBuilderItem.STATE_PENDING,
												SearchBuilderItem.ACTION_UNKNOWN },
										new Type[] { Hibernate.INTEGER,
												Hibernate.INTEGER });
						if (l == null || l.size() == 0
								|| ((Integer) l.get(0)).intValue() == 0) {
							dlog
									.debug("GOT NONE release from "
											+ writerThreadID);
							swl.setLockkey(LOCKKEY);
							swl.setNodename("none");
							session.saveOrUpdate(swl);
							dlog.debug(" Saved " + swl.getId() + ":"
									+ swl.getLockkey() + ":"
									+ swl.getNodename() + ":"
									+ swl.getVersion());
							t.commit();
							return new Integer(0);
						} else {
							dlog.debug("GOT " + l.get(0) + " Locking to  "
									+ writerThreadID + " in " + session);
							swl.setLockkey(LOCKKEY);
							swl.setNodename(writerThreadID);
							session.saveOrUpdate(swl);
							dlog.debug(" Saved " + swl.getId() + ":"
									+ swl.getLockkey() + ":"
									+ swl.getNodename() + ":"
									+ swl.getVersion());
							t.commit();
							return l.get(0);
						}

					} catch (Exception ex) {
						t.rollback();
						// failed to get lock , due to an optimistic locking
						// failiure
						dlog.debug("No Lock, Exception ", ex);
						return new Integer(0);
					}
				} else {
					if (!SearchBuilderItem.STATE_UNKNOWN
							.equals(getMasterAction())) {
						return new Integer(-1);
					}

					List l = session
							.find(
									"select count(*) from "
											+ SearchBuilderItemImpl.class
													.getName()
											+ " where searchstate = ? and searchaction <> ?",
									new Object[] {
											SearchBuilderItem.STATE_PENDING,
											SearchBuilderItem.ACTION_UNKNOWN },
									new Type[] { Hibernate.INTEGER,
											Hibernate.INTEGER });
					if (l == null || l.size() == 0) {
						return new Integer(0);
					} else {
						dlog.debug("GOT " + l.get(0) + " No Locking ");
						return l.get(0);
					}

				}

			}
		};

		return ((Integer) getHibernateTemplate().execute(callback)).intValue();
	}

	private SearchBuilderItem getMasterItem() {
		HibernateCallback callback = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException {
				List master = (List) session.createCriteria(
						SearchBuilderItemImpl.class).add(
						Expression.eq("name", SearchBuilderItem.INDEX_MASTER)).list();
				if (master != null && master.size() != 0) {
					return (SearchBuilderItem) master.get(0);
				}
				SearchBuilderItem sbi = new SearchBuilderItemImpl();
				sbi.setName(SearchBuilderItem.INDEX_MASTER);
				sbi.setVersion(new Date());
				sbi.setSearchaction(SearchBuilderItem.ACTION_UNKNOWN);
				sbi.setSearchstate(SearchBuilderItem.STATE_UNKNOWN);
				return sbi;
			}
		};
		return (SearchBuilderItem) getHibernateTemplate().execute(callback);
	}

	private Integer getMasterAction() {
		return getMasterAction(getMasterItem());
	}

	private Integer getMasterAction(SearchBuilderItem master) {
		if (SearchBuilderItem.INDEX_MASTER.equals(master.getName())) {
			if (SearchBuilderItem.STATE_PENDING.equals(master.getSearchstate())) {
				return master.getSearchaction();
			}
		}
		return SearchBuilderItem.STATE_UNKNOWN;
	}

	/**
	 * Contains the manual transaction management for free running threads
	 */
	private ThreadLocal txholder = new ThreadLocal();

	private boolean runThreads = true;

	/**
	 * start a transaction and store the transaction on the thread
	 * 
	 */
	private void txstartTransaction() {
		try {
			Transaction thisTransaction = (Transaction) txholder.get();
			if (thisTransaction == null) {
				thisTransaction = getSession().beginTransaction();
				txholder.set(thisTransaction);
			}

		} catch (HibernateException e) {
			throw new RuntimeException("Failed to start Transaction ", e);
		}
	}

	private void txsave(final SearchBuilderItem sbi) {
		HibernateCallback callback = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException {
				session.saveOrUpdate(sbi);
				return null;
			}
		};
		getHibernateTemplate().execute(callback);
	}

	private void txdelete(final SearchBuilderItem sbi) {
		HibernateCallback callback = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException {
				session.delete(sbi);
				return null;
			}
		};
		getHibernateTemplate().execute(callback);
	}

	/**
	 * Use the transaction on the thread and commit
	 * 
	 */
	private void txcommitTransaction() {
		try {
			Transaction thisTransaction = (Transaction) txholder.get();
			if (thisTransaction != null) {
				thisTransaction.commit();
				txholder.set(null);
			}
		} catch (HibernateException e) {
			throw new RuntimeException("Failed to Commit Data ", e);
		}
	}

	/**
	 * use the transaction on the thread and rollback
	 * 
	 */
	private void txrollbackTrasaction() {
		try {
			Transaction thisTransaction = (Transaction) txholder.get();
			if (thisTransaction != null) {
				thisTransaction.rollback();
				txholder.set(null);
			}
		} catch (HibernateException e) {
			throw new RuntimeException("Failed to Commit Data ", e);
		}

	}

	/**
	 * get the next x pending items If there is a master record with index
	 * refresh, the list will come back with only items existing before the
	 * index refresh was requested, those requested after the index refresh will
	 * be processed once the refresh has been completed.
	 * 
	 * If a rebuild is request, then the index queue will be deleted, and all
	 * the entitiescontentproviders polled to get all their entities
	 * 
	 * @return
	 */
	private List txfindPending(final int batchSize) {
		// Pending is the first 100 items
		// State == PENDING
		// Action != Unknown
		long start = System.currentTimeMillis();
		try {
			HibernateCallback callback = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException {
					SearchBuilderItem masterItem = getMasterItem();
					Integer masterAction = getMasterAction(masterItem);
					dlog.debug(" Master Item is "+masterItem.getName()+":"+masterItem.getSearchaction()+":"+masterItem.getSearchstate()+"::"+masterItem.getVersion());
					if (SearchBuilderItem.ACTION_REFRESH.equals(masterAction)) {
						dlog.debug(" Master Action is "+masterAction);
						dlog.debug("  REFRESH = "+SearchBuilderItem.ACTION_REFRESH);
						dlog.debug("  RELOAD = "+SearchBuilderItem.ACTION_REBUILD);
						// get a complete list of all items, before the master
						// action version
						// if there are none, update the master action action to
						// completed
						// and return a blank list
						List l = session.createCriteria(
								SearchBuilderItemImpl.class).add(
								Expression.lt("version", masterItem
										.getVersion()))
								.setMaxResults(batchSize).list();
						if (l == null || l.size() == 0) {
							masterItem
									.setSearchstate(SearchBuilderItem.STATE_COMPLETED);
							session.saveOrUpdate(masterItem);
						}
						dlog.debug("RESET NEXT 100 RECORDS ===========================================================");

						for (Iterator i = l.iterator(); i.hasNext();) {
							SearchBuilderItem sbi = (SearchBuilderItem) i
									.next();
							sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
						}
						dlog.debug("DONE RESET NEXT 100 RECORDS ===========================================================");

						return l;
					} else if (SearchBuilderItem.ACTION_REBUILD
							.equals(masterAction)) {
						// delete all and return the master action only
						// the caller will then rebuild the index from scratch
						dlog.debug("DELETE ALL RECORDS ==========================================================");
						session.flush();
						try {
							session.connection().createStatement().execute("delete from searchbuilderitem");
						} catch (SQLException e) {
							throw new HibernateException("Failed to perform delete ",e);
						}
						
						// THIS DOES NOT WORK IN H 2.1 session.delete("from "+SearchBuilderItemImpl.class.getName());
						dlog.debug("DONE DELETE ALL RECORDS ===========================================================");
						dlog.debug("ADD ALL RECORDS ===========================================================");
						for (Iterator i = searchIndexBuilder.getContentProducers().iterator(); i.hasNext();) {
							EntityContentProducer ecp = (EntityContentProducer) i
									.next();
							List contentList = ecp.getAllContent();
							int added = 0;
							for (Iterator ci = contentList.iterator(); ci
									.hasNext();) {
								String resourceName = (String) ci.next();
								List lx = session.find(" from "+SearchBuilderItemImpl.class.getName()+" where name = ? ",resourceName,Hibernate.STRING);
								if ( lx == null || lx.size() == 0 ) {
									added++;
									SearchBuilderItem sbi = new SearchBuilderItemImpl();								
									sbi.setName(resourceName);
									sbi.setSearchaction(SearchBuilderItem.ACTION_ADD);
									sbi.setSearchstate(SearchBuilderItem.STATE_PENDING);
									sbi.setVersion(new Date());
									session.saveOrUpdate(sbi);
								}
							}
							dlog.debug(" Added "+added);
						}
						dlog.debug("DONE ADD ALL RECORDS ===========================================================");

						// return normal first set
						return session.createCriteria(
								SearchBuilderItemImpl.class).add(
								Expression.eq("searchstate",
										SearchBuilderItem.STATE_PENDING)).add(
								Expression.not(Expression.eq("searchaction",
										SearchBuilderItem.ACTION_UNKNOWN))).add(
												Expression.not(Expression.eq("name",SearchBuilderItem.INDEX_MASTER)))
								.addOrder(Order.asc("version")).setMaxResults(
										batchSize).list();
					} else {
						return session.createCriteria(
								SearchBuilderItemImpl.class).add(
								Expression.eq("searchstate",
										SearchBuilderItem.STATE_PENDING)).add(
								Expression.not(Expression.eq("searchaction",
										SearchBuilderItem.ACTION_UNKNOWN))).add(
												Expression.not(Expression.eq("name",SearchBuilderItem.INDEX_MASTER))) 
								.addOrder(Order.asc("version")).setMaxResults(
										batchSize).list();
					}
				}
			};
			List l = (List) getHibernateTemplate().execute(callback);
			return l;

		} finally {
			long finish = System.currentTimeMillis();
			dlog.debug(" txfindPending took " + (finish - start) + " ms");
		}
	}

	/**
	 * Check running, and ping the thread if in a wait state
	 * 
	 */
	public void checkRunning() {
		if ( !enabled ) return;
		runThreads = true;
		synchronized (threadStartLock) {
			for (int i = 0; i < indexBuilderThread.length; i++) {
				if (indexBuilderThread[i] == null) {
					indexBuilderThread[i] = new Thread(this);
					indexBuilderThread[i].setName(String.valueOf(i) + "::"
							+ this.getClass().getName());
					indexBuilderThread[i].start();
				}
			}
		}
		synchronized (sem) {
			dlog.debug("_________NOTIFY");
			sem.notify();
			dlog.debug("_________NOTIFY COMPLETE");
		}

	}
	
	public void destroy() {
		if ( !enabled ) return;

		dlog.info("Destroy SearchIndexBuilderWorker ");
		runThreads  = false;
		
		synchronized (sem) {
			sem.notifyAll();
		}
	}
	

	/**
	 * @return Returns the sleepTime.
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime
	 *            The sleepTime to set.
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return Returns the logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger
	 *            The logger to set.
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return Returns the searchIndexBuilder.
	 */
	public SearchIndexBuilderImpl getSearchIndexBuilder() {
		return searchIndexBuilder;
	}

	/**
	 * @param searchIndexBuilder
	 *            The searchIndexBuilder to set.
	 */
	public void setSearchIndexBuilder(SearchIndexBuilderImpl searchIndexBuilder) {
		this.searchIndexBuilder = searchIndexBuilder;
	}

	/**
	 * @return Returns the searchService.
	 */
	public SearchService getSearchService() {
		return searchService;
	}

	/**
	 * @param searchService
	 *            The searchService to set.
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
