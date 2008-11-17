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
	 * Records displayed "per page"
	 */
	public static final String RECORDS_PER_PAGE = "10";
	/**
	 * Records to fetch from each search target
	 */
	private static final String RECORDS_PER_TARGET = "100";
	/**
	 * Unique name for this search application
	 */
	private final String APPLICATION = SessionContext.uniqueSessionName(this);
	/**
	 * Error code: No logged-in session (this is wrong - what text is returned?)
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
	 * Web2 input
	 */
	private Document _web2Document;
	/**
	 * Active reference ID #
	 */
	private static long _referenceId = System.currentTimeMillis();
	/**
	 * Local ID (for the current transaction)
	 */
	private long _transactionId;
	/**
	 * Local version of server response (modified to contain SFX URL data)
	 */
	private byte _localResponseBytes[];
	/**
	 * Local byte array ready for use?
	 */
	private boolean _localResponseBytesReady = false;
	/**
	 * General synchronization
	 */
	private static Object _sync = new Object();

	/**
	 * Constructor
	 */
	public Query() {
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
		}
		/*
		 * Now deal with the search criteria (CQL syntax)
		 */
		_museSearchString = parseCql(getRequestParameter("searchString"));
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
		 * Request additional results (pagination)?  Save the requested
		 * pagesize and starting record
		 */
		action = getRequestParameter("action");
		if (action.equalsIgnoreCase("requestRange"))
		{
			getSessionContext().putInt("startRecord", getIntegerRequestParameter("startRecord").intValue());
			getSessionContext().putInt("pageSize", getIntegerRequestParameter("pageSize").intValue());
		}
		/*
		 * New search?
		 */
		if (action.equalsIgnoreCase("startSearch"))
		{	/*
		 * Initialize a new search context block
		 */
			StatusUtils.initialize(getSessionContext(), getRequestParameter("targets"));
			/*
			 * LOGOFF any previous session
			 */

			doLogoffCommand();
			submit();

			try
			{
				_log.debug(DomUtils.serialize(getResponseDocument()));
			}
			catch (Exception ignore) { }

			/*
			 * LOGON
			 */

			clearParameters();

			doLogonCommand();
			submit();

			LogUtils.displayXml(_log, "Login", getResponseDocument());

			validateResponse("LOGON");
			/*
			 * Search
			 */
			clearParameters();

			doSearchCommand();
			submit();
//			LogUtils.displayXml(_log, "Search Results", getResponseDocument());
			validateResponse("SEARCH");
			/*
			 * Pick up the current status
			 */
			clearParameters();

			doStatusCommand();
			submit();
			
//			isStatusReadyToBeRead(getResponseDocument());

			int sleepCount = 1;
			
			while (! isStatusReadyToBeRead(getResponseDocument()) && sleepCount < 7)
			{
				_log.info("Status is not ready to be read");
				LogUtils.displayXml(_log, "Progress", getResponseDocument());

				try
				{
					Thread.sleep(5000);
					_log.info("sleeping for 5 seconds");
				}
				catch (InterruptedException ignore) { }

				doStatusCommand();
				submit();
				
				sleepCount++;				
			}

			LogUtils.displayXml(_log, "Status", getResponseDocument());
			validateResponse("PROGRESS");

			return;
		}
		/*
		 * Request additional SEARCH results
		 */
		doResultsCommand();
		LogUtils.displayXml(_log, "Results", getResponseDocument());
	}

	/*
	 * Helpers
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
		int targetCount;

		/*
		 * Set search criteria (use the search filter, if any is configured)
		 */
		searchFilter 		= SearchSource.getConfiguredParameter(_database, "searchFilter");
		searchCriteria 	= (searchFilter == null) ? "" : (searchFilter + " ");
		searchCriteria += getSearchString();
		_log.debug("Search criteria: " + searchCriteria);

		getSessionContext().put("SEARCH_QUERY", searchCriteria);
		/*
		 * Determine database(s) to examine, sort mode, page size, etc.
		 */
		targets       = getRequestParameter("targets");
		targetParser  = new StringTokenizer(targets);
		targetCount   = targetParser.countTokens();
		_log.debug("Targets for " + _database + ", " + targetCount + " targets: " + targets);

		pageSize = getIntegerRequestParameter("pageSize").toString();
		_log.debug("Page size: " + pageSize);
		/*
		 * Generate the search command
		 */
		setParameter("action", "search");
		setParameter("xml", "true");
		setParameter("sessionID", getSessionId());

		setParameter("queryStatement", searchCriteria);
		while (targetParser.hasMoreTokens())
		{
			setParameter("dbList", targetParser.nextToken());
		}

		setParameter("start", "1");
		setParameter("limitsMaxPerSource", RECORDS_PER_TARGET);
		setParameter("limitsMaxPerPage", pageSize);

		setParameter("recordFormat",    "raw.xsl");
		setParameter("headerTemplate",  "xml/list-header.xml");
		setParameter("footerTemplate",  "xml/list-footer.xml");
		setParameter("errorTemplate",   "xml/error.xml");
		setParameter("errorFormat",     "error2XML.xsl");
	}

	/**
	 * Generate a "get status" command
	 */
	private void doStatusCommand() throws SearchException
	{
		String pageSize = getIntegerRequestParameter("pageSize").toString();

		setParameter("action", "progress");
		setParameter("xml", "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());

		setParameter("errorTemplate", "xml/error.xml");
		setParameter("errorFormat",   "error2XML.xsl");
	}

	/**
	 * Generate a pagination command
	 * @param page Pagination (<code>next</code> | <code>previous</code>)
	 * @param firstRecord First record to retrieve
	 */
	private void doPaginationCommand(String page, int firstRecord)
	{
		Iterator	targetIterator;

		setParameter("action", page);
		setParameter("xml", "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());
		setParameter("resultSet", getResultSetName());

		targetIterator = StatusUtils.getStatusMapEntrySetIterator(getSessionContext());
		while (targetIterator.hasNext())
		{
			Map.Entry entry = (Map.Entry) targetIterator.next();

			setParameter("dbList", (String) entry.getKey());
		}

		setParameter("firstRetrievedRecord", Integer.toString(firstRecord));
		setParameter("totalRecords", RECORDS_PER_TARGET);
		setParameter("limitsMaxPerSource", RECORDS_PER_TARGET);
		setParameter("limitsMaxPerPage", (String) getSessionContext().get("pageSize"));

		_log.debug(page + ": first=" + firstRecord + ", page=" + getSessionContext().get("pageSize"));

		setParameter("recordFormat",    "raw.xsl");
		setParameter("headerTemplate",  "xml/list-header.xml");
		setParameter("footerTemplate",  "xml/list-footer.xml");
		setParameter("errorTemplate",   "xml/error.xml");
		setParameter("errorFormat",     "error2XML.xsl");
	}

	/**
	 * Generate a MORE data command
	 * @param firstRecord First record to retrieve
	 */
	private void doMoreCommand(int firstRecord)
	{
		Iterator targetIterator;

		setParameter("action", "more");
		setParameter("xml", "true");

		setParameter("sessionID", getSessionId());
		setParameter("searchReferenceID", getReferenceId());
		setParameter("resultSet", getResultSetName());
		setParameter("append", "true");

		setParameter("queryStatement", getSearchString());
		_log.debug("MORE: queryStatement = " + getSearchString());

		targetIterator = StatusUtils.getStatusMapEntrySetIterator(getSessionContext());
		while (targetIterator.hasNext())
		{
			Map.Entry entry = (Map.Entry) targetIterator.next();

			setParameter("dbList", (String) entry.getKey());
			_log.debug("MORE: added DB " + (String) entry.getKey());
		}

		setParameter("start", Integer.toString(firstRecord));
		setParameter("totalRecords", Integer.toString(firstRecord - 1));
		setParameter("limitsMaxPerSource", RECORDS_PER_TARGET);
		setParameter("limitsMaxPerPage", (String) getSessionContext().get("pageSize"));

		setParameter("recordFormat",    "raw.xsl");
		setParameter("headerTemplate",  "xml/list-header.xml");
		setParameter("footerTemplate",  "xml/list-footer.xml");
		setParameter("errorTemplate",   "xml/error.xml");
		setParameter("errorFormat",     "error2XML.xsl");
	}

	/**
	 * Generate a "fetch results" command
	 */
	private void doResultsCommand() throws SearchException
	{
		String  page;
		int			active, start, pageSize, perTarget;

		active 		= getSessionContext().getInt("active");
		start 		= getSessionContext().getInt("startRecord");
		pageSize	= getSessionContext().getInt("pageSize");
		perTarget = pageSize;
		page      = "next"; // XXX (start == 1) ? "previous" : "next";

		_museSearchString = (String) getSessionContext().get("SEARCH_QUERY");

		_log.debug("doResults: start = " + start);

		clearParameters();
		doMoreCommand(start);
		submit();
		validateResponse("MORE");

		clearParameters();
		doPaginationCommand(page, start);
		submit();
		validateResponse(page.toUpperCase());
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
	 * Save the name of the SEARCH result set
	 * @param referenceId The SEARCH command reference id
	 */
	private void setResultSetName(String referenceId)
	{
		StringBuilder resultSetName = new StringBuilder("default");

		resultSetName.append(referenceId);
		resultSetName.append(".xml");

		getSessionContext().put("RESULT_SET_NAME", resultSetName.toString());
	}

	/**
	 * Fetch the (Muse format) search string (overrides HttpTransactionQueryBase)
	 * @return The native Muse query text
	 */
	public String getSearchString()
	{
		return _museSearchString;
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
	 * Format the current reference id
	 * @return XML id
	 */
	private Element addReferenceId() {
		return addWeb2Input("REFERENCE_ID", getTransactionId());
	}

	/**
	 * Establish a transaction ID for the current activity (LOGIN, SEARCH, etc)
	 */
	private void setTransactionId() {
		synchronized (_sync) {
			_transactionId = _referenceId++;
		}
	}

	/**
	 * Fetch the current transaction id
	 * @return The ID
	 */
	private String getTransactionId() {
		return Long.toHexString(_transactionId);
	}

	/**
	 * Returns a new result set name for this transaction
	 * @return Result set name (constant portion + reference ID)
	 */
	private synchronized String saveFindReferenceId(String transactionId)
	{
		removeSessionParameter(APPLICATION, "findReferenceId");
		setSessionParameter(APPLICATION, "findReferenceId", transactionId);

		return getFindReferenceId();
	}

	/**
	 * Returns a new result set name for this transaction
	 * @return Result set name (constant portion + reference ID)
	 */
	private synchronized String getFindReferenceId()
	{
		return getSessionParameter(APPLICATION, "findReferenceId");
	}

	public Iterator getStatusMapEntrySetIterator()
	{
		HashMap statusMap = (HashMap) getSessionContext().get("searchStatus");
		Set			entrySet	= statusMap.entrySet();

		return entrySet.iterator();
	}

	/**
	 * Returns a new result set name for this transaction
	 * @return Active result set name(s) (name1|name2|name3), null if none active
	 */
	private String getFindResultSetId()
	{
		String	ids				= "";
		int			active		= 0;

		for (Iterator iterator = getStatusMapEntrySetIterator(); iterator.hasNext(); )
		{
			Map.Entry entry 			= (Map.Entry) iterator.next();
			HashMap		systemMap 	= (HashMap) entry.getValue();
			String		status			= (String) systemMap.get("STATUS");
			String		id;

			if (!status.equals("ACTIVE"))
			{
				continue;
			}

			id = (String) systemMap.get("RESULT_SET");

			if (ids.length() == 0)
			{
				ids = id;
			}
			else
			{
				ids = ids + "|" + id;
			}
			active++;
		}
		_log.debug(active + " result set ids: " + ids);
		getSessionContext().putInt("active", active);
		return (ids.length() == 0) ? null : ids;
	}

	/**
	 * Returns a new result set name for this transaction
	 * @return Result set name (constant portion + reference ID)
	 */
	private synchronized String getNewTransactionResultSetName() {

		removeSessionParameter(APPLICATION, "resultSetName");
		return getTransactionResultSetName();
	}

	/**
	 * Returns the result set name for this transaction (SEARCH)
	 * @return Result set name (constant portion + reference ID)
	 */
	private synchronized String getTransactionResultSetName() {
		String resultSetName = getSessionParameter(APPLICATION, "resultSetName");

		if (resultSetName == null) {
			StringBuilder name = new StringBuilder("sakaibrary");

			name.append(getTransactionId());
			name.append(".xml");

			resultSetName = name.toString();
			setSessionParameter(APPLICATION, "resultSetName", resultSetName);
		}
		_log.debug("Transaction result set name: " + resultSetName);
		return resultSetName;
	}


	/**
	 * Add Element and child text
	 * @param parentElement Add new element here
	 * @param newElementName New element name
	 * @param text Child text (for the new element)
	 */
	private Element addWeb2Input(Element parentElement,
			String newElementName,
			String text) {
		Element element;

		element = DomUtils.createElement(parentElement, newElementName);

		if (!StringUtils.isNull(text)) {
			DomUtils.addText(element, text);
		}
		return element;
	}

	/**
	 * Add Element and child text to document root
	 * @param newElementName New element name
	 * @param text Child text (for the new element)
	 */
	private Element addWeb2Input(String newElementName,
			String text) {

		return addWeb2Input(_web2Document.getDocumentElement(), newElementName, text);
	}

	/**
	 * Add Element to parent
	 * @param parentElement Add new element here
	 * @param newElementName New element name
	 */
	private Element addWeb2Input(Element 	parentElement,
			String 	newElementName) {

		return addWeb2Input(parentElement, newElementName, null);
	}

	/**
	 * Add Element to document root
	 * @param newElementName New element name
	 */
	private Element addWeb2Input(String newElementName) {

		return addWeb2Input(_web2Document.getDocumentElement(), newElementName, null);
	}

	/**
	 * Get an element from the server response
	 * @Element parent Look for named element here
	 * @param elementName Element name
	 * @return The first occurance of the named element (null if none)
	 */
	private Element getElement(Element parent, String elementName) {
		try {
			Element root = parent;

			if (root == null) {
				root = getResponseDocument().getDocumentElement();
			}
			return DomUtils.getElement(root, elementName);

		} catch (Exception exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
	 * Get an element from the server response (search from document root)
	 * @param elementName Element name
	 * @return The first occurance of the named element (null if none)
	 */
	private Element getElement(String elementName) {
		return getElement(null, elementName);
	}

	/**
	 * Initial response validation.  Verify:
	 * <ul>
	 * <li>Error code
	 * <li>Correct <REFERENCE_ID> value
	 * </ul>
	 *<p>
	 * @param action Server activity (SEARCH, LOGON, etc)
	 */
	private void validateResponse(String action) throws SearchException
	{
		Document 	document;
		Element 	element;
		String		message, errorText;

		document  = getResponseDocument();
		element		= document.getDocumentElement();
		/*
		 * Success?
		 */
		_log.debug("VALIDATE: " + element.getTagName() + " vs " + action);

		if ((element!= null) && (element.getTagName().equals(action)))
		{
			if (action.equals("LOGON"))
			{
//				if (getSessionId() == null)
//				{
					String sessionId;

					element   = DomUtils.getElement(element, "SESSION_ID");
					sessionId = DomUtils.getText(element);
					setSessionId(sessionId);

					_log.debug("Saved Muse session ID \"" + sessionId + "\"");
//				}

				return;
			}

			if (action.equals("SEARCH") || action.equals("MORE"))
			{
				String referenceId;

				element     = DomUtils.getElement(element, "REFERENCE_ID");
				referenceId = DomUtils.getText(element);

				setReferenceId(referenceId);
				_log.debug("Saved search reference ID \"" + referenceId + "\"");

				if (action.equals("SEARCH"))
				{
					setResultSetName(referenceId);
					_log.debug("Saved result set name \"" + getResultSetName() + "\"");
				}

				return;
			}

			if (action.equals("PROGRESS"))
			{
				boolean complete = false;
				int     count    = 0;

				complete = setStatus(document, element);

				_log.debug("PROGRESS - Status = " + complete);

/*				while (!complete)
				{
					try
					{
						Thread.sleep(count * 250);
					}
					catch (InterruptedException ignore) { }

					complete = setStatus(document, element);

					if (!complete)
					{
						complete = (++count == 10);
					}

				}
*/
			}

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
	 * Save the initial status (find set name(s), estimated hits, etc.) as
	 * session context information
	 * @return A Map of status details (keyed by target name)
	 */
	private void setFindStatus() throws SearchException
	{
		NodeList	nodeList;
		String		target;
		int				active, total;

		nodeList 		= DomUtils.getElementList(getResponseDocument().getDocumentElement(), "RECORD");
		active			= 0;
		total 			= 0;

		/*
		 * Update the status map for each target
		 */
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Element		recordElement	= (Element) nodeList.item(i);
			HashMap		map;

			String		text;
			Element		element;
			int				estimate;

			element	= DomUtils.getElement(recordElement, "TARGET");
			target	= DomUtils.getText(element);
			map 		= StatusUtils.getStatusMapForTarget(getSessionContext(), target);

			element = DomUtils.getElement(recordElement, "RESULT_SET");
			text 		= DomUtils.getText(element);
			map.put("RESULT_SET", ((text == null) ? "<none>" : text));

			element = DomUtils.getElement(recordElement, "ESTIMATE");
			text 		= DomUtils.getText(element);
			map.put("ESTIMATE", text);

			estimate = Integer.parseInt(text);
			total		+= estimate;

			map.put("STATUS", "DONE");
			if (estimate > 0)
			{
				map.put("STATUS", "ACTIVE");
				active++;
			}
		}
		/*
		 * Save in session context:
		 *
		 * -- The largest number of records we could possibly return
		 * -- The count of "in progress" searches
		 */
		getSessionContext().put("maxRecords", String.valueOf(total));
		getSessionContext().putInt("active", active);
	}

	
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
		
		int total_estimate = Integer.parseInt(node.getTextContent());
		
		if (total_estimate > 0)
		{
			_log.info("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ total estimate > 0");
			return true;
		}

		
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
	
	/**
	 * Save the initial search status (estimated hits, etc.) as session context information
	 * @param document Server response
	 * @rootElement Document root
	 */
	private boolean setStatus(Document document, Element rootElement) throws SearchException
	{
		NodeList	nodeList;
		
		int				active, total;
		boolean   complete;

		nodeList 		= DomUtils.getElementList(rootElement, "ITEM");
		active			= 0;
		total 			= 0;
		complete    = true;

		/*
		 * Update the status map for each target
		 */
		
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Element		recordElement	= (Element) nodeList.item(i);
			HashMap		map;

			String		text, target;
			Element		element;
			int			estimate;
			
			/*
			 * Target (database)
			 */
			
			element = DomUtils.selectFirstElementByAttributeValue (recordElement, "ENTRY", "key", "targetID");
			target	= DomUtils.getText(element);
			map 	= StatusUtils.getStatusMapForTarget(getSessionContext(), target);

			/*
			 * Percent complete
			 */
			
			element = DomUtils.selectFirstElementByAttributeValue (recordElement, "ENTRY", "key", "status");
			
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			
			map.put("PERCENT_COMPLETE", text);
			/*
			 * Estimated match count
			 */
			
			element = DomUtils.selectFirstElementByAttributeValue (recordElement, "ENTRY", "key", "estimate");
			
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
				complete = false;
			}
			
			map.put("ESTIMATE", text);

			estimate = Integer.parseInt(text);
			total		+= estimate;
			
			/*
			 * This search target is active only if there are records available
			 */
			
			map.put("STATUS", "DONE");
			
			if (estimate > 0)
			{
				map.put("STATUS", "ACTIVE");
				active++;
			}
		}
		
		/*
		 * Save in session context:
		 *
		 * -- The largest number of records we could possibly return
		 * -- The count of "in progress" searches
		 */
		
		getSessionContext().put("maxRecords", String.valueOf(total));
		getSessionContext().putInt("active", active);

		return complete;
	}

	/**
	 * Save the initial SEARCH command status (find set name, estimated hits)
	 * @return A Map of status details (keyed by target name)
	 */
	private void setSearchStatus() throws SearchException
	{
		List			nodeList;
		String		target;
		int				active, total;

		nodeList		= DomUtils.selectElementsByAttributeValue(getResponseDocument().getDocumentElement(), "RECORD", "type", "status");
		active			= 0;
		total 			= 0;

		for (int i = 0; i < nodeList.size(); i++)
		{
			Element		recordElement	= (Element) nodeList.get(i);
			HashMap		map;
			String		text;
			Element		element;
			int				max;


			target = getSourceId(recordElement.getAttribute("source"));
			if (target.equals("unavailable"))
			{
				target = recordElement.getAttribute("source");
			}

			map	= StatusUtils.getStatusMapForTarget(getSessionContext(), target);
			map.put("RESULT_SET", getTransactionResultSetName());
			map.put("HITS", "0");

			element = DomUtils.getElement(recordElement, "ESTIMATE");
			text 		= DomUtils.getText(element);
			map.put("ESTIMATE", text);

			max	= Integer.parseInt(text);
			total	+= max;

			map.put("STATUS", "DONE");
			if (max > 0)
			{
				map.put("STATUS", "ACTIVE");
				active++;
			}
		}
		/*
		 * Save in session context:
		 *
		 * -- The largest number of records we could possibly return
		 * -- The count of "in progress" searches
		 */
		getSessionContext().put("maxRecords", String.valueOf(total));
		getSessionContext().putInt("active", active);
	}

	/**
	 * Look up the "sourceID" attribute (the target name) for a specified
	 * RECORD element "source"
	 * @param source Source attribute text
	 * @return The sourceID attribute
	 */
	private String getSourceId(String source)
	{
		NodeList nodeList;

		nodeList = DomUtils.getElementList(getResponseDocument().getDocumentElement(), "RECORD");

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Element recordElement = (Element) nodeList.item(i);

			if (source.equals(recordElement.getAttribute("source")))
			{
				return recordElement.getAttribute("sourceID");
			}
		}
		return "unavailable";
	}
}