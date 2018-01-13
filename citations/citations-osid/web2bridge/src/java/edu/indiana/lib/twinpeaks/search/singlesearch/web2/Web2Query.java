/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008, 2009 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.singlesearch.web2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.indiana.lib.twinpeaks.search.HttpTransactionQueryBase;
import edu.indiana.lib.twinpeaks.search.SearchSource;
import edu.indiana.lib.twinpeaks.search.singlesearch.CqlParser;
import edu.indiana.lib.twinpeaks.util.DomException;
import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.SessionContext;
import edu.indiana.lib.twinpeaks.util.SessionTimeoutException;
import edu.indiana.lib.twinpeaks.util.StatusUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Send a query to the Muse Web2 interface
 */
@Slf4j
public class Web2Query extends HttpTransactionQueryBase {
	/**
	 * Records displayed "per page"
	 */
	public static final String RECORDS_PER_PAGE = "10";
	/**
	 * Records to fetch from each search target
	 */
	private static final String RECORDS_PER_TARGET = "30";
	/**
	 * Unique name for this search application
	 */
	private final String APPLICATION = SessionContext.uniqueSessionName(this);
	/**
	 * Web2 Bridge error code: No logged-in session
	 */
	private static final String NO_SESSION = "904";
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
   * Next RESULT record to request
   */
  private int _nextResult = 0;
	/**
	 * General synchronization
	 */
	private static Object _sync = new Object();

  /**
   * Constructor
   */
	public Web2Query() {
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
			 * Initialize a new session context block
			 */
			StatusUtils.initialize(getSessionContext(), getRequestParameter("targets"));
			/*
			 * LOGOFF any previous session
			 */
			clearParameters();

			doLogoffCommand();
	    submit();

			try
			{
				log.debug(DomUtils.serialize(getResponseDocument()));
			}
			catch (Exception ignore) { }
			/*
			 * LOGON
			 */
			clearParameters();

			doLogonCommand();
	    submit();
	    validateResponse("LOGON");
			/*
			 * FIND
			 */
			clearParameters();

			doFindCommand();
	    submit();
			validateResponse("FIND");
			setFindStatus();

			try
			{
				log.debug("Search response:");
				log.debug(DomUtils.serialize(getResponseDocument()));
			}
			catch (Exception ignore) { }

			return;
		}
		/*
		 * Request FIND results
		 */
		clearParameters();

