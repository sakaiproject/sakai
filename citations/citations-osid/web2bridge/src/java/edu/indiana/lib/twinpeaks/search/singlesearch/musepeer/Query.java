/**********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.singlesearch.musepeer;

import edu.indiana.lib.twinpeaks.net.*;
import edu.indiana.lib.twinpeaks.search.*;
import edu.indiana.lib.twinpeaks.search.singlesearch.CqlParser;
import edu.indiana.lib.twinpeaks.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Send a query to the Musepeer  interface
 */
public class Query extends HttpTransactionQueryBase
{
	private static org.apache.commons.logging.Log	_log = LogUtils.getLog(Query.class);
  /**
   * DEBUG only: Display server response data?
   */
  public static final boolean DISPLAY_XML = false;
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
	private static final String NO_SESSION = "Connection to server not set.";
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
		_log.debug("*** Beginning action: " + action);

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
  		int sleepCount, sleepLimit;
  		boolean done;
		  /*
		   * Initialize a new search context block
		   */
			StatusUtils.initialize(getSessionContext(), getRequestParameter("targets"));
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
			 * Search
			 */
			clearParameters();

			doSearchCommand();
			submit();

			displayXml("Search", getResponseDocument());
			validateResponse("SEARCH");
			/*
			 * Pick up the current status
			 */
			clearParameters();

			doProgressCommand();
			submit();
      /*
       * Wait for a complete set of responses.  If not found, wait:
       *
       *     8 seconds (one source)
       *    20 seconds (at most)
       *
       * The times are set via the math.min() and Thread.sleep() calls below.
       */
		  sleepCount = 0;
		  sleepLimit = 5 + Math.min((getTargetCount() * 3), 20);

		  done = setStatus(getResponseDocument());
      while (!done && (sleepCount++ < sleepLimit))
			{
			  displayXml("Progress", getResponseDocument());
				try
				{
					Thread.sleep(1000);
					_log.debug("Sleeping (" + sleepCount + " of " + sleepLimit + ")");
				}
				catch (InterruptedException ignore) { }

  			clearParameters();
				doProgressCommand();
				submit();

  		  done = setStatus(getResponseDocument());
			}

		  displayXml("Status done", getResponseDocument());

