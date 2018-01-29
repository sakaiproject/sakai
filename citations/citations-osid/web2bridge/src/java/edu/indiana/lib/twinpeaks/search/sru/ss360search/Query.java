/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2008, 2009 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.sru.ss360search;

import edu.indiana.lib.twinpeaks.search.sru.CqlParser;
import edu.indiana.lib.twinpeaks.search.sru.SruQueryBase;
import edu.indiana.lib.twinpeaks.util.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

/**
 * Send a query to the Serials Solutions 360 Search server
 */
@Slf4j
public class Query extends SruQueryBase implements Constants
{
  /**
   * Display debug details (verbose)
   */
  private boolean DEBUG = false;
	/**
	 * Unique name for this search application
	 */
	private final String APPLICATION = SessionContext.uniqueSessionName(this);
	/**
	 * Records displayed "per page" (default value)
	 */
	public static final String RECORDS_PER_PAGE = "10";
	/**
	 * Default sort key
	 */
	public static final String DEFAULT_SORT_KEY = "date";   // "received";
	/**
	 * Database for this request
	 */
	private String _database = null;
	/**
	 * Search criteria for this request (see parseRequest())
	 */
	private String _searchString = null;
  /**
   * Start new search?
   */
  private boolean _newSearch = true;
  /**
   * Constructor
   */
	public Query()
	{
    super();
  }

	/**
	 * Parse user request parameters.
	 * @param parameterMap Request details (name=value pairs)
	 */
  public void parseRequest(Map parameterMap)
  {
    String action;

		super.parseRequest(parameterMap);
		/*
		 * These cannot be null by the time we get here
		 */
    if ((getRequestParameter("guid") == null)   ||
		    (getRequestParameter("url")  == null))
	  {
      throw new IllegalArgumentException("Missing GUID or URL");
    }
    /*
     * Now deal with the search criteria (CQL syntax)
     */
  	_searchString = parseCql(getRequestParameter("searchString"));
  }

	/**
	 * Search
	 */
	public void doQuery()
	{
		SessionContext  session;
		String		      action;
		Document        responseDocument;

    Integer p = getIntegerRequestParameter("pageSize");
    Integer r = getIntegerRequestParameter("startingRecord");
    log.debug("PageSize: " + p + ", Starting Record: " + r);


		/*
		 * We'll manage redirects
		 */
	  setRedirectBehavior(REDIRECT_MANAGED);
    /*
     * Pick up session context and request type
     */
		session = getSessionContext();
		action  = getRequestParameter("action");

		log.debug("Requested ACTION: " + action);
		/*
		 * Start a new search?
		 */
		if (action.equalsIgnoreCase("startSearch"))
		{
		  /*
			 * Initialize a new session context block
			 */
			StatusUtils.initialize(session, getRequestParameter("targets"));
			session.remove("resultSetId");
			/*
			 * Set up the initial search
			 */
  		clearParameters();
  		postNewSearch();
	    submit();

      responseDocument = getResponseDocument();
      displayXml(responseDocument);

      validateResponse(responseDocument);
      parseStatusRecord(responseDocument);

      flagSearchInProgress();
	    return;
    }
		/*
		 * Process results?
		 */
		if (action.equalsIgnoreCase("requestResults"))
	  {
		  /*
		   * Get additional results? (pagination)
		   */
	    if (isNewPage())
      {
        clearParameters();
        doNewPage();
        submit();

        responseDocument = getResponseDocument();
        displayXml(responseDocument);

        validateResponse(responseDocument);
    		String id = saveResultSetId(responseDocument);
      }
      flagInitialSearchComplete();
      return;
		}
		/*
		 * Unexpected action: log it and continue
		 */
		log.warn("Unexpected ACTION requested: \"" + action + "\"");
	}

  /**
   * Set up a URL for the new search (method = GET)
   *<ul>
   * <li> Set up the required SRU "searchRetrieve" parameters
   * <li> Set the global _url (the base URL plus all SRU parameters)
   *</ul>
   */
  protected void doNewSearch()
  {
    String  searchRetrieve;

    String  targets     = getRequestParameter("targets");
    String  sortKey     = getRequestParameter("sortBy");
    String  baseUrl     = getRequestParameter("url");
    int     pageSize    = getIntegerRequestParameter("pageSize");
    int     startRecord = getIntegerRequestParameter("startRecord");

    /*
     * Fix up the sort key
     */
	  sortKey = normalizeSortKey(sortKey);
    /*
     * searchRetrieve parameters
     */
    searchRetrieve = addFirstParameter(sruVersion(CS_SRU_VERSION));

    searchRetrieve = addParameter(searchRetrieve, sruSearchRetrieve());
    searchRetrieve = addParameter(searchRetrieve, sruRecordSchema(CS_SCHEMA));
    /*
     * Observationally, values > 20 are discarded - SRS, 05/20/08
     */
    searchRetrieve = addParameter(searchRetrieve, sruMaximumRecords(pageSize));
    /*
     * Including the start record causes an error in some cases (FDB)
     *
     *    searchRetrieve = addParameter(searchRetrieve, sruStartRecord(startRecord));
     */
    searchRetrieve = addParameter(searchRetrieve, sruSort(sortKey));

    searchRetrieve = addParameter(searchRetrieve, sruQuery(encode(_searchString)));
    searchRetrieve = addParameter(searchRetrieve, xcsDatabase(targets));
    /*
     * Set the initial search URL and method
     */
		setUrl(baseUrl + searchRetrieve);
  	setQueryMethod(METHOD_GET);
  }

