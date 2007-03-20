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

package org.sakaiproject.search.component.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.filter.SearchItemFilter;
import org.sakaiproject.search.index.IndexStorage;
import org.sakaiproject.search.model.SearchWriterLock;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * The search service
 * 
 * @author ieb
 */
public class SearchServiceImpl implements SearchService
{

	private static Log log = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * Optional dependencies
	 */
	private List triggerFunctions;

	/**
	 * the notification object
	 */
	private NotificationEdit notification = null;

	/**
	 * init completed
	 */
	private boolean initComplete = false;

	/**
	 * the currently running index searcher
	 */
	private IndexSearcher runningIndexSearcher;

	/**
	 * The index builder dependency
	 */
	private SearchIndexBuilder searchIndexBuilder;

	private long reloadStart;

	private long reloadEnd;

	private NotificationService notificationService;

	private IndexStorage indexStorage = null;

	private SearchItemFilter filter;

	private Map luceneFilters = new HashMap();

	private Map luceneSorters = new HashMap();

	private String defaultFilter = null;

	private String defaultSorter = null;

	private EventTrackingService eventTrackingService;

	private UserDirectoryService userDirectoryService;

	private SessionManager sessionManager;

	private String sharedKey = null;

	private String searchServerUrl = null;

	private boolean searchServer = false;

	private ThreadLocal localSearch = new ThreadLocal();
	
	private HttpClient httpClient;
	private HttpConnectionManagerParams httpParams = new HttpConnectionManagerParams();
	private HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();


	/**
	 * Register a notification action to listen to events and modify the search
	 * index
	 */
	public void init()
	{

		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		notificationService = (NotificationService) load(cm,
				NotificationService.class.getName());
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName());
		eventTrackingService = (EventTrackingService) load(cm,
				EventTrackingService.class.getName());
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		userDirectoryService = (UserDirectoryService) load(cm,
				UserDirectoryService.class.getName());

