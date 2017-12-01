/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import lombok.extern.slf4j.Slf4j;

import org.osid.OsidContext;
import org.osid.OsidException;
import org.osid.repository.Asset;
import org.osid.repository.AssetIterator;
import org.osid.repository.Repository;
import org.osid.repository.RepositoryException;
import org.osid.repository.RepositoryIterator;
import org.osid.repository.RepositoryManager;
import org.osid.shared.ObjectIterator;
import org.osid.shared.SharedException;
import org.osid.shared.Type;
import org.osid.shared.TypeIterator;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.indiana.lib.twinpeaks.util.SessionContext;

import org.sakaibrary.xserver.session.MetasearchSessionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.citation.util.api.CQLSearchQuery;
import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.citation.util.api.SearchCancelException;
import org.sakaiproject.citation.util.api.SearchQuery;
import org.sakaiproject.citation.api.ActiveSearch;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationIterator;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.citation.api.SearchCategory;
import org.sakaiproject.citation.api.SearchDatabase;
import org.sakaiproject.citation.api.SearchDatabaseHierarchy;
import org.sakaiproject.citation.api.SearchManager;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;

/**
 *
 */
@Slf4j
public class BaseSearchManager implements SearchManager, Observer
{
  /**
   * Maximum number of consecutive duplicate result records we'll accept
   * before disabling the duplicate record check for the current search.
   *
   * The idea is to avoid displaying occasional duplicates, but don't
   * discard forever in the pathological case where we're only getting
   * duplicate results.
   */
  protected static int  MAX_DUPLICATES = 10;


	public class BasicObjectIterator
	implements ObjectIterator
	{
	    protected int i = 0;
	    protected Vector vector = new Vector();

	    public BasicObjectIterator(List keys)
	    throws SharedException
	    {
	        this.vector = new Vector(keys);
	    }

	    public boolean hasNextObject()
	    throws SharedException
	    {
	        return i < vector.size();
	    }

	    public java.io.Serializable nextObject()
	    throws SharedException
	    {
	        if (i < vector.size())
	        {
	            return (java.io.Serializable)vector.elementAt(i++);
	        }
	        else
	        {
	            throw new SharedException(SharedException.NO_MORE_ITERATOR_ELEMENTS);
	        }
	    }
	}


	public class BasicSearch implements ActiveSearch
	{
		protected List m_assets;
		protected List m_pageOrder;
		protected Set m_duplicateCheck;
		protected boolean m_duplicateCheckEnabled;
		protected boolean m_firstPage;
		protected String m_searchId;
		protected String m_searchType;
		protected boolean m_lastPage;
		protected boolean m_newSearch;
		protected Integer m_pageSize;
		protected Integer m_startRecord;
  	protected String[] m_databaseIds;

		protected AssetIterator m_assetIterator;
		protected Integer m_numRecordsFetched;
		protected Integer m_numRecordsFound;
		protected Integer m_numRecordsMerged;
		protected Repository m_repository;
		protected String m_repositoryId;
		protected String m_repositoryName;
		protected SearchQuery m_basicQuery;
		protected SearchQuery m_advancedQuery;
		protected String m_sortBy;

		protected CitationCollection m_searchResults;
		protected CitationCollection m_savedResults;
		protected CitationIterator m_resultsIterator;
		protected Map m_index;
		protected int m_lastPageViewed = -1;
		protected CitationIterator m_searchIterator;
		protected int start = 1;
		protected int end = DEFAULT_PAGE_SIZE;
		protected int m_viewPageSize = DEFAULT_PAGE_SIZE;
		protected String statusMessage = null;

		// saves the thread that the current search is running in
		protected Thread m_searchThread;

		/**
		 * Constructor
         */
        public BasicSearch()
        {
	        this.m_searchId = newSearchId();
	        this.m_searchType = null;
	        this.m_assets = new Vector();
	        m_pageOrder = new Vector();
	        this.m_index = new Hashtable();
	        m_duplicateCheck = new TreeSet();
	        m_duplicateCheckEnabled = true;
	        m_savedResults = CitationService.getTemporaryCollection();
	        m_newSearch = true;
	        m_firstPage = true;
	        m_lastPage = false;
	        m_pageSize = new Integer(DEFAULT_PAGE_SIZE);
	        m_startRecord = new Integer(DEFAULT_START_RECORD);
	        m_sortBy = DEFAULT_SORT_BY;
          m_databaseIds = null;
        }