  /**
   * Set up a URL for the new search (method = POST)
   *<ul>
   * <li> Set up the required SRU "searchRetrieve" parameters
   * <li> Set the global _url (the base URL plus all SRU parameters)
   *</ul>
   */
  protected void postNewSearch()
  {
    String  searchRetrieve;

    String  targets     = getRequestParameter("targets");
    String  sortKey     = getRequestParameter("sortBy");
    String  baseUrl     = getRequestParameter("url");
    int     pageSize    = getIntegerRequestParameter("pageSize");
    int     startRecord = getIntegerRequestParameter("startRecord");

    /*
     * Fix up the sort key
     */
    sortKey = normalizeSortKey(sortKey);
    /*
     * searchRetrieve parameters
     */
    sruPostVersion(CS_SRU_VERSION);

    sruPostSearchRetrieve();
    sruPostRecordSchema(CS_SCHEMA);
    /*
     * Observationally, values > 20 are discarded - SRS, 05/20/08
     */
    sruPostMaximumRecords(pageSize);
    /*
     * Including the start record causes an error in some cases (FDB)
     *
     *    sruPostStartRecord(startRecord));
     */
    sruPostSort(sortKey);

    sruPostQuery(_searchString);
    xcsPostDatabase(targets);
    /*
     * Set the initial search URL
     */
		setUrl(baseUrl);
  	setQueryMethod(METHOD_POST);
  }

  /**
   * Set up for pagination (method = GET)
   *<ul>
   * <li> Set up the required "searchRetrieve" parameters for pagination
   * <li> Set the global _url (the base URL)
   *</ul>
   */
  protected void doNewPage()
  {
   String  searchRetrieve;

    String  sortKey     = getRequestParameter("sortBy");
    String  baseUrl     = getRequestParameter("url");

    String  resultSetId = (String) getSessionContext().get("resultSetId");
		int     startRecord	= getSessionContext().getInt("startRecord");
		int     pageSize	  = getSessionContext().getInt("pageSize");

    log.debug("New Page: starting record = " + startRecord
            +  ", page size = " + pageSize);
    /*
     * Fix up the sort key
     */
	  sortKey = normalizeSortKey(sortKey);
    /*
     * searchRetrieve parameters
     */
    searchRetrieve = addFirstParameter(sruVersion(CS_SRU_VERSION));

    searchRetrieve = addParameter(searchRetrieve, sruSearchRetrieve());
    searchRetrieve = addParameter(searchRetrieve, sruRecordSchema(CS_SCHEMA));

    searchRetrieve = addParameter(searchRetrieve, sruMaximumRecords(pageSize));
    searchRetrieve = addParameter(searchRetrieve, sruStartRecord(startRecord));

    searchRetrieve = addParameter(searchRetrieve, sruSort(sortKey));

    searchRetrieve = addParameter(searchRetrieve, sruQuery("cql.resultSetId="
                                                  +         resultSetId));
    searchRetrieve = addParameter(searchRetrieve, xcsContinue());
    /*
     * Set the URL and method
     */
		setUrl(baseUrl + searchRetrieve);
  	setQueryMethod(METHOD_GET);
  }