		doResultsCommand(getFindResultSetId());
		submit();
		validateResponse("RESULTS");
	}

	/**
	 * Custom submit behavior (override HttpTransactionQueryBase)
	 */
	public int submit() {

		setWeb2InputMessage();
		return super.submit();
	}

	/*
	 * Helpers
	 */

	/**
   * Generate a LOGON command
   */
  private void doLogonCommand() throws SearchException {
  	Element logonElement;
		String 	username, password;

		username = getRequestParameter("username");
		password = getRequestParameter("password");

		try {
			doWeb2InputHeader();

			logonElement = addWeb2Input("LOGON");
			addWeb2Input(logonElement, "USER_ID", username);
			addWeb2Input(logonElement, "USER_PWD", password);

			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a LOGOFF command
   */
  private void doLogoffCommand() throws SearchException {

		try {
			doWeb2InputHeader();
			addWeb2Input("LOGOFF");
			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a SEARCH command
   */
  private void doSearchCommand() throws SearchException {
		Element element, searchElement;
		String sortBy, targets;

		/*
		 * Pick up the database(s) to examine, sort mode
		 */
		targets = getRequestParameter("targets");
		log.debug("Targets for search source " + _database + ": " + targets);

		log.debug("SEARCH FOR: " + getSearchString());

		sortBy = getRequestParameter("sortBy");
		if (StringUtils.isNull(sortBy))
		{
			sortBy = "ICERankingKeyRelevance";
		}
		log.debug("RANKING_KEY: " + sortBy);
		/*
		 * And generate the search command
		 */
		try {
			doWeb2InputHeader();

			searchElement = addWeb2Input("SEARCH");
			addWeb2Input(searchElement, "TERMS", getSearchString());
			addWeb2Input(searchElement, "QUERY_TYPE", "Muse");
			addWeb2Input(searchElement, "TARGETS", targets);
			addWeb2Input(searchElement, "START", "1");
			addWeb2Input(searchElement, "PER_TARGET", RECORDS_PER_TARGET);
			addWeb2Input(searchElement, "PER_PAGE", getIntegerRequestParameter("pageSize").toString());
	    addWeb2Input(searchElement, "RESULT_SET", getTransactionResultSetName());
			addWeb2Input(searchElement, "APPEND", "false");
			addWeb2Input(searchElement, "JITTERBUG_KEY");

			element = addWeb2Input(searchElement, "DEDUPE_KEY");
			element.setAttribute("dedupeMode", "");
			element.setAttribute("dedupeMixMode", "");

			element = addWeb2Input(searchElement, "RANKING_KEY", sortBy);
			element.setAttribute("rankingMode", "");
			element.setAttribute("rankingOrder", "");

			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a FIND command
   */
  private void doFindCommand() throws SearchException {
		Element element, searchElement;
		String 	pageSize, sortBy, targets;
		String  searchCriteria, searchFilter;
		int			active, targetCount;

		/*
		 * Set search criteria (use the search filter, if any is configured)
		 */
		searchFilter 		= SearchSource.getConfiguredParameter(_database, "searchFilter");
		searchCriteria 	= (searchFilter == null) ? "" : (searchFilter + " ");
		searchCriteria += getSearchString();

		/*
		 * Pick up the database(s) to examine, sort mode
		 */
		targets = getRequestParameter("targets");
		targetCount = new StringTokenizer(targets).countTokens();

		log.debug("Targets for search source " + _database + ", " + targetCount + " targets: " + targets);
		log.debug("Search for: " + searchCriteria);

		sortBy = getRequestParameter("sortBy");
		if (StringUtils.isNull(sortBy))
		{
			sortBy = "ICERankingKeyRelevance";
		}
		sortBy = "";
		log.debug("RANKING_KEY: " + sortBy);

		pageSize = getIntegerRequestParameter("pageSize").toString();
		log.debug("PAGE SIZE: " + pageSize);

		/*
		 * And generate the FIND command
		 */
		try
		{
			doWeb2InputHeader();

			searchElement = addWeb2Input("FIND");
			addWeb2Input(searchElement, "TERMS", searchCriteria);
			addWeb2Input(searchElement, "QUERY_TYPE", "Muse");
			addWeb2Input(searchElement, "TARGETS", targets);
	    addWeb2Input(searchElement, "FIND_SET", "sakaibrary");
			addWeb2Input(searchElement, "JITTERBUG_KEY");

	  	addWeb2Input(searchElement, "PER_PAGE", pageSize);
			addWeb2Input(searchElement, "PER_TARGET", pageSize);

			element = addWeb2Input(searchElement, "DEDUPE_KEY", "");
			element.setAttribute("dedupeMode", "");
			element.setAttribute("dedupeMixMode", "");

			element = addWeb2Input(searchElement, "RANKING_KEY", sortBy);
			element.setAttribute("rankingMode", "");
			element.setAttribute("rankingOrder", "");

			doWeb2InputClose();

			saveFindReferenceId(getTransactionId());

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a STATUS command
   */
  private void doStatusCommand() throws SearchException
  {
		Element statusElement;

		try
		{
			doWeb2InputHeader();

			statusElement = addWeb2Input("STATUS");
			addWeb2Input(statusElement, "ID", getFindReferenceId());

			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a COMBINE command
   */
  private void doCombineCommand() throws SearchException
  {
		Element combineElement;

		log.debug("COMBINE find sets: " + getFindResultSetId());
		log.debug("COMBINE output: " + getTransactionResultSetName());

		try
		{
			Element element;

			doWeb2InputHeader();

			combineElement = addWeb2Input("COMBINE");
			addWeb2Input(combineElement, "RESULT_SET", getFindResultSetId());
			addWeb2Input(combineElement, "OUTPUT_RESULT_SET", getTransactionResultSetName());

			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
   * Generate a RESULTS command
   */
  private void doResultsCommand(String resultSetId) throws SearchException {
		Element resultsElement;
		int			active, start, pageSize, perTarget;

		active 		= getSessionContext().getInt("active");
		start 		= getSessionContext().getInt("startRecord");
		pageSize	= getSessionContext().getInt("pageSize");
		perTarget = pageSize;

    _nextResult += Math.min(start, pageSize);

		log.debug("Results commmand: " + resultSetId);
		log.debug("Active = " + active + ", start record = " + _nextResult + ", page size = " + pageSize + ", per=target = " + perTarget);

		try
		{
			doWeb2InputHeader();

			resultsElement = addWeb2Input("RESULTS");
			addWeb2Input(resultsElement, "START", String.valueOf(_nextResult));
			addWeb2Input(resultsElement, "PER_PAGE", String.valueOf(pageSize));
			addWeb2Input(resultsElement, "PER_TARGET", String.valueOf(perTarget));
	    addWeb2Input(resultsElement, "RESULT_SET", resultSetId);

			doWeb2InputClose();

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
	}

	/**
	 * Create the Web2 input Document, add the standard Web2 XML header
	 */
	private void doWeb2InputHeader() throws DomException {
		setTransactionId();
		_web2Document = DomUtils.createXmlDocument("MUSEWEB2-INPUT");
	}

	/**
	 * Format the standard Web2 XML close
	 */
	private void doWeb2InputClose() {
		addReferenceId();
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

		log.debug( "Initial CQL Criteria: " + cql );

		parser 	= new CqlParser();
		result	= parser.doCQL2MetasearchCommand(cql);

		log.debug("Processed Result: " + result);
		return result;
	}

	/**
	 * Merge the STATUS and RESULTS response documents
	 */
	private void mergeResponseDocuments(Document statusDocument, Document resultsDocument)
	{
		_localResponseBytesReady = false;

		try
		{
			Element statusElement = DomUtils.getElement(statusDocument.getDocumentElement(), "STATUS");

			DomUtils.copyDocumentNode(statusElement, resultsDocument);

			_localResponseBytes = DomUtils.serialize(resultsDocument).getBytes("UTF-8");
			_localResponseBytesReady 	= true;

    }
    catch (Exception exception)
    {
      throw new SearchException(exception.toString());
    }
	}

	/**
	 * Set the xmlMessage parameter (this is the "command" sent to Web2)
	 * @param xml XML command
	 */
  private void setWeb2InputMessage() throws SearchException {
  	try {
			setParameter("xmlMessage", DomUtils.serialize(_web2Document));

		} catch (DomException exception) {
			throw new SearchException(exception.toString());
		}
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
		log.debug(active + " result set ids: " + ids);
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
		log.debug("Transaction result set name: " + resultSetName);
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
		String		error, id, message, status;

		document 	= getResponseDocument();
		element		= getElement(document.getDocumentElement(), action);
		error			= element.getAttribute("ERROR");
		status 		= element.getAttribute("STATUS");

		element		= getElement(document.getDocumentElement(), "REFERENCE_ID");
		id				= DomUtils.getText(element);

		if (!"false".equalsIgnoreCase(error)) {
			String text		= "Error "
			 		 					+ error
					 					+ ", status = "
										+ status
										+ ", for activity "
										+ action;

			log.debug("{} {}", text, document);

			if (status.equals(NO_SESSION)) {
				/*
				 * Session timeout is a special case
				 * o Re-initialize (clear the query URL)
				 * o Set "global failure" status
				 * o Throw the exception
				 */
				removeQueryUrl(APPLICATION);
				StatusUtils.setGlobalError(getSessionContext(), status, "Session timed out");
				throw new SessionTimeoutException();
			}

			element		= getElement(document.getDocumentElement(), "DATA");
			if ((message		= DomUtils.getText(element)) == null)
			{
				message = "";
			}

			StatusUtils.setGlobalError(getSessionContext(), status, message);

			if (!StringUtils.isNull(message)) {
				text 	= "Error "
							+ status
							+ ": "
							+ message;
			}
			throw new SearchException(text);
		}

		if (!getTransactionId().equalsIgnoreCase(id)) {
			String text = "Transaction ID mismatch, expected "
									+	getTransactionId()
									+ ", found "
									+	id;

			log.debug("{} {}", text, document);
			StatusUtils.setGlobalError(getSessionContext(), "<internal>", text);

			throw new SearchException(text);
		}
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
			int				estimate, hits;
			/*
			 * Database
			 */
			element	= DomUtils.getElement(recordElement, "TARGET");
			target	= DomUtils.getText(element);
			map 		= StatusUtils.getStatusMapForTarget(getSessionContext(), target);
			/*
			 * Result set
			 */
			element = DomUtils.getElement(recordElement, "RESULT_SET");
			text 		= DomUtils.getText(element);
			map.put("RESULT_SET", ((text == null) ? "<none>" : text));
			/*
			 * Get the estimated result count
			 */
			element = DomUtils.getElement(recordElement, "ESTIMATE");
			if ((text	= DomUtils.getText(element)) == null)
			{
				text = "0";
			}
			estimate = Integer.parseInt(text);
			/*
			 * Any hits available?
			 */
			element = DomUtils.getElement(recordElement, "HITS");
			text 		= DomUtils.getText(element);
			hits		= (text == null) ? 0 : Integer.parseInt(text);
			/*
			 * One common failure mode for the database connectors is to return a
			 * positive estimated result count with no actual hits.
			 *
			 * So, to use results from this database, we need to find both an
			 * estimate and some hits.
			 */
			map.put("ESTIMATE", "0");
			map.put("STATUS", "DONE");

			if ((estimate > 0) && (hits > 0))
			{
				map.put("ESTIMATE", String.valueOf(estimate));
				total	+= estimate;

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