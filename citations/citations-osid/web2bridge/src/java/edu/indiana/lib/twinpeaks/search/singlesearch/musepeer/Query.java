/**********************************************************************************
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
package edu.indiana.lib.twinpeaks.search.singlesearch.musepeer;

import edu.indiana.lib.twinpeaks.search.*;
import edu.indiana.lib.twinpeaks.search.singlesearch.CqlParser;
import edu.indiana.lib.twinpeaks.util.*;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

/**
 * Send a query to the Musepeer  interface
 */
@Slf4j
public class Query extends HttpTransactionQueryBase
{
	/**
	 * Records displayed "per page"
	 */
	public static final String RECORDS_PER_PAGE = "10";
	/**
	 * Records to fetch from each search target
	 */
	private static final String RECORDS_PER_TARGET = "50";
	/**
	 * Unique name for this search application
	 */
	private final String APPLICATION = SessionContext.uniqueSessionName(this);
	/**
	 * Error text: No logged-in session
	 */
	private static final String NO_SESSION = "Not logged on. Please logon first.";
	/**
	 * Database for this request
	 */
	private String 	_database;
	/**
	 * Muse syntax search criteria for this request (see parseRequest())
	 */
	private String _museSearchString;
  /**
   * Total number of target databases
   */
  private int _targetCount;
	/**
	 * Local ID (for the current transaction)
	 */
	private long _transactionId;

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
		String action, searchCriteria;

		super.parseRequest(parameterMap);
		/*
		 * These cannot be null by the time we get here
		 */
		if ((getRequestParameter("guid") == null)   ||
				(getRequestParameter("url")  == null))
		{
			throw new IllegalArgumentException("Missing GUID or URL");
		}

		action = getRequestParameter("action");
		log.debug("*** Beginning action: " + action);