  /**
   * Set up a URL for pagination (method = POST)
   *<ul>
   * <li> Set up the required "searchRetrieve" parameters for pagination
   * <li> Set the global _url (the base URL plus all pagination parameters)
   *</ul>
   */
  protected void postNewPage()
  {
   String  searchRetrieve;

    String  sortKey     = getRequestParameter("sortBy");
    String  baseUrl     = getRequestParameter("url");

    String  resultSetId = (String) getSessionContext().get("resultSetId");
		int     startRecord	= getSessionContext().getInt("startRecord");
		int     pageSize	  = getSessionContext().getInt("pageSize");

    log.debug("New Page: starting record = " + startRecord
            +  ", page size = " + pageSize);
    /*
     * Fix up the sort key
     */
    sortKey = normalizeSortKey(sortKey);
    /*
     * searchRetrieve parameters
     */
    sruPostVersion(CS_SRU_VERSION);

    sruPostSearchRetrieve();
    sruPostRecordSchema(CS_SCHEMA);

    sruPostMaximumRecords(pageSize);
    sruPostStartRecord(startRecord);
    /*
     * Observationally, using a sort key here will often cause the "continue"
     * to fail with a "no such result set" error from the server
     *
     *    sruPostSort(sortKey);
     */
    sruPostQuery("cql.resultSetId=" + resultSetId);
    xcsPostContinue();
    /*
     * Set the base URL and method.
     *
     * The sessionId parameter is required, even though we're POSTing this
     * request.  See the XML API documentation for details.
     */
		setUrl(baseUrl + "?sessionId=" + resultSetId);
  	setQueryMethod(METHOD_POST);
  }

	/*
	 * Helpers
	 */

  /**
   * Validate the server response
   * @param responseDocument The server response
   */
  private void validateResponse(Document responseDocument)
  {
    SessionContext  sessionContext = getSessionContext();
    Element         responseRoot   = responseDocument.getDocumentElement();
    Element         element;

    /*
     * Diagnostic record?  If present, this implies complete failure.
     */
		element = DomUtils.getElement(responseRoot, "diagnostic");
		if (element != null)
		{
		  String details  = DomUtils.getText(element, "details");
		  String message  = DomUtils.getText(element, "message");

		  if (details == null)
		  {
		    details = "<details not provided>";
      }

		  if (message == null)
		  {
		    message = "<message not provided>";
      }

			StatusUtils.setGlobalError(sessionContext, message, details);

			log.error("Diagnotic record found");
			displayXml(element);

			throw new SearchException(message + ", " + details);
		}
  }

  /**
   * Parse the status record.
   *
   * We establish the global search status block here, saving the result set
   * id, estimated hit count, etc.
   *
   * @param responseDocument The server response
   */
  protected void parseStatusRecord(Document responseDocument)
  {
    SessionContext  sessionContext = getSessionContext();
    Element         responseRoot   = responseDocument.getDocumentElement();
    Element         element;
    List            providerList;
    NodeList        counterList;
    int             active, total;
    /*
     * Save the result set ID
     */
		saveResultSetId(responseDocument);
    /*
		 * Examine the status record
		 */
    element = DomUtils.getElementNS(NS_CS, responseRoot, "searchProfile");

    providerList = DomUtils.selectElementsByAttributeValueNS(NS_CS, element,
                                                            "searchProfile",
                                                            "level",
                                                            "database");
    /*
     * No target databases?
     */
		if (providerList.isEmpty())
		{
		  String message = "No database specified for provider in 360 Search response";

      log.error(message);
      displayXml(element);

			throw new SearchException(message);
    }
    /*
     * Track statistics and status for each database
     */
    active = 0;
    total  = 0;

    for (int i = 0; i < providerList.size(); i++)
    {
      Element provider;
      String  target;
      Map     map;
      int     estimate, hits;
      /*
       * Set up a status map for this database (target)
       */
      provider  = (Element) providerList.get(i);
      target    = provider.getAttribute("id");

      map = StatusUtils.getStatusMapForTarget(sessionContext, target);
      if (map == null)
      {
        StatusUtils.initialize(sessionContext, target);
        map = StatusUtils.getStatusMapForTarget(sessionContext, target);
      }
      /*
       * Find the estimated and actual number of hits
       */
      element = DomUtils.selectFirstElementByAttributeValueNS(NS_CS, provider,
                                                              "citationCount",
                                                              "type", "total");
			estimate = Integer.parseInt(DomUtils.getText(element));

      element = DomUtils.selectFirstElementByAttributeValueNS(NS_CS, provider,
                                                              "citationCount",
                                                              "type", "partial");
			hits = Integer.parseInt(DomUtils.getText(element));

      log.debug("*** Estimated hits: " + estimate + ", actual hits: " + hits);
      /*
       * Set up the status map for the current provider.  The provider is active
       * only when the estimated and actual hit counts are both available.
       */
			map.put("ESTIMATE", "0");
			map.put("STATUS", "DONE");

			if ((estimate > 0) && (hits > 0))
			{
  			total += estimate;
  			map.put("ESTIMATE", String.valueOf(estimate));

				map.put("STATUS", "ACTIVE");
  			active++;
	  	}
	  	log.debug("Database details: " + map);
    }
		/*
		 * Save:
		 * 1) The largest number of records we could possibly return
		 * 2) The count of "in progress" searches
		 */
		sessionContext.put("maxRecords", String.valueOf(total));
		sessionContext.putInt("active", active);
  }