		/**
         */
        public BasicSearch(CitationCollection searchResults)
        {
	        this.m_searchId = newSearchId();
	        this.m_assets = new Vector();
	        m_pageOrder = new Vector();
	        this.m_index = new Hashtable();
	        m_duplicateCheck = new TreeSet();
	        m_duplicateCheckEnabled = true;
	        m_savedResults = CitationService.getTemporaryCollection();
	        m_newSearch = true;
	        m_firstPage = true;
	        m_lastPage = false;
	        m_pageSize = new Integer(DEFAULT_PAGE_SIZE);
	        m_startRecord = new Integer(DEFAULT_START_RECORD);
	        m_sortBy = DEFAULT_SORT_BY;
          m_databaseIds = null;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getAssetIterator()
         */
        protected AssetIterator getAssetIterator()
        {
	        return m_assetIterator;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getAssets()
         */
        public List getAssets()
        {
	        return m_assets;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getGuid()
         */
        public String getSearchId()
        {
	        return m_searchId;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getNumRecordsFetched()
         */
        public Integer getNumRecordsFetched()
        {
	        return m_numRecordsFetched;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getNumRecordsFound()
         */
        public Integer getNumRecordsFound()
        {
	        return m_numRecordsFound;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getNumRecordsMerged()
         */
        public Integer getNumRecordsMerged()
        {
	        return m_numRecordsMerged;
        }

        protected void setPageLimits(int page) throws SearchException
        {

        }

    	/**
    	 * @param page
    	 * @return
    	 * @throws SearchException
    	 */
    	public List viewPage(int page) throws SearchException, SearchCancelException
    	{
    		List citations = new Vector();
    		boolean searchPerformed = false;

          	if(page < 0)
        	{
        		page = 0;
        	}

          	int oldStart = this.start;
          	int oldEnd   = this.end;
        	this.start = page * m_viewPageSize;
         	this.end = start + m_viewPageSize;
        	if(start > this.m_pageOrder.size() + 1)
        	{
        		throw new SearchException("Request beyond next page");
        	}
        	else
        	{
        		if(this.m_pageOrder.isEmpty())
        		{
        			doSearch(this);
        			searchPerformed = true;
        		}
        		else if(end > this.m_pageOrder.size())
        		{
	        		try
	                {
		                doNextPage(this);
		                searchPerformed = true;
	                }
	                catch (SearchException e)
	                {
        				this.start = oldStart;
        				this.end   = oldEnd;
	                	setStatusMessage(m_repository);
	                	throw new SearchException(e.getMessage());
	                }
        		}
        	}
          /*
           * Determine the proper the "last page" setting.
           */
          log.debug(">>> viewPage() new page is " + page
                  +   ", last page is " + m_lastPageViewed);
          log.debug(">>> viewPage() was a search done? " + searchPerformed);
          log.debug(">>> viewPage() did we find the last page? " + this.isLastPage());
          log.debug(">>> viewPage() records found = " + getNumRecordsFetched()
                  +   ", records rendered = " + m_pageOrder.size());
          /*
           * Step 1: Previous pages (and the first) are a special case
           */
          if (page < m_lastPageViewed)
          {
            setLastPage(false);
          }
          /*
           * Step 2: Re-evaluate the "last page" status if one of these is true:
           *
           *    o This is a previous (or the first) page
           *    o This was purely a page size adjustment (no search required)
           *    o A search was performed (and it didn't hit "end-of-search-results")
           */
          if ((page < m_lastPageViewed)
          ||  (!searchPerformed)
          ||  (searchPerformed && !isLastPage()))
          {
            int estimatedHits = getNumRecordsFound();
            int hitsRendered  = m_pageOrder.size();
            int pageHits      = (page == 0) ? m_viewPageSize
                                            : ((page + 1) * m_viewPageSize);
            /*
             * Step 3: This is the last page if:
             *
             *    o The estimated number of possible results will fit on the
             *      current page
             * or
             *    o The number of results actually rendered is less than the
             *      current page size (we ran out)
             */
         		log.debug(">>> viewPage() estimate ("
         		        +   estimatedHits
         		        +   ") <= page size (in hits) ("
         		        +   pageHits
         		        +   ") ? "
         		        +   (estimatedHits <= pageHits));

         		log.debug(">>> viewPage() records rendered ("
         		        +   hitsRendered
         		        +   ") < page size (in hits) ("
         		        +   pageHits
         		        +   ") ? "
         		        +   (hitsRendered < pageHits));

         		if ((estimatedHits <= pageHits) || (hitsRendered < pageHits))
         		{
         		  setLastPage(true);
            }
          }

        	if(end > m_pageOrder.size())
        	{
        		end = m_pageOrder.size();
        	}

        	Citation citation = null;
        	for(int i = start; i < end; i++)
        	{
        		String id = (String) m_pageOrder.get(i);
        		try
                {
	                citation = m_searchResults.getCitation(id);
	                citations.add(citation);
                }
                catch (IdUnusedException e)
                {
	                log.warn("BasicSearch.getPage() unable to retrieve ciataion: " + id);
                }
        	}
        	m_lastPageViewed = page;

    		return citations;
    	}

    	/**
         *
         */
        protected void setStatusMessage(Repository repository)
        {
        	try
        	{
        		this.statusMessage = getSearchStatusMessage(repository);
        	}
        	catch(SearchException e)
        	{
        		this.statusMessage = e.getMessage();
        	}
        }

        public void setStatusMessage(String msg)
        {
        	this.statusMessage = msg;
        }

        public void setStatusMessage()
        {
        	this.statusMessage = null;
        }

        public String getStatusMessage()
        {
        	return this.statusMessage;
        }

        /**
         * Set the selected list of searchable databases
         * @param A list of database IDs
         */
        public void setDatabaseIds(String[] databaseIds)
        {
        	m_databaseIds = databaseIds;
        }

        /**
         * Fetch the selected list of searchable databases
         * @return The list of database IDs (null if none)
         */
        public String[] getDatabaseIds()
        {
        	return m_databaseIds;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getPageSize()
         */
        public Integer getPageSize()
        {
	        return m_pageSize;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getRepository()
         */
        public Repository getRepository()
        {
	        return m_repository;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getRepositoryId()
         */
        public String getRepositoryId()
        {
	        return m_repositoryId;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getRepositoryName()
         */
        public String getRepositoryName()
        {
	        return m_repositoryName;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getSearchCriteria()
         */
        public SearchQuery getBasicQuery()
        {
	        return m_basicQuery;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getSortBy()
         */
        public String getSortBy()
        {
	        return m_sortBy.toLowerCase();
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getStartRecord()
         */
        public Integer getStartRecord()
        {
        	if(m_startRecord.intValue() < MIN_START_RECORD)
        	{
        		m_startRecord = new Integer(MIN_START_RECORD);
        	}
	        return m_startRecord;
        }

		/**
         * @return the firstPage
         */
        public boolean isFirstPage()
        {
        	return m_firstPage;
        }

		/**
         * @return the lastPage
         */
        public boolean isLastPage()
        {
        	return m_lastPage;
        }

		/**
         * @return the newSearch
         */
        public boolean isNewSearch()
        {
        	return m_newSearch;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setAssetIterator(org.osid.repository.AssetIterator)
         */
        public void setAssetIterator(AssetIterator assetIterator)
        {
	        this.m_assetIterator = assetIterator;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setAssets(java.util.List)
         */
        public void setAssets(List assets)
        {
	        this.m_assets = assets;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setFirstPage(boolean)
         */
        public void setFirstPage(boolean firstPage)
        {
	        this.m_firstPage = firstPage;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setGuid(java.lang.String)
         */
        public void setGuid(String guid)
        {
	        this.m_searchId = guid;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setLastPage(boolean)
         */
        public void setLastPage(boolean lastPage)
        {
	        this.m_lastPage = lastPage;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setNewSearch(boolean)
         */
        public void setNewSearch(boolean newSearch)
        {
	        this.m_newSearch = newSearch;
        }

		/**
         * @param numRecordsFetched the numRecordsFetched to set
         */
        public void setNumRecordsFetched(Integer numRecordsFetched)
        {
        	m_numRecordsFetched = numRecordsFetched;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setNumRecordsFound(java.lang.Integer)
         */
        public void setNumRecordsFound(Integer numRecordsFound)
        {
	        this.m_numRecordsFound = numRecordsFound;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setNumRecordsMerged(java.lang.Integer)
         */
        public void setNumRecordsMerged(Integer numRecordsMerged)
        {
	        this.m_numRecordsMerged = numRecordsMerged;
        }

		/**
         * @param pageSize the pageSize to set
         */
        public void setPageSize(Integer pageSize)
        {
        	if(pageSize == null || pageSize.intValue() < 1)
        	{
        		m_pageSize = new Integer(DEFAULT_PAGE_SIZE);
        	}
        	else
        	{
            	m_pageSize = pageSize;
         	}
        }

		/**
         * @param pageSize the pageSize to set
         */
        public void setPageSize(String pageSize)
        {
        	if(pageSize == null || pageSize.trim().equals(""))
        	{
        		m_pageSize = new Integer(DEFAULT_PAGE_SIZE);
        	}
        	else
        	{
            	try
            	{
            		m_pageSize = new Integer(pageSize);
            	}
            	catch(NumberFormatException e)
            	{
            		m_pageSize = new Integer(DEFAULT_PAGE_SIZE);
            	}
        	}
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setRepository(org.osid.repository.Repository)
         */
        public void setRepository(Repository repository)
        {
	        this.m_repository = repository;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setRepositoryId(java.lang.String)
         */
        public void setRepositoryId(String repositoryId)
        {
	        this.m_repositoryId = repositoryId;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setRepositoryName(java.lang.String)
         */
        public void setRepositoryName(String repositoryName)
        {
	        this.m_repositoryName = repositoryName;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setSearchCriteria(java.lang.String)
         */
        public void setBasicQuery(SearchQuery basicQuery)
        {
	        this.m_basicQuery = basicQuery;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setSortBy(java.lang.String)
         */
        public void setSortBy(String sortBy)
        {
	        this.m_sortBy = sortBy;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setStartRecord(java.lang.Integer)
         */
        public void setStartRecord(Integer startRecord)
        {
        	if(startRecord.intValue() < MIN_START_RECORD)
        	{
        		this.m_startRecord = new Integer(MIN_START_RECORD);
        	}
        	else
        	{
        		this.m_startRecord = startRecord;
        	}
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getSearchResults()
         */
        public CitationCollection getSearchResults()
        {
	        return m_searchResults;
        }

		/**
         * @param searchResults the searchResults to set
         */
        public void setSearchResults(CitationCollection searchResults)
        {
        	m_searchResults = searchResults;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setStartRecord(java.lang.String)
         */
        public void setStartRecord(String startRecord)
        {
           	if(startRecord == null || startRecord.trim().equals(""))
        	{
        		m_startRecord = new Integer(DEFAULT_START_RECORD);
        	}
        	else
        	{
            	try
            	{
            		m_startRecord = new Integer(startRecord);
            	}
            	catch(NumberFormatException e)
            	{
            		m_startRecord = new Integer(DEFAULT_START_RECORD);
            	}
        	}
         }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getIndex()
         */
        public Map getIndex()
        {
	        return m_index;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#setIndex(java.util.Map)
         */
        public void setIndex(Map index)
        {
	        m_index = new Hashtable( index );
        }

		/**
         * @return
         */
        public Set getDuplicateCheck()
        {
	        if(m_duplicateCheck == null)
	        {
	        	m_duplicateCheck = new TreeSet();
	        }
	        return m_duplicateCheck;
        }

    /**
     * Are we checking for duplicate search results?
     * @return true if so
     */
    public boolean isDuplicateCheckEnabled()
    {
      return m_duplicateCheckEnabled;
    }

    /**
     * Enable/disable duplicate checking
     * @param state true to enable the duplicate check
     */
    public void setDuplicateCheckEnabled(boolean state)
    {
      m_duplicateCheckEnabled = state;
    }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#prepareForNextPage()
         *
         * As far as I can tell, this is no longer used.  At one time it was
         * referenced from CitationsHelperAction.  SRS, 03/34/09
         */
        public void prepareForNextPage()
        {
        	Iterator it = m_searchResults.getCitations().iterator();
        	while(it.hasNext())
        	{
        		Citation citation = (Citation) it.next();
        		if(! m_pageOrder.contains(citation.getId()))
        		{
        			m_pageOrder.add(citation.getId());
        		}
        	}

	        this.m_savedResults.addAll(this.m_searchResults);
	        this.m_searchResults.clear();
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getLastPageViewed()
         */
        public int getViewPageNumber()
        {
	        return m_lastPageViewed ;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#getLastPageViewed()
         */
        public int getViewPageSize()
        {
	        return m_viewPageSize ;
        }

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.ActiveSearch#viewPage()
         */
        public List viewPage() throws SearchException, SearchCancelException
        {
        	return viewPage(0);
        }

		public int getFirstRecordIndex()
		{
			return start;
		}

		public int getLastRecordIndex()
		{
			return end;
		}

		public void setViewPageSize(int size)
		{
			m_viewPageSize = size;
		}

		public String getSearchType() {
			return m_searchType;
		}

		public void setSearchType(String searchType) {
			m_searchType = searchType;
		}

		public SearchQuery getAdvancedQuery() {
			return m_advancedQuery;
		}

		public void setAdvancedQuery(SearchQuery advancedQuery) {
			m_advancedQuery = advancedQuery;
		}

		public Thread getSearchThread() {
			return m_searchThread;
		}

		public void setSearchThread(Thread searchThread) {
			m_searchThread = searchThread;
		}

		public void resetSearch()
		{
			this.m_pageOrder.clear();

			this.m_searchResults.clear();

		}

	}

	public class BasicSearchProperties implements org.osid.shared.Properties
	{
		protected List keys;
		protected java.util.Properties properties;
		protected Type type = new BasicType( "sakaibrary", "properties", "asynchMetasearch" );

		public BasicSearchProperties( java.util.Properties properties )
		{
			this.keys = new Vector();
			this.properties = properties;

			Enumeration keyNames = properties.keys();
			while( keyNames.hasMoreElements() ) {
				this.keys.add( (java.io.Serializable)keyNames.nextElement() );
			}
		}

		public ObjectIterator getKeys()
			throws SharedException
		{
			return new BasicObjectIterator( keys );
		}

		public java.io.Serializable getProperty( java.io.Serializable key )
			throws SharedException
		{
			return (java.io.Serializable)properties.get( key );
		}

		public Type getType()
			throws SharedException
		{
			return type;
		}
	}

	public class BasicType extends org.osid.shared.Type
	{

	    protected BasicType(String authority
	                 , String domain
	                 , String keyword)
	    {
	        super(authority,domain,keyword);
	    }

	    public BasicType(String authority
	                 , String domain
	                 , String keyword
	                 , String description)
	    {
	        super(authority,domain,keyword,description);
	    }

//	    public final Type CITATION 			= new BasicType("sakaibrary", "recordStructure", "citation");
//	    public final Type CREATOR 			= new BasicType("mit.edu", "partStructure", "creator");
//	    public final Type DATE 				= new BasicType("mit.edu", "partStructure", "date");
//	    public final Type DATE_RETRIEVED 	= new BasicType("sakaibrary", "partStructure", "dateRetrieved");
//	    public final Type DOI 				= new BasicType("sakaibrary", "partStructure", "doi");
//	    public final Type EDITION 			= new BasicType("sakaibrary", "partStructure", "edition");
//	    public final Type END_PAGE 			= new BasicType("sakaibrary", "partStructure", "endPage");
//	    public final Type INLINE_CITATION 	= new BasicType("sakaibrary", "partStructure", "inLineCitation");
//	    public final Type ISN_IDENTIFIER 	= new BasicType("sakaibrary", "partStructure", "isnIdentifier");
//	    public final Type ISSUE 			= new BasicType("sakaibrary", "partStructure", "issue");
//	    public final Type LANGUAGE 			= new BasicType("mit.edu", "partStructure", "language");
//	    public final Type LOC_IDENTIFIER 	= new BasicType("sakaibrary", "partStructure", "locIdentifier");
//	    public final Type NOTE 				= new BasicType("sakaibrary", "partStructure", "note");
//	    public final Type OPEN_URL 			= new BasicType("sakaibrary", "partStructure", "openUrl");
//	    public final Type PAGES 			= new BasicType("sakaibrary", "partStructure", "pages");
//	    public final Type PUB_LOCATION 		= new BasicType("sakaibrary", "partStructure", "publicationLocation");
//	    public final Type PUBLISHER 		= new BasicType("mit.edu", "partStructure", "publisher");
//	    public final Type RIGHTS 			= new BasicType("sakaibrary", "partStructure", "rights");
//	    public final Type SOURCE_TITLE 		= new BasicType("sakaibrary", "partStructure", "sourceTitle");
//	    public final Type START_PAGE 		= new BasicType("sakaibrary", "partStructure", "startPage");
//	    public final Type SUBJECT 			= new BasicType("mit.edu", "partStructure", "subject");
//	    public final Type TYPE 				= new BasicType("mit.edu", "partStructure", "type");
//	    public final Type URL 				= new BasicType("mit.edu", "partStructure", "url");
//	    public final Type URL_FORMAT 		= new BasicType("sakaibrary", "partStructure", "urlFormat");
//	    public final Type URL_LABEL 		= new BasicType("sakaibrary", "partStructure", "urlLabel");
//	    public final Type VOLUME 			= new BasicType("sakaibrary", "partStructure", "volume");
//	    public final Type YEAR 				= new BasicType("sakaibrary", "partStructure", "year");

	}

	/**
	 * @author gbhatnag
	 *
	 */
	public class BasicSearchDatabaseHierarchy
	extends org.xml.sax.helpers.DefaultHandler
	implements SearchDatabaseHierarchy
	{

		public class BasicSearchCategory implements SearchCategory
		{
			private String id;
			private String displayName;
			private String description;
			private boolean defaultStatus;

			// list of sub-categories contained in this category (could be null)
			private java.util.List<SearchCategory> subcategoryList;

			// list of database ids contained in this category (could be null)
			private java.util.List<String> databaseList;

			// list of database ids that are recommended within this category
			// (could be null)
			private java.util.List<String> recommendedDatabases;

			// map of databases with alternate metadata within this category
			// keyed using database id
			private java.util.Map<String, SearchDatabase> altDatabases;

			/**
			 * BasicSearchCategory constructor creates a BasicSearchCategory
			 * with the given name and id
			 *
			 * @param name display name for this category
			 * @param id unique identifier for this category
			 */
			protected BasicSearchCategory( String name, String id )
			{
				this.id = id;
				this.displayName = name;
				this.description = null;
				this.defaultStatus = false;
			}

			protected void updateDescription( String description )
			{
				this.description = description;
			}

			protected void addSubcategory( SearchCategory subcategory )
			{
				if( subcategory != null )
				{
					if( subcategoryList == null )
					{
						subcategoryList = new Vector<SearchCategory>();
					}
					subcategoryList.add( subcategory );
				}
				else
				{
					log.warn( "BasicSearchCategory.addSubCategory() was " +
							"passed a null subcategory to add" );
				}
			}

			protected void addDatabase( String databaseId )
			{
				if( databaseId != null )
				{
					if( databaseList == null )
					{
						databaseList = new Vector<String>();
					}
					databaseList.add( databaseId );
				}
				else
				{
					log.warn( "BasicSearchCategory.addDatabase() was " +
							"passed a null databaseId to add" );
				}
			}

			protected void addRecommendedDatabase( String databaseId )
			{
				if( databaseId != null )
				{
					if( recommendedDatabases == null )
					{
						recommendedDatabases = new Vector<String>();
					}
					recommendedDatabases.add( databaseId );

					// if this database is not in the overall list of databases,
					// add it
					if( databaseList == null )
					{
						databaseList = new Vector<String>();
					}

					if( !databaseList.contains( databaseId ) )
					{
						databaseList.add( databaseId );
					}
				}
				else
				{
					log.warn( "BasicSearchCategory.addRecommendedDatabase()" +
							" was passed a null databaseId to add" );
				}
			}

			protected void addAlternateDatabase( SearchDatabase altDatabase )
			{
				if( altDatabase != null )
				{
					if( altDatabases == null )
					{
						altDatabases = new Hashtable<String, SearchDatabase>();
					}
					altDatabases.put( altDatabase.getId(), altDatabase );

					// if this database is not in the overall list of databases,
					// add it
					if( !databaseList.contains( altDatabase.getId() ) )
					{
						databaseList.add( altDatabase.getId() );
					}
				}
				else
				{
					log.warn( "BasicSearchCategory.addAlternateDatabase() " +
							"was passed a null SearchDatabase to add" );
				}
			}

			protected void setDefault( boolean value )
			{
				this.defaultStatus = value;
			}

			protected boolean isDefault()
			{
				return defaultStatus;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#hasDatabases()
			 */
			public boolean hasDatabases() {
				return ( databaseList != null && !databaseList.isEmpty() );
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#getDatabases()
			 */
			public List<SearchDatabase> getDatabases() {
				// List to be returned
				Vector<SearchDatabase> databases = new Vector<SearchDatabase>();

				// make sure this category has databases in it
				if( !hasDatabases() )
				{
					log.warn( "Search Library Resources Category: '" +
							displayName + "' contains no databases." );
				}
				else
				{
					for( int i = 0; i < databaseList.size(); i++ )
					{
						String databaseId = databaseList.get(i);
						SearchDatabase database;

						// check if there is an alternate for this database
						if( altDatabases != null &&
								altDatabases.containsKey( databaseId ) )
						{
							database = altDatabases.get( databaseId );
						}
						else
						{
							// get the database from the global map of databases
							database = databaseMap.get( databaseId );
						}

						// make sure we have found the database
						if( database == null )
						{
							// database not found
							log.warn( "Unidentified Search Libary Resources " +
									"database: '" + databaseId +
									"' in category: " + displayName );
						}
						else
						{
							// check if the database is a member of authorized groups
							for( String groupId : groups )
							{
								if( database.isGroupMember( groupId ) )
								{
									// add to the return List
									databases.add( database );
									break;
								}
							}
						}
					}
				}
				return databases;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#getDescription()
			 */
			public String getDescription() {
				return description;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#getDisplayName()
			 */
			public String getDisplayName() {
				return displayName;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#getId()
			 */
			public String getId() {
				return id;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#hasSubCategories()
			 */
			public boolean hasSubCategories() {
				return ( subcategoryList != null && !subcategoryList.isEmpty() );
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#getSubCategories()
			 */
			public List<SearchCategory> getSubCategories() {
				return subcategoryList;
			}

			/* (non-Javadoc)
			 * @see org.sakaiproject.citation.api.SearchCategory#isDatabaseRecommended()
			 */
			public boolean isDatabaseRecommended( String databaseId ) {
				return recommendedDatabases.contains( databaseId );
			}

		}  // public class BasicSearchCategory

		public class BasicSearchDatabase implements SearchDatabase
		{
			private String id;
			private String displayName;
			private String description;

			// groups this database belongs to
			private List<String> groups;

			protected BasicSearchDatabase( String name, String id )
			{
				this.displayName = name;
				this.id = id;
				this.description = null;
			}

			protected void updateDescription( String description )
			{
				this.description = description;
			}

			protected void addGroup( String groupId )
			{
				if( groupId != null )
				{
					if( groups == null )
					{
						groups = new Vector<String>();
					}
					groups.add( groupId );
				}
				else
				{
					log.warn( "BasicSearchDatabase.addGroup() " +
							"was passed a null groupId to add" );
				}
			}

			public String getDescription() {
				return description;
			}

			public String getDisplayName() {
				return displayName;
			}

			public String getId() {
				return id;
			}

			public boolean isGroupMember( String groupId ) {
				return groups.contains( groupId );
			}

		}  // public class BasicSearchDatabase


		/* org.sakaiproject.citation
		 * BasicSearchDatabaseHierarchy instance variables
		 */

		// this user's repository and groups
		protected String repositoryPkgName;
		protected List<String> groups;

		// root category (contains top level categories)
		protected BasicSearchCategory rootCategory;

		// map containing all databases, keyed by database id
		protected java.util.Map<String, SearchDatabase> databaseMap;

		// map containing all categories, keyed by category id
		protected java.util.Map<String, SearchCategory> categoryMap;

		// default category
		protected SearchCategory defaultCategory;

		// configured flag
		protected boolean isConfigured;

		// for SAX parsing
		protected StringBuilder textBuffer;
		protected boolean recommendedDatabaseFlag;
		protected int hierarchyDepth;
		protected java.util.Stack<BasicSearchCategory> categoryStack;
		protected BasicSearchDatabase currentDatabase;
		protected String currentDatabaseId;


		public BasicSearchDatabaseHierarchy(String xmlContent)
        {
			/*
			 * Any basic user authn/authz things we can check to not go
			 * further than we have to?
			 */

			// get a ConfigurationService instance
			if( m_configService == null )
			{
				log.warn( "BasicSearchDatabaseHierarchy() m_configService is " +
						"null - components.xml injection did not work... getting instance from cover" );
				m_configService = org.sakaiproject.citation.cover.ConfigurationService.getInstance();
			}

      isConfigured = false;
			try
			{
				/*
				 * Determine which repository implementation this user should get
				 * access to
				 *  - ip-based
				 *  - other things?
				 *
				 *  (currently assuming X-Server)
				 */
//				repositoryPkgName = "org.sakaibrary.osid.repository.xserver";
				repositoryPkgName = m_configService.getSiteConfigOsidPackageName();
				if(isNull(repositoryPkgName))
				{
					// cannot continue
					return;
				}

				/*
				 * Now we know which metasearch engine, get the corresponding XML
				 * for that database
				 *  - XML describes all accessible databases/categories for a given
				 *  metasearch engine
				 *
				 *  (currently assuming CATEGORIES_XML for all users)
				 */

				/*
				 * Determine which groups this user is a member of
				 *  - should get an array of strings with group names/ids
				 *  which should appear in the XML
				 */
//				String[] tempGroups = { "all", "free" };
//				groups = tempGroups;
				groups = m_configService.getGroupIds();

				/*
				 * Parse the XML using the group information to build a hierarchy
				 * of categories and databases this user has access to
				 */
				recommendedDatabaseFlag = false;
				hierarchyDepth = 0;
				databaseMap = new java.util.Hashtable<String, SearchDatabase>();
				categoryMap = new java.util.Hashtable<String, SearchCategory>();
				categoryStack = new java.util.Stack<BasicSearchCategory>();

				parseXML(xmlContent);
			}
			catch (Exception exception)
			{
				log.warn("Exception seen in BasicSearchDatabaseHierarchy() constructor", exception);
			}
  }

		protected void parseXML(String xmlContent)
        {
			// Use the default (non-validating) parser
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        InputSource source = new InputSource(new StringReader(xmlContent));

	        try {
	            // Parse the input
	            SAXParser saxParser = factory.newSAXParser();
	            saxParser.parse( source, this );
              log.debug("After parse, categories found = " + categoryMap.size());
	            isConfigured = (this.categoryMap.size() > 0) ? true : false;
	        } catch (SAXParseException spe) {
	            // Use the contained exception, if any
	            Exception x = spe;

	            if (spe.getException() != null) {
	                x = spe.getException();
	            }

	            // Error generated by the parser
	        	log.warn("parseXML() parsing exception: " +
	        			spe.getMessage() + " - xml line " + spe.getLineNumber()
	        			+ ", uri " + spe.getSystemId(), x);

	        	// unset configuration flag
	        	isConfigured = false;
	        } catch (SAXException sxe) {
	            // Error generated by this application
	            // (or a parser-initialization error)
	            Exception x = sxe;

	            if (sxe.getException() != null) {
	                x = sxe.getException();
	            }

	            log.warn( "parseXML() SAX exception: " +
	            		sxe.getMessage(), x );
	            // unset configuration flag
	        	isConfigured = false;
	        } catch (ParserConfigurationException pce) {
	            // Parser with specified options can't be built
	        	log.warn( "parseXML() SAX parser cannot be built " +
	        			"with specified options" );

	        	// unset configuration flag
	        	isConfigured = false;
	        } catch (IOException ioe) {
	            // I/O error
	        	log.warn( "parseXML() IO exception", ioe );

	        	// unset configuration flag
	        	isConfigured = false;
	        } catch (Throwable t) {
	        	log.warn( "parseXML() exception", t );

	        	// unset configuration flag
	        	isConfigured = false;
	        }
        }

		protected void setDefaultCategory( SearchCategory defaultCategory )
		{
			if( defaultCategory != null )
			{
				this.defaultCategory = defaultCategory;
			}
			else
			{
				log.warn( "BasicSearchDatabaseHierarchy.setDefaultCategory()"+
						" was passed a null SearchCategory to set" );
			}
		}

		protected void addTopLevelCategory( SearchCategory topLevelCategory )
		{
			if( topLevelCategory != null )
			{
				if( rootCategory == null )
				{
					rootCategory = new BasicSearchCategory(
							SearchDatabaseHierarchy.ROOT_CATEGORY_NAME,
							SearchDatabaseHierarchy.ROOT_CATEGORY_ID );
				}
				rootCategory.addSubcategory( topLevelCategory );
			}
			else
			{
				log.warn( "BasicSearchDatabaseHierarchy.addTopLevelCategory()"+
						" was passed a null SearchCategory to add" );
			}
		}

	    public void startElement( String namespaceURI, String sName,
	    		String qName, Attributes attrs ) throws SAXException
	    {
	        String eName = sName;  // element name

	        if( eName.equals( "" ) )
	        {
	            eName = qName;  // not namespaceAware
	        }

	        if( eName.equals( "category" ) )
	        {
	        	// create a new category with the given attribute info
	        	BasicSearchCategory newCategory = new BasicSearchCategory(
	        			attrs.getValue( "name" ), attrs.getValue( "id" ) );

	        	// check if this is the default category
	        	if( attrs.getValue( "default" ) != null )
	        	{
	        		newCategory.setDefault( true );
	        	}

	        	// add new category to the stack
	        	categoryStack.push( newCategory );
	        }
	        else if( eName.equals( "database" ) )
	        {
	        	// create a new database with the given attribute info
	        	currentDatabase = new BasicSearchDatabase(
	        			attrs.getValue( "name" ), attrs.getValue( "id" ) );
	        }
	        else if( eName.equals( "category_database" ) )
	        {
	        	// determine whether this is a "recommended" database
	        	if( attrs.getValue( "recommended" ) != null )
	        	{
	        		recommendedDatabaseFlag = true;
	        	}
	        }
	    }

	    public void endElement( String namespaceURI, String sName,
	    		String qName ) throws SAXException
	    {
	    	String eName = sName;  // element name

	        if( eName.equals( "" ) )
	        {
	            eName = qName;  // not namespaceAware
	        }

	        parseData( eName );
	    }

	    public void characters( char[] buf, int offset, int len )
        throws SAXException
        {
	    	String s = new String( buf, offset, len );

	        if( textBuffer == null )
	        {
	            textBuffer = new StringBuilder( s );
	        }
	        else
	        {
	            textBuffer.append( s );
	        }
	    }

	    protected String getAttribute( Attributes attrs, String attrName )
	    {
	    	if( attrs != null )
	    	{
	            for( int i = 0; i < attrs.getLength(); i++ )
	            {
	                String name = attrs.getLocalName( i );

	                if( name.equals( "" ) )
	                {
	                    name = attrs.getQName(i);
	                }

	                if( name.equals( attrName ) )
	                {
	                	return attrs.getValue( i );
	                }
	            }
	    	}
	    	return null;
	    }

	    protected void parseData( String endElement )
	    {
	    	String text = null;
	    	if( textBuffer != null )
	    	{
	    		text = textBuffer.toString().trim();
	    	}

	    	/*
			 * category elements
			 */
	    	if( endElement.equals( "category_description" ) )
			{
	    		BasicSearchCategory temp = categoryStack.pop();
	    		temp.updateDescription( text );
				categoryStack.push( temp );
			}
	    	else if( endElement.equals( "category" ) )
	    	{
	    		// a category has just ended

	    		// attach it to its proper hierarchy container
	    		if( !categoryStack.peek().isDefault() )
	    		{
	    			// add category to the category map
		    		categoryMap.put( categoryStack.peek().getId(),
		    				categoryStack.peek() );

	    			if( categoryStack.size() == 1 )
	    			{
	    				// at the top level
	    				addTopLevelCategory( categoryStack.pop() );
	    			}
	    			else
	    			{
	    				// not at the top level
	    				// determine hierarchy depth
	    				if( hierarchyDepth < categoryStack.size() )
	    				{
	    					hierarchyDepth = categoryStack.size();
	    				}

	    				// add current subcategory to parent category
	    				BasicSearchCategory subcategory = categoryStack.pop();
	    				BasicSearchCategory parentCategory = categoryStack.pop();
	    				parentCategory.addSubcategory( subcategory );
	    				categoryStack.push( parentCategory );
	    			}
	    		}
	    		else
	    		{
	    			// TODO this assumes the default category is outside of the
	    			// hierarchy (it is not attached to any parent container)
	    			defaultCategory = categoryStack.pop();
	    		}
	    	}

			/*
			 * category_database elements
			 */
			else if( endElement.equals( "id" ) )
			{
				currentDatabaseId = text;

				// is this database recommended?
				if( recommendedDatabaseFlag )
				{
					recommendedDatabaseFlag = false;
					BasicSearchCategory temp = categoryStack.pop();
					temp.addRecommendedDatabase( text );
					categoryStack.push( temp );
				}
				else
				{
					BasicSearchCategory temp = categoryStack.pop();
					temp.addDatabase( text );
					categoryStack.push( temp );
				}
			}
			else if( endElement.equals( "alt_name" ) )
			{
				currentDatabase = new BasicSearchDatabase( text,
						currentDatabaseId );
			}
			else if( endElement.equals( "alt_description" ) )
			{
				currentDatabase.updateDescription( text );
			}
			else if( endElement.equals( "category_database" ) )
			{
				if( currentDatabase != null )
				{
					BasicSearchCategory temp = categoryStack.pop();
					temp.addAlternateDatabase( currentDatabase );
					categoryStack.push( temp );
					currentDatabase = null;
				}
			}

	    	/*
	    	 * database elements
	    	 */
			else if( endElement.equals( "database_description" ) )
			{
				currentDatabase.updateDescription( text );
			}
			else if( endElement.equals( "database_group" ) )
			{
				currentDatabase.addGroup( text );
			}
			else if( endElement.equals( "database" ) )
			{
				// a database has just ended - add to databaseMap
				databaseMap.put( currentDatabase.getId(), currentDatabase );
				currentDatabase = null;
			}

			textBuffer = null;
	    }

		public SearchCategory getCategory( String categoryId ) {
			if( categoryId.equals( defaultCategory.getId() ) )
			{
				return defaultCategory;
			}
			else
			{
				return categoryMap.get( categoryId );
			}
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#getNumLevels()
		 */
		public int getNumLevels() {
			return hierarchyDepth;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#getNumMaxSearchableDb()
		 */
		public int getNumMaxSearchableDb()
		{
		  int number = m_configService.getSiteConfigMaximumSearchableDBs();

		  log.debug("getNumMaxSearchableDb() returns " + number);
			return number;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#getTopLevelCategories()
		 */
		public List<SearchCategory> getCategoryListing() {
			// return list
			List<SearchCategory> categoryListing = new java.util.ArrayList<SearchCategory>();

			// add root category to return list
			categoryListing.add( rootCategory );

			// iterate through all categories (starting at root)
			// and add them to the return list
			for( int i = 0; i <= categoryMap.size(); i++ )
			{
				SearchCategory category = categoryListing.get( i );

				if( category.hasSubCategories() )
				{
					for( SearchCategory cat : category.getSubCategories() )
					{
						categoryListing.add( cat );
					}
				}
			}

			return categoryListing;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#getRepository()
		 */
		public Repository getRepository()
		{
			Repository repository = null;

			// get a RepositoryManager
			RepositoryManager repositoryManager = null;
			try
			{
				repositoryManager = ( RepositoryManager )
				SakaiOsidLoader.getManager(
						"org.osid.repository.RepositoryManager",
						repositoryPkgName,
						new OsidContext(), null );

				RepositoryIterator rit = null;
				if( repositoryManager == null )
				{
					log.warn( "getRepository() failed getting RepositoryManager from SakaiOsidLoader" );
				}
				else
				{
					rit = repositoryManager.getRepositoriesByType( repositoryType );
				}

				// get repositories of type sakaibrary/repository/metasearch
				if( rit == null )
				{
					log.warn( "getRepository() failed getting RepositoryIterator of type sakaibrary/repository/metasearch from RepositoryManager" );
					return null;
				}
				else
				{
					// only one repository should be in the iterator
					repository = rit.nextRepository();
					String extendedId = m_configService.getSiteConfigExtendedRepositoryId();

					if ((extendedId != null) && (extendedId.length() > 0))
					{
						while (repository != null)
						{
							log.debug("Matching Repositories? "
									+ repository.getId().getIdString()
									+ " VS "
									+ extendedId);

							if (repository.getId().getIdString().equals(extendedId))
							{
								break;
							}
							repository = rit.nextRepository();
						}
					}
				}

				if( repository == null )
				{
					log.warn( "getRepository() failed getting repository from RepositoryIterator" );
				}
			}
			catch( OsidException oe )
			{
				log.warn( "getRepository threw OsidException: ", oe );
			}

			return repository;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#getDefaultCategory()
		 */
		public SearchCategory getDefaultCategory() {
			return defaultCategory;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.citation.api.SearchDatabaseHierarchy#isSearchableDatabase(java.lang.String)
		 */
		public boolean isSearchableDatabase(String databaseId) {
			return databaseMap.containsKey( databaseId );
		}

		public boolean isConfigured() {
			return isConfigured;
		}

	}  // public class BasicSearchDatabaseHierarchy

	// our ConfigurationService (gets set in BaseSearchDatabaseHierarchy)


	// google scholar constants
	public static final String SAKAI_SESSION = "sakai.session.key";
	public static final String SAKAI_KEY = "sakai.key";
	public static final String SAKAI_HOST = "sakai.host";

	//public static final String SERVLET_NAME = "savecite";
	public static final String SERVLET_NAME = "sakai-citations-servlet";
	public static final String WINDOW_PREFIX = "WebLearn Solo - ";

	// Our types (defined in setupTypes())
	protected static BasicType categoryAssetType;
	protected static BasicType databaseAssetType;
	protected static BasicType searchType;
	protected static BasicType repositoryType;

	// String array for databases being searched and database hierarchy
	protected Map<String, String> hierarchyMap = new HashMap<String, String>();
	protected SortedSet<String>   updatableResources = Collections.synchronizedSortedSet(new TreeSet<String>());

	private static Random m_generator;

	/*
	 * necessary services and managers (provided by components.xml)
	 */
	protected SessionManager m_sessionManager = null;
	protected ConfigurationService m_configService = null;
	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

	protected String databaseHierarchyResourceRef;

	private MemoryService memoryService;

	private Cache sessionContextCache;

	private Cache metasearchSessionManagerCache;

	public void setSessionManager(SessionManager sessionManager)
	{
		m_sessionManager = sessionManager;
	}

	public void setConfigurationService( ConfigurationService configService )
	{
		m_configService = configService;
	}

	public void destroy()
	{
		log.info("BaseSearchManager.destroy()");
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.impl.SearchManager#doNextPage(org.sakaiproject.citation.api.ActiveSearch)
     */
	public ActiveSearch doNextPage(ActiveSearch search)
	throws SearchException
	{
		Repository repository = ((BasicSearch) search).getRepository();
		AssetIterator assetIterator = ((BasicSearch) search).getAssetIterator();
		int last = search.getLastRecordIndex();

		CitationCollection citations = search.getSearchResults();
		if(citations == null)
		{
			citations = CitationService.getTemporaryCollection();
			((BasicSearch) search).setSearchResults(citations);
		}

		Set duplicateCheck = ((BasicSearch) search).getDuplicateCheck();
    int duplicateCount = 0;

		boolean done = false;
		boolean moreResults = false;

		try
		{
   		// poll until we get pageSize results (or run out of results)
   	  moreResults = assetIterator.hasNextAsset();
			while( !done && moreResults )
			{
				try
				{
					Asset asset = assetIterator.nextAsset();

					Citation citation = CitationService.getTemporaryCitation(asset);

					String dupCheckCriteria = citation.hasPreferredUrl()
					                          ? citation.getPrimaryUrl()
					                          : citation.getOpenurlParameters();

			    log.debug("DUP CHECK: " + dupCheckCriteria);

					if (((BasicSearch) search).isDuplicateCheckEnabled() &&
					      duplicateCheck.contains(dupCheckCriteria))
					{
 					  log.debug("Duplicate #" + (duplicateCount + 1) + " found");
					  if (duplicateCount++ >= MAX_DUPLICATES)
					  {
					    ((BasicSearch) search).setDuplicateCheckEnabled(false);
					  }
            // make sure we have more search results
         	  moreResults = assetIterator.hasNextAsset();
						continue;
					}
					else
					{
						((BasicSearch) search).m_pageOrder.add(citation.getId());
						citations.add(citation);

						duplicateCheck.add(dupCheckCriteria);
						duplicateCount = 0;
					}

					// check if we've got enough to return
					done = (citations.size() >= last);
				}
				catch( RepositoryException re )
				{
					if( re.getMessage().equals( SESSION_TIMED_OUT ) ||
							re.getMessage().equals( METASEARCH_ERROR ) ||
							re.getMessage().equals( SharedException.NO_MORE_ITERATOR_ELEMENTS ) ||
							re.getMessage().equals( OsidException.OPERATION_FAILED ) )
					{
						// search is over, all assets that have been retrieved have been
						// optionally check searchStatus Properties for further details or information to present in UI
						search.setLastPage(true);

						log.warn("doNextPage -- RepositoryException nextAsset(): " + re.getMessage());

						String message = getSearchStatusMessage(repository);
						if(message == null)
						{
							throw new SearchException(re.getMessage());
						}

						throw new SearchException( message );
					}
					else if( re.getMessage().equals( ASSET_NOT_FETCHED ) )
					{
						// need to wait some time and then try again
						try
						{
							Thread.sleep( 2500 );  // sleep 2.5 seconds
						}
						catch( InterruptedException ie )
						{
							search.setLastPage(true);

							log.warn("doNextPage -- InterruptedException nextAsset(): ", ie);

    						String message = getSearchStatusMessage(repository);

							throw new SearchException( message );
						}
					}
				}
        // make sure we have more search results
    	  moreResults = assetIterator.hasNextAsset();
			}
		}
		catch( RepositoryException re )
		{
			if( re.getMessage().equals( SESSION_TIMED_OUT ) ||
				re.getMessage().equals( METASEARCH_ERROR ) )
			{
				search.setLastPage(true);

				// search is over, all assets that have been retrieved have been
				// optionally check searchStatus Properties for further details or information to present in UI
				log.warn("doNextPage -- RepositoryException hasNextAsset(): " + re.getMessage());

				String message = getSearchStatusMessage(repository);

				throw new SearchException( message );
			}
		}

		// get search status properties
		Type statusType = getPropertyType( repository );
		org.osid.shared.Properties statusProperties = null;

		try
		{
			statusProperties = repository.getPropertiesByType( statusType );
		}
		catch( RepositoryException re )
		{
			search.setLastPage(true);

			String message = getSearchStatusMessage(repository);

			throw new SearchException( message );
		}

		Integer numRecordsFound = null;
		Integer numRecordsFetched = null;
		Integer numRecordsMerged = null;
		try
		{
			numRecordsFound = ( Integer ) statusProperties.getProperty("numRecordsFound");
			numRecordsFetched = ( Integer ) statusProperties.getProperty("numRecordsFetched");
			numRecordsMerged = ( Integer ) statusProperties.getProperty("numRecordsMerged");
		}
		catch( SharedException se )
		{
			search.setLastPage(true);

			String message = getSearchStatusMessage(repository);

			throw new SearchException( message );
		}

		search.setNumRecordsFound( numRecordsFound );
		search.setNumRecordsFetched( numRecordsFetched );
		search.setNumRecordsMerged( numRecordsMerged );
		search.setNewSearch(false);
		search.setFirstPage(false);
    /*
     * disable the "next page" arrow if we've exhausted the search results
     */
  	if (!moreResults)
  	{
  	  search.setLastPage(true);
  	}
  	else if(done)
		{
			search.setLastPage(false);
		}
		else
		{
			search.setLastPage(true);
		}
		return search;
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.impl.SearchManager#doPrevPage(org.sakaiproject.citation.api.ActiveSearch)
     */
	public ActiveSearch doPrevPage(ActiveSearch search)
		throws SearchException
	{
		return search;
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.impl.SearchManager#doSearch(org.sakaiproject.citation.api.ActiveSearch)
     */
	public ActiveSearch doSearch(ActiveSearch search)
		throws SearchException, SearchCancelException
	{
		// search parameters
		Integer    pageSize       = search.getPageSize();
		Integer    startRecord    = search.getStartRecord();
		String     sortBy         = search.getSortBy();
		String     guid           = search.getSearchId();
		String[]   searchDbs      = search.getDatabaseIds();

		/*
		 * Repository set up
		 */
		SearchDatabaseHierarchy hierarchy = getSearchHierarchy();
		if (hierarchy == null)
		{
			throw new SearchException("ERROR: No appropriate database hierarchy available");
		}
		Repository repository = hierarchy.getRepository();

		// CQL search query setup
		String cqlQuery = null;
		CQLSearchQuery cqlSearch = new org.sakaiproject.citation.util.impl.CQLSearchQuery();

		// determine whether this is an advanced or basic search
		if( search.getSearchType().equalsIgnoreCase( ActiveSearch.BASIC_SEARCH_TYPE ) )
		{
			// get search criteria in CQL
			cqlQuery = cqlSearch.getCQLSearchQueryString( search.getBasicQuery() );
		}
		else
		{
			// get search criteria in CQL
			cqlQuery = cqlSearch.getCQLSearchQueryString( search.getAdvancedQuery() );
		}
		log.debug( "CQL query: " + cqlQuery );

		// initiate the search
		try
		{
			if( cqlQuery == null )
	    	{
	    		// something went horrible
	    		throw new SearchException( "ERROR: could not properly " +
	    		"convert search criteria to CQL." );
	    	}

	    	// set up search properties
	    	java.util.Properties properties = new java.util.Properties();
	    	properties.put( "guid", guid );
	    	properties.put( "baseUrl", m_configService.getSiteConfigMetasearchBaseUrl() );
	    	properties.put( "username", m_configService.getSiteConfigMetasearchUsername() );
	    	properties.put( "password", m_configService.getSiteConfigMetasearchPassword() );
	    	properties.put( "sortBy", sortBy );
	    	properties.put( "pageSize", pageSize );
	    	properties.put( "startRecord", startRecord );

	    	// put selected databases
	    	List<String> databaseIds = new java.util.ArrayList<String>();
	    	for( String databaseId : searchDbs )
	    	{
	    		databaseIds.add( databaseId );
	    	}
	    	properties.put( "databaseIds", databaseIds );

	    	// create OSID Properties
	    	org.osid.shared.Properties searchProperties = new BasicSearchProperties( properties );

	    	// get "sakaibrary / search / asynchMetasearch" search type
	    	Type searchType = getSearchType( repository );

	    	// call getAssetsBySearch
	    	AssetIterator assetIterator =
	    		repository.getAssetsBySearch( cqlQuery, searchType,
	    				searchProperties );

	    	CitationCollection citations = search.getSearchResults();
	    	if(citations == null)
	    	{
	    		citations = CitationService.getTemporaryCollection();
	    		((BasicSearch) search).setSearchResults(citations);
	    	}

	    	Set duplicateCheck  = ((BasicSearch) search).getDuplicateCheck();
	    	int duplicateCount  = 0;
	    	int assetsRetrieved = 0;

	    	boolean done = false;
	    	boolean moreResults = false;

	    	try
	    	{
	    		// poll until we get pageSize results (or run out of results)
	    	  moreResults = assetIterator.hasNextAsset();
	    		while( !done && moreResults )
	    		{
	    			try
	    			{
	    				Asset asset = assetIterator.nextAsset();

	    				Citation citation = CitationService.getTemporaryCitation(asset);

    					String dupCheckCriteria = citation.hasPreferredUrl()
		    		  	                          ? citation.getPrimaryUrl()
				    	                            : citation.getOpenurlParameters();

					    log.debug("DUP CHECK: " + dupCheckCriteria);

	    				if (((BasicSearch) search).isDuplicateCheckEnabled() &&
	    						duplicateCheck.contains(dupCheckCriteria))
	    				{
	    					log.debug("Duplicate #" + (duplicateCount + 1) + " found");
	    					if (duplicateCount++ >= MAX_DUPLICATES)
	    					{
	    						((BasicSearch) search).setDuplicateCheckEnabled(false);
	    					}
                // make sure we have more search results
             	  moreResults = assetIterator.hasNextAsset();
	    					continue;
	    				}
	    				else
	    				{
	    					((BasicSearch) search).m_pageOrder.add(citation.getId());
	    					citations.add(citation);

	    					duplicateCheck.add(dupCheckCriteria);
	    					duplicateCount = 0;
	    				}

	    				// check if we've got enough to return
	    				if( ++assetsRetrieved >= pageSize.intValue() )
	    				{
	    					done = true;
	    				}
	    			}
	    			catch( RepositoryException re )
	    			{
	    				if( re.getMessage().equals( SESSION_TIMED_OUT ) ||
	    						re.getMessage().equals( METASEARCH_ERROR ) ||
	    						re.getMessage().equals( SharedException.NO_MORE_ITERATOR_ELEMENTS ) ||
	    						re.getMessage().equals( OsidException.OPERATION_FAILED ) )
	    				{
	    					search.setLastPage(true);

	    	    			search.resetSearch();


	    					// search is over, all assets that have been retrieved have been
	    					// optionally check searchStatus Properties for further details or information to present in UI

	    					String message = getSearchStatusMessage(repository);

	    					log.warn("doSearch -- RepositoryException nextAsset(): " + re.getMessage());
	    					throw new SearchException( message );
	    				}
	    				else if( re.getMessage().equals( ASSET_NOT_FETCHED ) )
	    				{
	    					// need to wait some time and then try again
	    					try
	    					{
	    						Thread.sleep( 2500 );  // sleep 2.5 seconds
	    					}
	    					catch( InterruptedException ie )
	    					{
	    						// search canceled
	    						throw new SearchCancelException();
	    					}
	    				}
	    			}
            // make sure we have more search results
		    	  moreResults = assetIterator.hasNextAsset();
	    		}
	    	}
	    	catch( RepositoryException re )
	    	{
	    		if( re.getMessage().equals( SESSION_TIMED_OUT ) ||
	    				re.getMessage().equals( METASEARCH_ERROR ) )
	    		{
	    			search.setLastPage(true);

	    			// search is over, all assets that have been retrieved have been
	    			// optionally check searchStatus Properties for further details or information to present in UI
	    			String message = getSearchStatusMessage(repository);

	    			log.warn("doSearch -- RepositoryException hasNextAsset(): " + re.getMessage());
	    			throw new SearchException( message );
	    		}
	    	}

	    	// get search status properties
	    	Type statusType = getPropertyType( repository );
	    	org.osid.shared.Properties statusProperties =
    		repository.getPropertiesByType( statusType );

	    	Integer numRecordsFound = null;
	    	Integer numRecordsFetched = null;
	    	Integer numRecordsMerged = null;
	    	try
	    	{
	    		numRecordsFetched = ( Integer ) statusProperties.getProperty(
	    		"numRecordsFetched" );
	    		numRecordsFound = ( Integer ) statusProperties.getProperty(
	    		"numRecordsFound" );
	    		numRecordsMerged = ( Integer ) statusProperties.getProperty(
	    		"numRecordsMerged" );
	    	}
	    	catch( SharedException se )
	    	{
	    		search.setLastPage(true);

	    		String message = getSearchStatusMessage(repository);

	    		throw new SearchException( message );
	    	}

	    	/*
	    	 * forward results handling
	    	 */
	    	search.setNumRecordsFound( numRecordsFound );
	    	search.setNumRecordsFetched( numRecordsFetched );
	    	search.setNumRecordsMerged( numRecordsMerged );

	    	search.setNewSearch(false);
	    	search.setFirstPage(false);
        /*
         * disable the "next page" arrow if we've exhausted the search results
         */
	    	if (!moreResults)
	    	{
	    	  search.setLastPage(true);
	    	}
	    	else
	    	{
	    	  search.setLastPage(!done);
        }
	    	((BasicSearch) search).setRepository(repository);
	    	((BasicSearch) search).setAssetIterator(assetIterator);

	    	return search;
	    }
	    catch( RepositoryException re )
		{
			log.warn("doSearch -- RepositoryException: " + re.getMessage());
			throw new SearchException( re.getMessage() );
		}
	}

	protected String newSearchId()
	{
		/******* A unique ID per-session ********/

		return m_sessionManager.getCurrentSession().getId();

		/******* Unique ID per-search (original)

		String sessionId = m_sessionManager.getCurrentSession().getId();
		long number = m_generator.nextLong();
		String hexString = Long.toHexString(number);
		log.debug("getSearchId:  " + sessionId + hexString);
		return sessionId + hexString;

		 *************************************************************************/
	}

	protected Type getPropertyType(Repository repository)
		throws SearchException
	{
		TypeIterator propertyTypes = null;
		Type propertyType = null;

		try
		{
			propertyTypes = repository.getPropertyTypes();

			while( propertyTypes.hasNextType() )
			{
				Type tempType = propertyTypes.nextType();
				if( tempType.getAuthority().equals( "sakaibrary" ) &&
						tempType.getDomain().equals( "properties" ) &&
						tempType.getKeyword().equals( "metasearchStatus" ) )
				{
					propertyType = tempType;
					break;
				}
			}
		}
		catch( OsidException oe )
		{
			log.warn("getPropertyType -- OsidException: " + oe.getMessage());
			throw new SearchException( "ERROR in getting search types: " + oe.getMessage() );
		}

		return propertyType;
	}

	protected Type getCategoryType( Repository repository ) throws SearchException
	{
		TypeIterator assetTypes = null;

		try
		{
			assetTypes = repository.getAssetTypes();

			while( assetTypes.hasNextType() )
			{
				Type tempType = assetTypes.nextType();
				if( tempType.isEqual( categoryAssetType ) )
				{
					return tempType;
				}
			}
		}
		catch( OsidException oe )
		{
			log.warn("getCategoryType -- OsidException: ", oe);
			throw new SearchException( "ERROR in getting category type: " + oe.getMessage() );
		}

		return null;
	}

	protected Type getSearchType( Repository repository ) throws SearchException
	{
		TypeIterator searchTypes = null;

		try
		{
			searchTypes = repository.getSearchTypes();

			while( searchTypes.hasNextType() )
			{
				Type tempType = searchTypes.nextType();
				if( tempType.isEqual( searchType ) )
				{
					return tempType;
				}
			}
		}
		catch( OsidException oe )
		{
			log.warn("getSearchType -- OsidException: ", oe);
			throw new SearchException( "ERROR in getting search types: " + oe.getMessage() );
		}

		return null;
	}

	protected String getSearchStatusMessage( Repository repository ) throws SearchException
	{
		BasicType statusType = new BasicType("sakaibrary", "properties", "metasearchStatus");

		String message = null;
		try
		{
			org.osid.shared.Properties statusProperties = repository.getPropertiesByType(statusType);

			message = (String) statusProperties.getProperty("statusMessage");
		}
		catch(RepositoryException e)
		{
			// let the message remain null but log this exception
			log.warn("getSearchStatusMessage RepositoryException getting properties " + e.getMessage());
		}
		catch(SharedException e)
		{
			// let the message remain null but log this exception
			log.warn("getSearchStatusMessage SharedException getting property " + e.getMessage());
		}

		return message;
	}

	public void init()
	{



		sessionContextCache = memoryService.getCache("org.sakaiproject.citation.api.SearchManager.sessionContextCache");
		SessionContext.setCache(sessionContextCache);

		metasearchSessionManagerCache = memoryService.getCache("org.sakaiproject.citation.api.SearchManager.metasearchSessionManagerCache");
		MetasearchSessionManager.setCache(metasearchSessionManagerCache);


		log.info("BaseSearchManager.init()");

		EventTrackingService.addObserver(this);

		long seed = TimeService.newTime().getTime();
		m_generator = new Random(seed);
		setupTypes();

		String configFolderRef = m_configService.getConfigFolderReference();
		Collection<String> hierarchyIds = m_configService.getAllCategoryXml();
		for(String hierarchyId : hierarchyIds)
		{
			this.updateHierarchy(configFolderRef + hierarchyId);
			this.updatableResources.add(configFolderRef + hierarchyId);

		}
	}

	protected void setupTypes() {
		categoryAssetType = new BasicType( "sakaibrary", "asset", "category" );
		databaseAssetType = new BasicType( "sakaibrary", "asset", "database" );
		searchType = new BasicType( "sakaibrary", "search", "asynchMetasearch" );
		repositoryType = new BasicType( "sakaibrary", "repository", "metasearch" );
	}

	  /*
	   * Fetch the appropriate database hierarchy resource from our cache (a new
	   * hiearchy resource is created if none exists).
	   *
     * @see org.sakaiproject.citation.api.SearchManager#listRepositories()
     */
    public SearchDatabaseHierarchy getSearchHierarchy() throws SearchException
    {
    	SearchDatabaseHierarchy hierarchy;

      try
      {
      	String configFolderRef  = m_configService.getConfigFolderReference();
      	String hierarchyXml     = m_configService.getDatabaseHierarchyXml();

       	if(!isNull(configFolderRef) && !isNull(hierarchyXml))
      	{
          String hierarchyRef   = configFolderRef + hierarchyXml;
        	String xmlContent;

          /*
           * Look up the requested hierarchy
           */
          log.debug("Looking for hierarchy: " + hierarchyRef);
          synchronized (this)
          {
    	    	if ((xmlContent = this.hierarchyMap.get(hierarchyRef)) == null)
    	    	{
      	    	  return null;
     	    	}
          }
          /*
           * Set up the per-user database hierarchy
           */
  	    	hierarchy = new BasicSearchDatabaseHierarchy(xmlContent);
  	    	return hierarchy.isConfigured() ? hierarchy : null;
      	}
      }
      catch (OsidConfigurationException exception)
      {
        log.warn("Failed to get configuration details: " + exception);
      }
     	return null;
    }

  public synchronized void updateHierarchy(String databaseXmlReference)
  {
    try
    {
      String xmlContent = getResourceContent(databaseXmlReference);

      if (xmlContent != null)
      {
  		  this.hierarchyMap.put(databaseXmlReference, xmlContent);
      }
    }
    catch (Exception exception)
    {
      log.warn("Failed to load "
              +  databaseXmlReference
              +  " (no changes made): "
              +  exception);
    }
  }

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.api.SearchManager#newSearch(java.lang.String)
     */
    public ActiveSearch newSearch()
    {
	    return new BasicSearch();
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.api.SearchManager#newSearch(java.lang.String)
     */
    public ActiveSearch newSearch(CitationCollection savedResults)
    {
	    return new BasicSearch(savedResults);
    }

	protected boolean paramIsEmpty( String param )
	{
		return param.trim().equals("");
	}

    /**
     *
     */
    public String getGoogleScholarUrl(String resourceId)
    {
    	String serverUrl = serverConfigurationService.getServerUrl();
 		//SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
		//String sessionId = sessionManager.getCurrentSession().getId();

		try
		{
			return ( m_configService.getSiteConfigGoogleBaseUrl()
					+ "?sciui=2&as_sdt=0,15&"
					+ "linkurl_base="
					+ java.net.URLEncoder.encode( serverUrl +
							Entity.SEPARATOR +
							SERVLET_NAME +
							Entity.SEPARATOR +
							resourceId +
							"?" +
							SAKAI_SESSION +
							"=nada&", "UTF-8" )
					+ "&linkurl_id="
					+ java.net.URLEncoder.encode( m_configService.getSiteConfigSakaiServerKey(), "UTF-8" ) );
		}
		catch( Exception e )
		{
			log.warn( "getGoogleScholarUrl encoding failed", e );
			return null;
		}
    }

    /**
     * Supply the url for the savecite servlet to add a citation to a particular citation list.
     * @param resourceId The identifier for the citation list.
     */
	public String getSaveciteUrl(String resourceId, String saveciteClientId) {
    	StringBuilder buf = new StringBuilder();
    	
    	String serverUrl = serverConfigurationService.getServerUrl();
    	buf.append(serverUrl);
    	buf.append(Entity.SEPARATOR);
    	buf.append(SERVLET_NAME);
    	buf.append(Entity.SEPARATOR);
    	buf.append(resourceId);
    	buf.append('?');
    	buf.append(SAKAI_SESSION);
    	buf.append("=nada&client=");
    	buf.append(saveciteClientId);
    	//buf.append("&");
    	
    	return buf.toString();
    }

    public String getExternalSearchWindowName(String resourceId)
    {
        String serverUrl = serverConfigurationService.getServerUrl() + Entity.SEPARATOR + SERVLET_NAME + Entity.SEPARATOR + resourceId;
        try {
            String encodedUrl = URLEncoder.encode(serverUrl, "UTF-8");
            return WINDOW_PREFIX + encodedUrl;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Does anywhere not have UTF-8?", e);
        }
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
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
    {
    	this.serverConfigurationService = serverConfigurationService;
    }


	public void update(Observable arg0, Object arg1)
    {
	    if (arg1 instanceof Event)
	    {
	    	Event event = (Event) arg1;
	    	/*
	    	 * Modified?  If so, reload if it's one of our hierarchy files
	    	 */
	    	if (event.getModify())
	    	{
  	    	String refstr = event.getResource();

          synchronized (this)
          {
    	    	if (this.updatableResources.contains(refstr))
    	    	{
              log.debug("Updating configuration from " + refstr);
    	    		this.updateHierarchy(refstr);
    	    	}
          }
        }
	    }
    }

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected SecurityAdvisor enableSecurityAdvisor()
	{
		SecurityAdvisor advisor = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		};
		// put in a security advisor so we can create citationAdmin site without need
		// of further permissions
		SecurityService.pushAdvisor(advisor);
		return advisor;
	}

  /**
   * Fetch the content from a Resource
   * @param resource Content hosting resource
   */
  public String getResourceContent(ContentResource resource)
                                   throws IOException, ServerOverloadException
  {
    InputStream   input     = resource.streamContent();
    StringBuilder content   = new StringBuilder();
    byte[]        bytesIn   = new byte[1024 * 8];
    int           count;

    while ((count = input.read(bytesIn)) != -1)
    {
      content.append(new String(bytesIn, 0, count, "UTF-8"));
    }
    return content.toString();
  }

  /**
   * Fetch content from a resource reference (a named resource)
   * @param resourceReference Resource reference
   * @return Resource content (null if none)
   */
  public String getResourceContent(String resourceReference)
                                               throws IOException,
                                                      IdUnusedException,
                                                      PermissionException,
                                                      ServerOverloadException,
                                                      TypeException
  {
	String content = null;
    Reference reference = EntityManager.newReference(resourceReference);
	if (reference == null)
	{
	  return null;
	}

	SecurityAdvisor pushed = enableSecurityAdvisor();
	try {
		ContentResource resource = ContentHostingService.getResource(reference.getId());
		if(resource != null) {
			content = getResourceContent(resource);
		}
	} catch(Exception e) {
		log.warn("getReourceContent() " + e);
	} finally {
		if(pushed != null) {
			boolean found = false;
			while(SecurityService.hasAdvisors() && ! found) {
				SecurityAdvisor popped = SecurityService.popAdvisor();
				found = popped == pushed;
			}
		}
	}
	
	return content;
  }

  /**
   * Null (or empty) String?
   * @param string String to check
   * @return true if so
   */
	private boolean isNull(String string)
	{
		return (string == null) || (string.trim().equals(""));
	}

	/**
	 * @return the memoryService
	 */
	public MemoryService getMemoryService()
	{
		return memoryService;
	}

	/**
	 * @param memoryService the memoryService to set
	 */
	public void setMemoryService(MemoryService memoryService)
	{
		this.memoryService = memoryService;
	}
}