		if ("startsearch".equalsIgnoreCase(action))
		{
			if ((getRequestParameter("targets")   == null)  ||
					(getRequestParameter("username")  == null)  ||
					(getRequestParameter("password")  == null))
			{
				throw new IllegalArgumentException("Missing target list, username, or password");
			}
  		/*
  		 * Now deal with the search criteria (CQL syntax)
  		 */
      searchCriteria = parseCql(getRequestParameter("searchString"));
  		getSessionContext().put("SEARCH_QUERY", searchCriteria);
    }
  }

	/**
	 * Search
	 */
	public void doQuery()
	{
		Document 	document;
		String		action;
		/*
		 * Get the logical "database" (a name for the configuration for this search)
		 */
		_database = getRequestParameter("database");
		/*
		 * We'll manage redirects, and submit with POST
		 */
		setRedirectBehavior(REDIRECT_MANAGED);
		setQueryMethod(METHOD_POST);
		/*
		 * Save the URL and query text
		 */
		setUrl(getRequestParameter("url"));
		setSearchString(getSearchString());
		/*
		 * New search?
		 */
		action = getRequestParameter("action");
		if (action.equalsIgnoreCase("startSearch"))
		{
		  /*
		   * Initialize a new search context block.  Augment the standard
		   * (synchronous) initialization with the necessary asynchronous
		   * setup (an asynchronous search with initialization in progress).
		   */
			StatusUtils.initialize(getSessionContext(), getRequestParameter("targets"));
			StatusUtils.setAsyncSearch(getSessionContext());
			StatusUtils.setAsyncInit(getSessionContext());
			/*
			 * LOGOFF any previous session
			 */
			clearParameters();

			doLogoffCommand();
			submit();

			setSessionId(null);
			displayXml("Logoff", getResponseDocument());
			/*
			 * LOGON
			 */
			clearParameters();

			doLogonCommand();
			submit();

			displayXml("Login", getResponseDocument());
			validateResponse("LOGON");
			/*
			 * SEARCH - on success, set a PROGRESS command time limit (in seconds)
			 */
			clearParameters();

			doSearchCommand();
			submit();

			displayXml("Search", getResponseDocument());
			validateResponse("SEARCH");

			setSearchStatusTimeout(60);
			return;
		}
    /*
     * PROGRESS
     *
     * Still doing asynchronous initialization?  If so, pick up the search
     * status.  Throw "no assets ready" (to try again) if the estimates aren't
     * available yet...
     */
    if (StatusUtils.doingAsyncInit(getSessionContext()))
    {
    	clearParameters();

  		doProgressCommand();
  	  submit();
  		displayXml("Progress command done", getResponseDocument());

  		validateResponse("PROGRESS");
		  if (!setSearchStatus(getResponseDocument()))
			{
			  throw new SearchException(SearchException.ASSET_NOT_READY);
			}
      /*
       * Asynchronous initialization is finished now.  If we found no hits,
       * loop one more time to let hasNextAsset() reflect that fact...
       */
 	    StatusUtils.clearAsyncInit(getSessionContext());

			if (StatusUtils.getActiveTargetCount(getSessionContext()) == 0)
			{
			  throw new SearchException(SearchException.ASSET_NOT_READY);
			}
		}
		/*
		 * NEXT or MORE
		 *
		 * Fetch additional results
		 */
		doResultsCommand();
		displayXml("Results", getResponseDocument());
	}

	/*
	 * MusePeer API Helpers
	 */

	/**
	 * Generate a LOGON command
	 */
	private void doLogonCommand() throws SearchException
	{
		String 	username, password;

		username = getRequestParameter("username");
		password = getRequestParameter("password");

		log.debug("Logging in as \"" + username + "\"");

		setParameter("action", "logon");

		setParameter("userID", username);
		setParameter("userPwd", password);

		setParameter("templateFile", "xml/index.xml");
		setParameter("errorTemplate", "xml/error.xml");
	}

	/**
	 * Generate a LOGOFF command
	 */
	private void doLogoffCommand() throws SearchException
	{
		setParameter("action", "logoff");
		setParameter("sessionID", getSessionId());

		setParameter("templateFile", "xml/index.xml");
		setParameter("errorTemplate", "xml/error.xml");
	}

	/**
	 * Generate a SEARCH command
	 */
	private void doSearchCommand() throws SearchException
	{
		StringTokenizer targetParser;
		String searchCriteria, searchFilter, targets;
		String pageSize, sessionId, sortBy;
		/*
		 * Set search criteria
		 */
		searchCriteria = getSearchString();
		log.debug("Search criteria: " + searchCriteria);
		/*
		 * Generate the search command
		 */
		setParameter("action",    "search");
		setParameter("xml",       "true");
		setParameter("sessionID", getSessionId());

		setParameter("queryStatement", searchCriteria);

		targets       = getRequestParameter("targets");
		targetParser  = new StringTokenizer(targets);
		_targetCount  = targetParser.countTokens();

		while (targetParser.hasMoreTokens())
		{
      String target  = targetParser.nextToken();

		  setParameter("dbList", target);

 			log.debug("SEARCH: added DB " + target);
		}
    /*
     * Start an asynchronous query (no results are returned) to set things up
     * for a subsequent PROGRESS (status) command
     */
    setParameter("limitsMaxPerSource", getPageSize());
		setParameter("limitsMaxPerPage",   "0");
    /*
     * Formatting
     */
		setFormattingFiles();
	}

	/**
	 * Generate a PROGRESS (status) command
	 */
	private void doProgressCommand() throws SearchException
	{
		setParameter("action",  "progress");
		setParameter("xml",     "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());

		setParameter("errorTemplate", "xml/error.xml");
		setParameter("errorFormat",   "error2XML.xsl");
	}

	/**
	 * Generate a pagination (NEXT or PREVIOUS) command
	 * @param page Pagination (<code>next</code> | <code>previous</code>)
	 * @param firstRecord First record to retrieve
	 */
	private void doPaginationCommand(String page, int firstRecord, int pageSize)
	{
    String    start = Integer.toString(firstRecord);
    String    total = Integer.toString(firstRecord - 1);

		log.debug("Using result set name \"" + getResultSetName() + "\"");
    /*
     * Action, identification
     */
		setParameter("action", page);
		setParameter("xml",   "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());
		setParameter("resultSet", getResultSetName());
    /*
     * Active database(s)
     */
    setDbList();
    /*
     * Record, page, host requirements
     */
    log.debug("PAGE: start = " + start
                +     ", first = " + start
                +     ", total = " + total
                +     ", pageSize = " + pageSize);

		setParameter("start", start);
		setParameter("firstRetrievedRecord", start);
  	setParameter("limitsMaxPerSource", String.valueOf(pageSize));
		setParameter("limitsMaxPerPage", String.valueOf(pageSize));
    /*
     * Formatting
     */
		setFormattingFiles();
	}

	/**
	 * Generate a MORE data command
	 * @param firstRecord First record to retrieve
	 * @param pageSize The number of results we want
	 */
	private void doMoreCommand(int firstRecord, int pageSize, int totalRemaining)
	{
    String    start = Integer.toString(firstRecord);
    String    first = Integer.toString(firstRecord - Math.min(pageSize, totalRemaining)); // pageSize);
    String    total = Integer.toString(firstRecord - 1);
    String    limit = Integer.toString(Math.min(pageSize, totalRemaining));

		log.debug("MORE: using result set name \"" + getResultSetName() + "\"");
		log.debug("MORE: queryStatement = " + getSearchString());

    /*
     * Action, identification
     */
		setParameter("action", "more");
		setParameter("actionType", "SEARCH");
		setParameter("xml", "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());
		setParameter("resultSet", getResultSetName());

		setParameter("queryStatement", getSearchString());
    /*
     * Active database(s)
     */
    setDbList();
    /*
     * Record, page, host requirements
     */
    log.debug("MORE: start = " + start
                +     ", first = " + first
                +     ", total = " + total
                +     ", pageSize = " + pageSize
                +     ", remaining = " + totalRemaining
                +     ", page limit = " + limit);

		setParameter("start", start);
    setParameter("firstRetrievedRecord", first);
    setParameter("limitsMaxPerSource", limit);
  	setParameter("limitsMaxPerPage", limit);
    /*
     * Formatting
     */
		setFormattingFiles();
  }

	/**
	 * Fetch more results
	 */
	private void doResultsCommand() throws SearchException
	{
 		int start           = getSessionContext().getInt("startRecord");
    int pageSize        = Integer.parseInt(getPageSize());
    int totalRemaining  = StatusUtils.getAllRemainingHits(getSessionContext());


    log.debug(pageSize + " VS " + totalRemaining);
    /*
     * The first page of results?
     */
    if (start == 1)
    {
      /*
       * Reduce requested page size to match the remaining result count and
       * fetch the results ...
       */
   	  clearParameters();
   		doPaginationCommand("previous", start,  Math.min(pageSize, totalRemaining));

   		submit();
   		validateResponse("PREVIOUS");
      return;
    }
    /*
     * The normal case, use MORE to pick up the results
     */
	  clearParameters();
	  doMoreCommand(start, pageSize, totalRemaining);

    submit();
    validateResponse("MORE");
	}

  /*
   * Helpers
   */

  /**
   * Set up the list of common server-side formatting files
   */
  private void setFormattingFiles()
  {
		setParameter("recordFormat",    "raw.xsl");
		setParameter("headerTemplate",  "xml/list-header.xml");
		setParameter("footerTemplate",  "xml/list-footer.xml");
		setParameter("errorTemplate",   "xml/error.xml");
		setParameter("errorFormat",     "error2XML.xsl");
  }

  /**
   * Set up the active database parameter(s) for MORE, NEXT, PREVIOUS
   */
  private void setDbList()
  {
    Iterator targetIterator;
    int count;

		targetIterator = StatusUtils.getStatusMapEntrySetIterator(getSessionContext());
		count = 0;

		while (targetIterator.hasNext())
		{
			Map.Entry entry   = (Map.Entry) targetIterator.next();
      String    target  = (String) entry.getKey();

		  Map       map     = StatusUtils.getStatusMapForTarget(getSessionContext(), target);
		  String    status  = (String) map.get("STATUS");

      if ("ACTIVE".equals(status))
      {
			  setParameter("dbList", target);
        count++;
      }
		}
		log.debug(count + " active database(s)");
  }

	/**
	 * Initial response validation and command cleanup/post-processing activities.
	 * <ul>
	 * <li>Verify the response format (an ERROR?)
	 * <li>If no error, perform any cleanup required for the command in question
	 * </ul>
	 *<p>
	 * @param action Server activity (SEARCH, LOGON, etc)
	 */
	private void validateResponse(String action) throws SearchException
	{
		Document 	document;
		Element 	element;
		String		message, errorText;

		log.debug("VALIDATE: " + action);
		/*
		 * Verify this response corresponds to anticipated server activity
		 */
		document = getResponseDocument();
		element = document.getDocumentElement();

		if ((element!= null) && (element.getTagName().equals(action)))
		{
		  /*
		  * Success - handle any post-processing required for this action
		  */
			if (action.equals("LOGON"))
			{
  			String sessionId;
        /*
         * We just logged in.  Save the session ID.
         */
				element   = DomUtils.getElement(element, "SESSION_ID");
	  		sessionId = DomUtils.getText(element);
				setSessionId(sessionId);

				log.debug("Saved Muse session ID \"" + sessionId + "\"");
				return;
			}

			if (action.equals("SEARCH") || action.equals("MORE"))
			{
			  Element searchElement;
				String  id;
        /*
         * A search (or "more results") command.  Save the reference ID.
         */
        searchElement = element;
				element = DomUtils.getElement(element, "REFERENCE_ID");
				id = DomUtils.getText(element);

				setReferenceId(id);
				log.debug("Saved search reference ID \"" + getReferenceId() + "\"");
        /*
         * For the initial search, save the result set name as well.
         */
				if (action.equals("SEARCH"))
				{
  				element = DomUtils.getElement(searchElement, "RESULT_SET_NAME");
	  			id = DomUtils.getText(element);

					setResultSetName(id);
					log.debug("Saved result set name \"" + getResultSetName() + "\"");
				}
				return;
			}
      /*
       * No cleanup activities for this action
       */
      log.debug("No \"cleanup\" activities implemented for " + action);
			return;
		}
		/*
		 * An error - see if we can decipher it
		 */
		element = document.getDocumentElement();
		if ((element != null) && (element.getTagName().equals("ERROR")))
		{
			element = DomUtils.getElement(element, "MESSAGE");
		}

		if (element == null)
		{
			errorText = action + ": Unexpected document format";

			log.debug("{} {}", errorText, document);

			StatusUtils.setGlobalError(getSessionContext(), errorText);
			throw new SearchException(errorText);
		}
		/*
		 * Format and log the error
		 */
		message   = DomUtils.getText(element);
		errorText = action
		          + " error: "
		          + (StringUtils.isNull(message) ? "*unknown*" : message);

		log.debug("{} {}", errorText, document);
		/*
		 * Session timeout is a special case
		 *
		 * -- Re-initialize (clear the query URL)
		 * -- Set "global failure" status
		 * -- Throw the timeout exception
		 */
		if (message.endsWith(NO_SESSION))
		{
			removeQueryUrl(APPLICATION);
			StatusUtils.setGlobalError(getSessionContext(), "Session timed out");
			throw new SessionTimeoutException();
		}
		/*
		 * Set final status, abort
		 */
		StatusUtils.setGlobalError(getSessionContext(), errorText);
		throw new SearchException(errorText);
	}

	/**
	 * Save the current search status (estimated hits, etc.) as session
	 * context information (status obtained by the PROGRESS command).
	 *
	 * @param document Server response
	 * @param rootElement Document root
	 * @return true If all targets have responded
	 */
	private boolean setSearchStatus(Document document) throws SearchException
	{
    Element   rootElement   = document.getDocumentElement();
		NodeList  nodeList 		  = DomUtils.getElementList(rootElement, "ITEM");
    Element   statusElement = DomUtils.getElement(rootElement, "STATUS");
		String    status        = "0";
		String    finalStatus   = "unknown";
		boolean   timedOut      = searchTimedOut();

		int       targetCount   = nodeList.getLength();
		int       active			  = 0;
		int       total 			  = 0;
		int       totalHits     = 0;
		int       complete      = 0;

		/*
		 * Update the status map for each target
		 */
		for (int i = 0; i < targetCount; i++)
		{
			Element		recordElement	= (Element) nodeList.item(i);
			HashMap		map;

			String		text, target;
			Element		element;
			int			  estimate, hits;

			/*
			 * Look for the target (database name)
			 */
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
			                                                      "ENTRY",
			                                                      "key", "targetID");
			target	= DomUtils.getText(element);
			map 	  = StatusUtils.getStatusMapForTarget(getSessionContext(), target);
			/*
			 * Get the current search status (we show this as "percent complete")
			 */
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
  		                                                      "ENTRY",
			                                                      "key", "status");
			if ((status	= DomUtils.getText(element)) == null)
			{
				status = "0";
				/*
				 * No status value; if the search will never start, mark it complete
				 */
  			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
	  		                                                      "ENTRY",
		  	                                                      "key", "message");
			  if ((text	= DomUtils.getText(element)) != null)
			  {
				  if ("Not Started".equals(text))
				  {
				    status = "100";
			    }
		    }
			}
			/*
			 * Find the estimated match count
			 */
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
			                                                      "ENTRY",
			                                                      "key", "estimate");
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			estimate = Integer.parseInt(text);
      /*
       * Any hits? (unused for now)
       */
/*******************************************************************************
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
			                                                      "ENTRY",
			                                                      "key", "hits");
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			hits = Integer.parseInt(text);
*******************************************************************************/
      /*
       * Add this estimate to the grand total.
       *
       * Do we need to check for?
       *
       *    (hits > 0)
       *    (status.equals("100"))
       */
			map.put("ESTIMATE", "0");
 			map.put("STATUS", "DONE");

 			if (estimate > 0)
 			{
  			map.put("ESTIMATE", String.valueOf(estimate));
   			total	+= estimate;

				map.put("STATUS", "ACTIVE");
				active++;

				status = "100";
 			}
      /*
       * Is this search complete?
       */
			map.put("PERCENT_COMPLETE", status);

			if ("100".equals(status))
      {
        complete++;
      }

  		log.debug("****** Target: "
  		        +  target
  		        +  ", status = "
  		        +  status
  		        +  ", all searches complete? "
  		        +  (complete == targetCount)
  		        +  ", timedout? "
  		        +  timedOut);
    }
		/*
		 * Save in session context:
		 *
		 * -- The largest number of records we could possibly return
		 * -- The count of "in progress" searches
		 */
		getSessionContext().putInt("TOTAL_ESTIMATE", total);
		getSessionContext().putInt("maxRecords", total);
		getSessionContext().putInt("active", active);
    /*
     * Determine final status
     */
    finalStatus = "not finished";
    if (statusElement != null)
    {
      String commandStatus = DomUtils.getText(statusElement);

      if ("1".equals(commandStatus))
      {
        finalStatus = "complete";
      }
    }

		return (finalStatus.equals("complete") || timedOut);
	}

  /*
   * Search status (PROGRESS command) timers
   */
  private static final long ONE_SECOND = 1000;
  private long _timeLimit;

  /**
   * Set the search status timout
   * @param numberOfSeconds Seconds (from now) until the search times out
   */
  private void setSearchStatusTimeout(long numberOfSeconds)
  {
    _timeLimit = System.currentTimeMillis() + (numberOfSeconds * ONE_SECOND);
  }

  /**
   * Has the current search timed out?
   */
  private boolean searchTimedOut()
  {
    return System.currentTimeMillis() >= _timeLimit;
  }

  /*
   * Getters & setters
   */

  /**
   * Get the number of requested search targets (databases)
   * @return The count of target DBs
   */
  private int getTargetCount()
  {
    return _targetCount;
  }

  /**
   * Determine the page size (the number of results to request from the server)
   * @return The page size (as a String)
   */
  private String getPageSize()
  {
    return "30";
  }

	/**
	 * Fetch the Muse session ID
	 * @return The session ID (null until a Muse LOGON has taken place)
	 */
	private String getSessionId()
	{
		return (String) getSessionContext().get("SESSION_ID");
	}

	/**
	 * Set the Muse session ID
	 * @param sessionId The session ID
	 */
	private void setSessionId(String sessionId)
	{
		getSessionContext().put("SESSION_ID", sessionId);
	}

	/**
	 * Fetch the search reference id
	 * @return The reference id (null until a search is done)
	 */
	private String getReferenceId()
	{
		return (String) getSessionContext().get("REFERENCE_ID");
	}

	/**
	 * Save the Muse search reference
	 * @param referenceNumber The reference number for this search
	 */
	private void setReferenceId(String referenceNumber)
	{
		getSessionContext().put("REFERENCE_ID", referenceNumber);
	}

	/**
	 * Fetch the search result set name
	 * @return the default result set for this search
	 */
	private String getResultSetName()
	{
		return (String) getSessionContext().get("RESULT_SET_NAME");
	}

	/**
	 * Save the name of the search result set
	 * @param name The result set name
	 */
	private void setResultSetName(String name)
	{
		getSessionContext().put("RESULT_SET_NAME", name);
	}

	/**
	 * Fetch the (Muse format) search string (overrides HttpTransactionQueryBase)
	 * @return The native Muse query text
	 */
	public String getSearchString()
	{
	  return (String) getSessionContext().get("SEARCH_QUERY");
	}

  /*
   * Helpers
   */

	/**
	 * Parse CQL search queries into a crude take on the Muse format.
	 * @param cql String containing a cql query
	 * @return Muse search criteria
	 */
	private String parseCql(String cql) throws IllegalArgumentException
	{
		CqlParser	parser;
		String		result;

		log.debug( "Initial CQL Criteria: " + cql );

		parser 	= new CqlParser();
		result	= parser.doCQL2MetasearchCommand(cql);

		log.debug("Processed Result: " + result);
		return result;
	}

	/**
	 * Get an element from the server response
	 * @Element parent Look for named element here
	 * @param elementName Element name
	 * @return The first occurance of the named element (null if none)
	 */
	private Element getElement(Element parent, String elementName)
	{
		try
		{
			Element root = parent;

			if (root == null)
			{
				root = getResponseDocument().getDocumentElement();
			}
			return DomUtils.getElement(root, elementName);

		}
		catch (Exception exception)
		{
			throw new SearchException(exception.toString());
		}
	}

	/**
	 * Get an element from the server response (search from document root)
	 * @param elementName Element name
	 * @return The first occurance of the named element (null if none)
	 */
	private Element getElement(String elementName)
	{
		return getElement(null, elementName);
	}

	/**
	 * Debugging: Display XML (write a Document or Element to the log)
	 *
	 * @param text Label text for this document
	 * @param xmlObject XML Document or Element to display
	 */
	private void displayXml(String text, Object xmlObject)
	{
	    log.debug("{} {}", text, xmlObject);
	}
}