  /**
   * Look up the result set ID (and save it in session state)
   *
   * @param responseDocument The server response
   * @return The ID
   */
  protected String saveResultSetId(Document responseDocument)
  {
    SessionContext  sessionContext = getSessionContext();
    Element         responseRoot   = responseDocument.getDocumentElement();
    String          resultSetId;
    String          previousId;
    /*
     * Find the result set ID
     */
		resultSetId = DomUtils.getTextNS(NS_SRW, responseRoot, "resultSetId");
    if (StringUtils.isNull(resultSetId))
    {
      String message = "No result set id in 360 Search response";

      log.error(message);
      throw new SearchException(message);
    }
    /*
     * DEBUG: Did the result set ID change?
     */
    previousId = (String) sessionContext.get("resultSetId");

    if (!resultSetId.equals(previousId))
    {
      log.debug("*** Result set ID changed.  Was: "
              +  previousId
              +  ", now: "
              + resultSetId);
    }
    /*
     * Save the (globally applicable) result set ID
     */
    sessionContext.put("resultSetId", resultSetId);
    return resultSetId;
  }

	/**
	 * Custom submit behavior (override HttpTransactionQueryBase)
	 */
	public int submit()
	{
		return super.submit();
	}

	/**
	 * URL encode a parameter value (UTF-8)
	 * @param value Parameter value to encode
	 * @return The [possibly] encoded value
   */
  private String encode(String value)
  {
    try
    {
      return URLEncoder.encode(value, "UTF-8");
    }
    catch (UnsupportedEncodingException exception)
    {
      log.error("UTF-8: " + exception);

      return value;
    }
  }

  /**
   * Fix up the sort key
   */
  private String normalizeSortKey(String sortKey)
  {
    return "received";
  }

  /*
   * 360 Search GET parameters
   */

  /**
   * Make the database list (SRU extension)
   * @param databaseList The list of databases
   * @return A fully formed database parameter
   */
  protected String xcsDatabase(String databaseList)
  {
    return formatParameter(CS_DATABASES, formatDatabaseList(databaseList));
  }

  /**
   * Make a 360 Search "continue" parameter
   * @return The continue parameter
   */
  protected String xcsContinue()
  {
    return formatParameter(CS_ACTION, CS_CONTINUE);
  }

  /*
   * 360 Search POST parameters
   */

  /**
   * Make the database list (SRU extension)
   * @param databaseList The list of databases
   */
  protected void xcsPostDatabase(String databaseList)
  {
    setParameter(CS_DATABASES, formatDatabaseList(databaseList));
  }

  /**
   * Make a 360 Search "continue" parameter
   */
  protected void xcsPostContinue()
  {
    setParameter(CS_ACTION, CS_CONTINUE);
  }

  /**
   * Build up comma separated database list
   * @param databaseList Databases: <code>db1 bd2 bd3</code>
   * @return Comma separated list:  <code>db1,bd2,bd3</code>
   */
  protected String formatDatabaseList(String databaseList)
  {
    StringTokenizer parser          = new StringTokenizer(databaseList);
    StringBuilder   normalizedList  = new StringBuilder();
    String          separator       = "";

    while (parser.hasMoreTokens())
    {
      String db = parser.nextToken().trim();

      if (StringUtils.isNull(db))
      {
        continue;
      }

      normalizedList.append(separator);
      normalizedList.append(db);

      if (separator.length() == 0)
      {
        separator = ",";
      }
    }
    return normalizedList.toString();
  }

	/**
	 * Parse CQL search queries.
	 * @param cql String containing a cql query
	 * @return SRU/SRW search criteria
	 */
	private String parseCql(String cql) throws IllegalArgumentException
	{
		CqlParser	parser;
		String		result;

		log.debug("Initial CQL Criteria: " + cql);

		parser 	= new CqlParser();
		result	= parser.doCQL2MetasearchCommand(cql);

		log.debug("Processed Result: " + result);
		return result;
	}

	/**
	 * Display XML information
	 * @param xmlObject XML to display (Document or Element)
	 */
	private void displayXml(Object xmlObject)
	{
    if (!DEBUG) return;

		try
		{
			log.debug("{}", xmlObject);
		}
		catch (Exception ignore) { }
	}

  /*
   * New search helpers
   */

  /**
   * Is this a new page (pagination, not a brand new search)?
   * @return true if so
   */
  protected boolean isNewPage()
  {
    return !_newSearch;
  }

  /**
   * Clear the "start a new search" flag
   */
  protected void flagInitialSearchComplete()
  {
    _newSearch = false;
  }

  /**
   * Set the "start a new search" flag
   */
  protected void flagSearchInProgress()
  {
    _newSearch = true;
  }
}