			validateResponse("PROGRESS");
			return;
		}
		/*
		 * Request additional results
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

		_log.debug("Logging in as \"" + username + "\"");

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
		_log.debug("Search criteria: " + searchCriteria);
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

 			_log.debug("SEARCH: added DB " + target);
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

		_log.debug("Using result set name \"" + getResultSetName() + "\"");
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
    _log.debug("PAGE: start = " + start
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

		_log.debug("MORE: using result set name \"" + getResultSetName() + "\"");
		_log.debug("MORE: queryStatement = " + getSearchString());

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
    _log.debug("MORE: start = " + start
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


    _log.debug(pageSize + " VS " + totalRemaining);
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
		_log.debug(count + " active database(s)");
  }

	/**
	 * Initial response validation and cleanup activities.
	 * <ul>
	 * <li>Verify the response format (ERROR?)
	 * <li>If no error, perform any cleanup required for the action in question
	 * </ul>
	 *<p>
	 * @param action Server activity (SEARCH, LOGON, etc)
	 */
	private void validateResponse(String action) throws SearchException
	{
		Document 	document;
		Element 	element;
		String		message, errorText;


		_log.debug("VALIDATE: " + action);
		/*
		 * Success?
		 */
		document = getResponseDocument();
		element = document.getDocumentElement();

		if ((element!= null) && (element.getTagName().equals(action)))
		{
			if (action.equals("LOGON"))
			{
  			String sessionId;

				element   = DomUtils.getElement(element, "SESSION_ID");
	  		sessionId = DomUtils.getText(element);
				setSessionId(sessionId);

				_log.debug("Saved Muse session ID \"" + sessionId + "\"");
				return;
			}

			if (action.equals("SEARCH") || action.equals("MORE"))
			{
			  Element searchElement;
				String  id;

        searchElement = element;
				element = DomUtils.getElement(element, "REFERENCE_ID");
				id = DomUtils.getText(element);

				setReferenceId(id);
				_log.debug("Saved search reference ID \"" + getReferenceId() + "\"");

				if (action.equals("SEARCH"))
				{
  				element = DomUtils.getElement(searchElement, "RESULT_SET_NAME");
	  			id = DomUtils.getText(element);

					setResultSetName(id);
					_log.debug("Saved result set name \"" + getResultSetName() + "\"");
				}
				return;
			}
      /*
       * No cleanup activities for this action
       */
      _log.debug("No \"cleanup\" activities implemented for " + action);
			return;
		}
		/*
		 * Error
		 */
		element = document.getDocumentElement();
		if ((element != null) && (element.getTagName().equals("ERROR")))
		{
			element = DomUtils.getElement(element, "MESSAGE");
		}

		if (element == null)
		{
			errorText = action + ": Unexpected document format";

			LogUtils.displayXml(_log, errorText, document);

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

		LogUtils.displayXml(_log, errorText, document);
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
	 * Save the initial search status (estimated hits, etc.) as session context information
	 * @param document Server response
	 * @rootElement Document root
	 */
	private boolean setStatus(Document document) throws SearchException
	{
    Element   rootElement = document.getDocumentElement();
		NodeList  nodeList 		= DomUtils.getElementList(rootElement, "ITEM");
		String    status      = "0";
		int       targetCount = nodeList.getLength();
		int       active			= 0;
		int       total 			= 0;
		int       complete    = 0;

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
			 * Target (database)
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
				 * No status value - did the search ever start?
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
			map.put("PERCENT_COMPLETE", status);
			/*
			 * Estimated match count
			 */
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
			                                                      "ENTRY",
			                                                      "key", "estimate");

			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			map.put("ESTIMATE", text);

			estimate = Integer.parseInt(text);
      /*
       * Any hits?
       */
			element = DomUtils.selectFirstElementByAttributeValue(recordElement,
			                                                      "ENTRY",
			                                                      "key", "hits");
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			hits = Integer.parseInt(text);
			/*
			 * Do final hit and estimate totals
			 */
 			map.put("STATUS", "DONE");
      /*
       * If we have hits, add this estimate to the grand total
       */
      if (hits > 0)
      {
   			total	+= estimate;
      }
			/*
			 * This search target is active only if there are records available
			 */
			if ((estimate > 0) && (hits > 0))
			{
			  int pageSize = Math.min(targetCount * MINIMUM_PAGESIZE,
			                                        MAXIMUM_PAGESIZE);

				map.put("STATUS", "ACTIVE");
				active++;
        /*
         * To try and exit the "check status" loop earlier, we consider this
         * target complete if we've already found enough result records ...
         */
        _log.debug("**** " + target + ": " + total + " vs " + pageSize);
        if (total >= pageSize)
        {
          status = "100";
          _log.debug("**** marking "  + target + " \"early complete\"");
        }
			}
      /*
       * Is this search complete?
       */
			if ("100".equals(status))
      {
        complete++;
      }

  		_log.debug("****** Target: "
  		        +  target
  		        +  ", status = "
  		        +  status);
    }
		/*
		 * Save in session context:
		 *
		 * -- The largest number of records we could possibly return
		 * -- The count of "in progress" searches
		 */
		getSessionContext().put("maxRecords", String.valueOf(total));
		getSessionContext().putInt("TOTAL_ESTIMATE", total);
		getSessionContext().putInt("active", active);

		return complete == targetCount;
	}


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
  public static final int MINIMUM_PAGESIZE = 20;
  public static final int MAXIMUM_PAGESIZE = 50;

  private String getPageSize()
  {
    return "30";
/*******************************************************************************
    int targets   = StatusUtils.getActiveTargetCount(getSessionContext());
    int pageSize  = targets * MINIMUM_PAGESIZE;

    if ((pageSize = Math.min(pageSize, MAXIMUM_PAGESIZE)) == 0)
    {
      pageSize = MINIMUM_PAGESIZE;
    }
    return String.valueOf(pageSize);
*******************************************************************************/
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
	 * Construct the search result set name
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

	/**
	 * Parse CQL search queries into a crude take on the Muse format.
	 * @param cql String containing a cql query
	 * @return Muse search criteria
	 */
	private String parseCql(String cql) throws IllegalArgumentException
	{
		CqlParser	parser;
		String		result;

		_log.debug( "Initial CQL Criteria: " + cql );

		parser 	= new CqlParser();
		result	= parser.doCQL2MetasearchCommand(cql);

		_log.debug("Processed Result: " + result);
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
	  if (DISPLAY_XML)
	  {
	    LogUtils.displayXml(_log, text, xmlObject);
    }
  }

  /*
   * Not used
   */
	private boolean isStatusReadyToBeRead(Document document)
	{
		NodeList	nodeList;
		Node node;

		Element rootElement;
		Element	recordElement;

		String attributeResult;

		boolean status = false;


		/* Response will look like:

		<PROGRESS>
			<TOTAL_HITS>100</TOTAL_HITS>
		  	<TOTAL_ESTIMATE>4449</TOTAL_ESTIMATE>
			<STATUS>1</STATUS>
			<ITEMS>
				<ITEM>
					<ENTRY key="searchURL">
						http://server
					</ENTRY>
					<ENTRY key="messageID">STATUS_DONE</ENTRY>
					<ENTRY key="moduleName">Academic Search Premier</ENTRY>
					<ENTRY key="targetID">EBSCOASP</ENTRY>
					<ENTRY key="hits">100</ENTRY>
					<ENTRY key="instructionID">
						EBSCOASP.jar:com.edulib.ice.modules.connectors.EBSCO@4
					</ENTRY>
					<ENTRY key="status">100</ENTRY>
					<ENTRY key="timestamp">1226947004852</ENTRY>
					<ENTRY key="estimate">4449</ENTRY>
					<ENTRY key="message">Done</ENTRY>
				</ITEM>
			</ITEMS>
		</PROGRESS>
		*/

		rootElement = document.getDocumentElement();

		node = DomUtils.getElement(rootElement, "TOTAL_ESTIMATE");

		if (node == null)
		{
			_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ total estimate = null");
			return false;
		}
/*
		int total_estimate = Integer.parseInt(node.getTextContent());

		if (total_estimate > 0)
		{
			_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ total estimate > 0");
			return true;
		}
*/

		nodeList = DomUtils.getElementList(rootElement, "ENTRY");

		if (nodeList == null)
		{
			_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ no items");
			return false;
		}

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			recordElement	= (Element) nodeList.item(i);

			attributeResult = recordElement.getAttribute("key");

			if (attributeResult != null)
			{
				attributeResult = attributeResult.trim();

				if (attributeResult.equalsIgnoreCase("status"))
				{
					if (recordElement.getTextContent().trim().equals("100"))
					{
						_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ status = 100");
						status = true;
					}
					else
					{
						_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ status != 100. status = " + recordElement.getTextContent());
						_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ key = " + attributeResult);

						return false;
					}
				} // end if attributeResult.length > 0
			} // end if attributeResult != null


		} // end for i

		return status;
	}
}