		try
		{
			if (log.isDebugEnabled())
			{
				log.debug("init start"); //$NON-NLS-1$
				log.debug("checking setup"); //$NON-NLS-1$
			}
			if (indexStorage == null)
			{
				log.error(" indexStorage must be set"); //$NON-NLS-1$
				throw new RuntimeException("Must set indexStorage"); //$NON-NLS-1$

			}
			if (searchIndexBuilder == null)
			{
				log.error(" searchIndexBuilder must be set"); //$NON-NLS-1$
				throw new RuntimeException("Must set searchIndexBuilder"); //$NON-NLS-1$
			}
			if (filter == null)
			{
				log.error("filter must be set, even if its a null filter"); //$NON-NLS-1$
				throw new RuntimeException("Must set filter"); //$NON-NLS-1$
			}
			if (eventTrackingService == null)
			{
				log.error("Event Tracking Service was not found"); //$NON-NLS-1$
				throw new RuntimeException(
						"Event Tracking Service was not found"); //$NON-NLS-1$
			}
			if (sessionManager == null)
			{
				log.error("Session Manager was not found"); //$NON-NLS-1$
				throw new RuntimeException("Session Manager was not found"); //$NON-NLS-1$
			}
			if (userDirectoryService == null)
			{
				log.error("User Directory Service was not found"); //$NON-NLS-1$
				throw new RuntimeException(
						"User Directory Service was not found"); //$NON-NLS-1$
			}

			// register a transient notification for resources
			notification = notificationService.addTransientNotification();

			// add all the functions that are registered to trigger search index
			// modification

			notification.setFunction(SearchService.EVENT_TRIGGER_SEARCH);
			if (triggerFunctions != null)
			{
				for (Iterator ifn = triggerFunctions.iterator(); ifn.hasNext();)
				{
					String function = (String) ifn.next();
					notification.addFunction(function);
					if (log.isDebugEnabled())
					{
						log.debug("Adding Search Register " + function); //$NON-NLS-1$
					}
				}
			}

			// set the filter to any site related resource
			notification.setResourceFilter("/"); //$NON-NLS-1$

			// set the action
			notification.setAction(new SearchNotificationAction(
					searchIndexBuilder));
			
			
			
//			 Configure params for the Connection Manager
			httpParams.setDefaultMaxConnectionsPerHost( 20 );
			httpParams.setMaxTotalConnections( 30 );

//			 This next line may not be necessary since we specified default 2 lines ago, but here it is anyway
			httpParams.setMaxConnectionsPerHost( HostConfiguration.ANY_HOST_CONFIGURATION, 20 );

//			 Set up the connection manager
			httpConnectionManager.setParams( httpParams );

//			 Finally set up the static multithreaded HttpClient
			httpClient = new HttpClient( httpConnectionManager );
			

			

			initComplete = true;
			if (log.isDebugEnabled())
			{
				log.debug("init end"); //$NON-NLS-1$
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to start ", t); //$NON-NLS-1$
		}

	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	/**
	 * @return Returns the triggerFunctions.
	 */
	public List getTriggerFunctions()
	{
		return triggerFunctions;
	}

	/**
	 * @param triggerFunctions
	 *        The triggerFunctions to set.
	 */
	public void setTriggerFunctions(List triggerFunctions)
	{
		if (initComplete)
			throw new RuntimeException(
					" use register function at runtime, setTriggerFucntions is for Spring IoC only"); //$NON-NLS-1$
		this.triggerFunctions = triggerFunctions;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFunction(String function)
	{
		notification.addFunction(function);
		if (log.isDebugEnabled())
		{
			log.debug("Adding Function " + function); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param indexFilter
	 */
	public SearchList search(String searchTerms, List contexts, int start,
			int end)
	{
		return search(searchTerms, contexts, start, end, defaultFilter,
				defaultSorter);
	}

	public SearchList search(String searchTerms, List contexts, int start,
			int end, String filterName, String sorterName)
	{
		try
		{
			BooleanQuery query = new BooleanQuery();
			BooleanQuery contextQuery = new BooleanQuery();
			for (Iterator i = contexts.iterator(); i.hasNext();)
			{
				// Setup query so that it will allow results from any
				// included site, not all included sites.
				contextQuery.add(new TermQuery(new Term(
				SearchService.FIELD_SITEID, (String) i.next())),
				BooleanClause.Occur.SHOULD);
				// This would require term to be in all sites :-(
//				contextQuery.add(new TermQuery(new Term(
//						SearchService.FIELD_SITEID, (String) i.next())),
//						BooleanClause.Occur.MUST);
			}

			QueryParser qp = new QueryParser(SearchService.FIELD_CONTENTS,
					indexStorage.getAnalyzer());
			Query textQuery = qp.parse(searchTerms);
			query.add(contextQuery, BooleanClause.Occur.MUST);
			query.add(textQuery, BooleanClause.Occur.MUST);
			log.info("Compiled Query is " + query.toString()); //$NON-NLS-1$

			if (localSearch.get() == null && searchServerUrl != null
					&& searchServerUrl.length() > 0)
			{
				try
				{
					PostMethod post = new PostMethod(searchServerUrl);
					String userId = sessionManager.getCurrentSessionUserId();
					StringBuffer sb = new StringBuffer();
					for (Iterator ci = contexts.iterator(); ci.hasNext();)
					{
						sb.append(ci.next()).append(";"); //$NON-NLS-1$
					}
					String contextParam = sb.toString();
					post.setParameter(REST_CHECKSUM, digestCheck(userId,
							searchTerms));
					post.setParameter(REST_CONTEXTS, contextParam);
					post.setParameter(REST_END, String.valueOf(end));
					post.setParameter(REST_START, String.valueOf(start));
					post.setParameter(REST_TERMS, searchTerms);
					post.setParameter(REST_USERID, userId);
					
					
					int status = httpClient.executeMethod(post);
					if ( status != 200  ) {
						throw new RuntimeException("Failed to perform remote search, http status was "+status); //$NON-NLS-1$
					}

					String response = post.getResponseBodyAsString();
					return new SearchListResponseImpl(response, textQuery,
							start, end, indexStorage.getAnalyzer(), filter,
							searchIndexBuilder, this);
				}
				catch (Exception ex)
				{

					log.error("Remote Search Failed ", ex); //$NON-NLS-1$
					throw new IOException(ex.getMessage());
				}

			}
			else
			{

				IndexSearcher indexSearcher = getIndexSearcher(false);
				if (indexSearcher != null)
				{
					Hits h = null;
					Filter indexFilter = (Filter) luceneFilters.get(filterName);
					Sort indexSorter = (Sort) luceneSorters.get(sorterName);
					if (log.isDebugEnabled())
					{
						log.debug("Using Filter " + filterName + ":" //$NON-NLS-1$ //$NON-NLS-2$
								+ indexFilter + " and " + sorterName + ":" //$NON-NLS-1$ //$NON-NLS-2$
								+ indexSorter);
					}
					if (indexFilter != null && indexSorter != null)
					{
						h = indexSearcher.search(query, indexFilter,
								indexSorter);
					}
					else if (indexFilter != null)
					{
						h = indexSearcher.search(query, indexFilter);
					}
					else if (indexSorter != null)
					{
						h = indexSearcher.search(query, indexSorter);
					}
					else
					{
						h = indexSearcher.search(query);
					}
					if (log.isDebugEnabled())
					{
						log.debug("Got " + h.length() + " hits"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					eventTrackingService.post(eventTrackingService.newEvent(
							EVENT_SEARCH, EVENT_SEARCH_REF
									+ textQuery.toString(), true,
							NotificationService.PREF_IMMEDIATE));
					return new SearchListImpl(h, textQuery, start, end,
							indexStorage.getAnalyzer(), filter, 
							searchIndexBuilder, this);
				}
				else
				{
					throw new RuntimeException(
							"Failed to start the Lucene Searche Engine"); //$NON-NLS-1$
				}
			}

		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to parse Query ", e); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to run Search ", e); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload()
	{
		getIndexSearcher(true);
	}

	public void forceReload()
	{
		reloadStart = 0;
	}

	public IndexSearcher getIndexSearcher(boolean reload)
	{
		if (runningIndexSearcher == null || ( reload && !searchIndexBuilder.isLocalLock() ) )
		{

			long lastUpdate = indexStorage.getLastUpdate();
			
			if (lastUpdate > reloadStart)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Reloading Index, force=" + reload); //$NON-NLS-1$
				}
				try
				{
					reloadStart = System.currentTimeMillis();

					// dont leave closing the index searcher to the GC. It may
					// not happen fast enough.

					IndexSearcher newRunningIndexSearcher = indexStorage
							.getIndexSearcher();

					IndexSearcher oldRunningIndexSearcher = runningIndexSearcher;
					runningIndexSearcher = newRunningIndexSearcher;

					if (oldRunningIndexSearcher != null)
					{
						try
						{
							indexStorage
									.closeIndexSearcher(oldRunningIndexSearcher);
						}
						catch (Exception ex)
						{
							log.error("Failed to close old searcher ", ex); //$NON-NLS-1$
						}
					}

					reloadEnd = System.currentTimeMillis();
				}
				catch (IOException e)
				{
					reloadStart = reloadEnd;
				}
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("No Reload lastUpdate " + lastUpdate //$NON-NLS-1$
							+ " < lastReload " + reloadStart); //$NON-NLS-1$
				}

			}
		}

		return runningIndexSearcher;
	}

	public void refreshInstance()
	{
		searchIndexBuilder.refreshIndex();

	}

	public void rebuildInstance()
	{
		searchIndexBuilder.rebuildIndex();
	}

	public void refreshSite(String currentSiteId)
	{
		searchIndexBuilder.refreshIndex(currentSiteId);
	}

	public void rebuildSite(String currentSiteId)
	{
		searchIndexBuilder.rebuildIndex(currentSiteId);

	}

	public String getStatus()
	{

		String lastLoad = (new Date(reloadEnd)).toString();
		String loadTime = String
				.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		SearchWriterLock lock = searchIndexBuilder.getCurrentLock();
		List lockNodes = searchIndexBuilder.getNodeStatus();

		return Messages.getString("SearchServiceImpl.40") + lastLoad + Messages.getString("SearchServiceImpl.38") + loadTime + Messages.getString("SearchServiceImpl.37"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public int getNDocs()
	{
		try
		{
			return getIndexSearcher(false).maxDoc();
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public int getPendingDocs()
	{
		return searchIndexBuilder.getPendingDocuments();
	}

	public List getAllSearchItems()
	{
		return searchIndexBuilder.getAllSearchItems();
	}

	public List getSiteMasterSearchItems()
	{
		return searchIndexBuilder.getSiteMasterSearchItems();
	}

	public List getGlobalMasterSearchItems()
	{
		return searchIndexBuilder.getGlobalMasterSearchItems();
	}

	public SearchStatus getSearchStatus()
	{
		String ll = Messages.getString("SearchServiceImpl.36"); //$NON-NLS-1$
		String lt = ""; //$NON-NLS-1$
		if (reloadEnd != 0)
		{
			ll = (new Date(reloadEnd)).toString();
			lt = String.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		}
		final String lastLoad = ll;
		final String loadTime = lt;
		final SearchWriterLock lock = searchIndexBuilder.getCurrentLock();
		final List lockNodes = searchIndexBuilder.getNodeStatus();
		final String pdocs = String.valueOf(getPendingDocs());
		final String ndocs = String.valueOf(getNDocs());

		return new SearchStatus()
		{
			public String getLastLoad()
			{
				return lastLoad;
			}

			public String getLoadTime()
			{
				return loadTime;
			}

			public String getCurrentWorker()
			{
				return lock.getNodename();
			}

			public String getCurrentWorkerETC()
			{
				if (SecurityService.isSuperUser())
				{
					return MessageFormat
							.format(
									Messages.getString("SearchServiceImpl.35"), //$NON-NLS-1$
									new Object[] {
											lock.getExpires(),
											searchIndexBuilder
													.getLastDocument(),
											searchIndexBuilder.getLastElapsed(),
											searchIndexBuilder
													.getCurrentDocument(),
											searchIndexBuilder
													.getCurrentElapsed(),
											ServerConfigurationService
													.getServerIdInstance() });
				}
				else
				{
					return MessageFormat.format(Messages.getString("SearchServiceImpl.39"), new Object[] { lock //$NON-NLS-1$
							.getExpires() });
				}
			}

			public List getWorkerNodes()
			{
				List l = new ArrayList();
				for (Iterator i = lockNodes.iterator(); i.hasNext();)
				{
					SearchWriterLock swl = (SearchWriterLock) i.next();
					Object[] result = new Object[3];
					result[0] = swl.getNodename();
					result[1] = swl.getExpires();
					if (lock.getNodename().equals(swl.getNodename()))
					{
						result[2] = Messages.getString("SearchServiceImpl.47"); //$NON-NLS-1$
					}
					else
					{
						result[2] = Messages.getString("SearchServiceImpl.48"); //$NON-NLS-1$
					}
					l.add(result);
				}
				return l;
			}

			public String getNDocuments()
			{
				return ndocs;
			}

			public String getPDocuments()
			{
				return pdocs;
			}

		};

	}

	public boolean removeWorkerLock()
	{
		return searchIndexBuilder.removeWorkerLock();

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
	 * @return Returns the filter.
	 */
	public SearchItemFilter getFilter()
	{
		return filter;
	}

	/**
	 * @param filter
	 *        The filter to set.
	 */
	public void setFilter(SearchItemFilter filter)
	{
		this.filter = filter;
	}

	/**
	 * @return Returns the defaultFilter.
	 */
	public String getDefaultFilter()
	{
		return defaultFilter;
	}

	/**
	 * @param defaultFilter
	 *        The defaultFilter to set.
	 */
	public void setDefaultFilter(String defaultFilter)
	{
		this.defaultFilter = defaultFilter;
	}

	/**
	 * @return Returns the defaultSorter.
	 */
	public String getDefaultSorter()
	{
		return defaultSorter;
	}

	/**
	 * @param defaultSorter
	 *        The defaultSorter to set.
	 */
	public void setDefaultSorter(String defaultSorter)
	{
		this.defaultSorter = defaultSorter;
	}

	/**
	 * @return Returns the luceneFilters.
	 */
	public Map getLuceneFilters()
	{
		return luceneFilters;
	}

	/**
	 * @param luceneFilters
	 *        The luceneFilters to set.
	 */
	public void setLuceneFilters(Map luceneFilters)
	{
		this.luceneFilters = luceneFilters;
	}

	/**
	 * @return Returns the luceneSorters.
	 */
	public Map getLuceneSorters()
	{
		return luceneSorters;
	}

	/**
	 * @param luceneSorters
	 *        The luceneSorters to set.
	 */
	public void setLuceneSorters(Map luceneSorters)
	{
		this.luceneSorters = luceneSorters;
	}

	public List getSegmentInfo()
	{
		return indexStorage.getSegmentInfoList();
	}

	public TermFrequency getTerms(int documentId) throws IOException
	{
		final TermFreqVector tf = getIndexSearcher(false).getIndexReader()
				.getTermFreqVector(documentId, FIELD_CONTENTS);
		// TODO Auto-generated method stub
		return new TermFrequency()
		{
			public String[] getTerms()
			{
				if ( tf != null ) {
					return tf.getTerms();
				}
				return new String[0];
			}

			public int[] getFrequencies()
			{
				if ( tf != null ) {
					return tf.getTermFrequencies();
				}
				return new int[0];
			}
		};
	}

	public String searchXML(Map parameterMap)
	{
		String userid = null;
		String searchTerms = null;
		String checksum = null;
		String contexts = null;
		String ss = null;
		String se = null;
		try
		{
			if (!searchServer)
			{
				throw new Exception(
						Messages.getString("SearchServiceImpl.49")); //$NON-NLS-1$
			}
			String[] useridA = (String[]) parameterMap.get(REST_USERID);
			String[] searchTermsA = (String[]) parameterMap.get(REST_TERMS);
			String[] checksumA = (String[]) parameterMap.get(REST_CHECKSUM);
			String[] contextsA = (String[]) parameterMap.get(REST_CONTEXTS);
			String[] ssA = (String[]) parameterMap.get(REST_START);
			String[] seA = (String[]) parameterMap.get(REST_END);

			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\"?>"); //$NON-NLS-1$

			boolean requestError = false;
			if (useridA == null || useridA.length != 1)
			{
				requestError = true;
			}
			else
			{
				userid = useridA[0];
			}
			if (searchTermsA == null || searchTermsA.length != 1)
			{
				requestError = true;
			}
			else
			{
				searchTerms = searchTermsA[0];
			}
			if (checksumA == null || checksumA.length != 1)
			{
				requestError = true;
			}
			else
			{
				checksum = checksumA[0];
			}
			if (contextsA == null || contextsA.length != 1)
			{
				requestError = true;
			}
			else
			{
				contexts = contextsA[0];
			}
			if (ssA == null || ssA.length != 1)
			{
				requestError = true;
			}
			else
			{
				ss = ssA[0];
			}
			if (seA == null || seA.length != 1)
			{
				requestError = true;
			}
			else
			{
				se = seA[0];
			}

			if (requestError)
			{
				throw new Exception(Messages.getString("SearchServiceImpl.34")); //$NON-NLS-1$

			}

			int searchStart = Integer.parseInt(ss);
			int searchEnd = Integer.parseInt(se);
			String[] ctxa = contexts.split(";"); //$NON-NLS-1$
			List ctx = new ArrayList(ctxa.length);
			for (int i = 0; i < ctxa.length; i++)
			{
				ctx.add(ctxa[i]);
			}

			if (sharedKey != null && sharedKey.length() > 0)
			{
				String check = digestCheck(userid, searchTerms);
				if (!check.equals(checksum))
				{
					throw new Exception(Messages.getString("SearchServiceImpl.53")); //$NON-NLS-1$
				}
			}

			org.sakaiproject.tool.api.Session s = sessionManager.startSession();
			User u = userDirectoryService.getUser("admin"); //$NON-NLS-1$
			s.setUserId(u.getId());
			sessionManager.setCurrentSession(s);
			localSearch.set("localsearch"); //$NON-NLS-1$
			try
			{

				SearchList sl = search(searchTerms, ctx, searchStart, searchEnd);
				sb.append("<results "); //$NON-NLS-1$
				sb.append(" fullsize=\"").append(sl.getFullSize()) //$NON-NLS-1$
						.append("\" "); //$NON-NLS-1$
				sb.append(" start=\"").append(sl.getStart()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(" size=\"").append(sl.size()).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(" >"); //$NON-NLS-1$
				for (Iterator si = sl.iterator(); si.hasNext();)
				{
					SearchResult sr = (SearchResult) si.next();
					sr.toXMLString(sb);
				}
				sb.append("</results>"); //$NON-NLS-1$
				return sb.toString();
			}
			finally
			{
				sessionManager.setCurrentSession(null);
				localSearch.set(null);
			}
		}
		catch (Exception ex)
		{
			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\"?>"); //$NON-NLS-1$
			sb.append("<fault>"); //$NON-NLS-1$
			sb.append("<request>"); //$NON-NLS-1$
			sb.append("<![CDATA["); //$NON-NLS-1$
			sb.append(" userid = ").append(StringUtils.xmlEscape(userid)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(" searchTerms = ").append(StringUtils.xmlEscape(searchTerms)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(" checksum = ").append(StringUtils.xmlEscape(checksum)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(" contexts = ").append(StringUtils.xmlEscape(contexts)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(" ss = ").append(StringUtils.xmlEscape(ss)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(" se = ").append(StringUtils.xmlEscape(se)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("]]>"); //$NON-NLS-1$
			sb.append("</request>"); //$NON-NLS-1$
			sb.append("<error>"); //$NON-NLS-1$
			sb.append("<![CDATA["); //$NON-NLS-1$
			try
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.flush();
				sb.append(sw.toString());
				pw.close();
				sw.close();
			}
			catch (Exception ex2)
			{
				sb.append("Failed to serialize exception " + ex.getMessage()) //$NON-NLS-1$
						.append("\n"); //$NON-NLS-1$
				sb.append("Case:  " + ex2.getMessage()); //$NON-NLS-1$

			}
			sb.append("]]>"); //$NON-NLS-1$
			sb.append("</error>"); //$NON-NLS-1$
			sb.append("</fault>"); //$NON-NLS-1$
			return sb.toString();
		}
	}

	private String digestCheck(String userid, String searchTerms)
			throws GeneralSecurityException, IOException
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$
		String chstring = sharedKey + userid + searchTerms;
		return byteArrayToHexStr(sha1.digest(chstring.getBytes("UTF-8"))); //$NON-NLS-1$
	}

	private static String byteArrayToHexStr(byte[] data)
	{
		char[] chars = new char[data.length * 2];
		for (int i = 0; i < data.length; i++)
		{
			byte current = data[i];
			int hi = (current & 0xF0) >> 4;
			int lo = current & 0x0F;
			chars[2 * i] = (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
			chars[2 * i + 1] = (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
		}
		return new String(chars);
	}

	/**
	 * @return the sharedKey
	 */
	public String getSharedKey()
	{
		return sharedKey;
	}

	/**
	 * @param sharedKey
	 *        the sharedKey to set
	 */
	public void setSharedKey(String sharedKey)
	{
		this.sharedKey = sharedKey;
	}

	/**
	 * @return the searchServerUrl
	 */
	public String getSearchServerUrl()
	{
		return searchServerUrl;
	}

	/**
	 * @param searchServerUrl
	 *        the searchServerUrl to set
	 */
	public void setSearchServerUrl(String searchServerUrl)
	{
		this.searchServerUrl = searchServerUrl;
	}

	/**
	 * @return the searchServer
	 */
	public boolean isSearchServer()
	{
		return searchServer;
	}

	/**
	 * @param searchServer
	 *        the searchServer to set
	 */
	public void setSearchServer(boolean searchServer)
	{
		this.searchServer = searchServer;
	}

}
