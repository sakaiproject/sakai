/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.indiana.lib.twinpeaks.net.HttpTransaction;
import edu.indiana.lib.twinpeaks.util.CookieUtils;
import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.HttpTransactionUtils;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.SessionContext;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Base class for HTTP search activities
 */
@Slf4j
public abstract class HttpTransactionQueryBase
											extends QueryBase
											implements HttpTransactionQueryInterface {
	/**
	 * Name of the cookie List (stored with session context)
	 */
	private static final String	COOKIELIST = "CookieList";
	/**
	 * The stored query URL (stored with session context)
	 */
	private static final String	QUERYURL   = "QueryUrl";
	/**
	 * Stored query form (stored with session context)
	 */
	private static final String	QUERYFORM  = "QueryForm";
	/**
	 * General purpose parameter name prefix
	 */
	private static final String GP_PREFIX   = "GP_";

  private HttpTransaction	_transaction;
  private boolean     		_followRedirects;
  private int		 	    		_redirectBehavior;
  private String					_method;
  private String      		_searchString;
  private String      		_url;
  private String      		_searchResult;
  private SessionContext	_session;

	/**
	 * Constructor
	 */
  public HttpTransactionQueryBase() {
    _transaction 			= null;
		_session					= null;
		_method 					= "POST";
    _followRedirects  = false;
    _searchString     = null;
    _url              = null;
    _searchResult     = null;
  }

	/**
	 * One time initialization
	 * @param session SessionContext object
	 */
	public void initialize(SessionContext session) {

		_session = session;

		if ((_session.get(COOKIELIST)) == null) {
			_session.put(COOKIELIST, CookieUtils.newCookieList());
		}
		_transaction = new HttpTransaction();
		_transaction.initialize((List) _session.get(COOKIELIST));
	}

	/**
	 * Set search URL
	 * @param url URL string
	 */
  public void setUrl(String url) {
    _url = url;
  }

	/**
	 * Set search URL
	 * @param url URL object
	 */
  public void setUrl(URL url) {
    _url = url.toString();
  }

	/**
	 * Fetch the current search URL
	 * @return The URL (as a String)
	 */
  public String getUrl() {
    return _url;
  }

	/**
	 * Specify the search text
	 * @param searchString Text to look for
	 */
  public void setSearchString(String searchString) {
    _searchString = searchString;
  }

	/**
	 * Fetch the current search text
	 * @return The search string
	 */
  public String getSearchString() {
    return _searchString;
  }

	/**
	 * Set the HTTP query method (post or get)
	 * @param method <code>METHOD_POST</code> or <code>METHOD_GET</code>
	 */
	public void setQueryMethod(String method) {
		_method = method;
	}

	/**
	 * Fetch the current HTTP query method
	 * @return The method (as text)
	 */
	public String getQueryMethod() {
		return _method;
	}

	/**
	 * Fetch a named HTTP response parameter
	 * @param name Parameter name
	 * @return Parameter value
	 */
	public String getResponseHeader(String name) {
		return _transaction.getResponseHeader(name);
	}

	/**
	 * Set the default character set for this transaction
	 * @param cs Character set (UTF-8, ISO-8859-1, etc)
	 */
	public void setDefaultCharacterSet(String cs) {
		_transaction.setDefaultCharacterSet(cs);
	}

	/**
	 * Fetch the response character set
	 * @return Character set designation (as a String)
	 */
	public String getResponseCharacterSet() {
		return _transaction.getResponseCharacterSet();
	}

	/*
	 * The following "query" methods are used only for EBSCO - should
	 * they be moved to EbscoQueryBase?
	 */

	/**
	 * Create the session context name for a specified consumer
	 * @param base The base name for session context object
	 * @param consumer A unique name for the "user"
	 * @return Full session context name
	 */
	private String scn(String base, String consumer) {
		StringBuilder name = new StringBuilder(base);

		if (!StringUtils.isNull(consumer))
		{
			name.append('.');
			name.append(consumer);
		}
		return name.toString();
	}

	/**
	 * Create the session context name for a specified consumer/parameter pair
	 * @param base The base name for session context object
	 * @param consumer A unique name for the "user"
	 * @return Full session context name
	 */
	private String gp_scn(String base, String consumer) {
		StringBuilder name = new StringBuilder(GP_PREFIX);

		name.append(scn(base, consumer));
		return name.toString();
	}

	/**
	 * Save the URL for the query page
	 * @param consumer A unique name for the "user" of this object
	 * @param queryUrl Address of the final query page
	 */
	public void setQueryUrl(String consumer, String queryUrl) {
		_session.put(scn(QUERYURL, consumer), queryUrl);
	}

	/**
	 * Fetch the URL for the query
	 * @param consumer A unique name for the "user" of this object
	 * @return Address of the final query page
	 */
	public String getQueryUrl(String consumer) {
		return (String) _session.get(scn(QUERYURL, consumer));
	}

	/**
	 * Delete a stored query URL
	 * @param consumer A unique name for the "user" of this object
	 */
	public void removeQueryUrl(String consumer) {
		_session.remove(scn(QUERYURL, consumer));
	}

	/**
	 * Save the final query form as a DOM document
	 * @param consumer A unique name for the "user" of this object
	 * @param queryForm Query page as a DOM document
	 */
	public void setQueryDocument(String consumer, Document queryForm) {
		_session.put(scn(QUERYFORM, consumer), queryForm);
	}

	/**
	 * Fetch the final query form as a DOM document
	 * @param consumer A unique name for the "user" of this object
	 * @return Query form (as a DOM document)
	 */
	public Document getQueryDocument(String consumer) {
		return (Document) _session.get(scn(QUERYFORM, consumer));
	}

	/**
	 * Save a general purpose parameter
	 * @param consumer A unique name for the "user" of this object
	 * @param name Parameter name
	 * @param value Parameter value
	 */
	public void setSessionParameter(String consumer, String name, String value) {
		_session.put(gp_scn(name, consumer), value);
	}

	/**
	 * Fetch the requested general purpose parameter
	 * @param consumer A unique name for the "user" of this object
	 * @param name Parameter name
	 * @return Parameter value (null if none)
	 */
	public String getSessionParameter(String consumer, String name) {
		return (String) _session.get(gp_scn(name, consumer));
	}

	/**
	 * Save a general purpose parameter
	 * @param consumer A unique name for the "user" of this object
	 * @param name Parameter name
	 * @param value Parameter value
	 */
	public void setSessionValue(String consumer, String name, Object value) {
		_session.put(gp_scn(name, consumer), value);
	}

	/**
	 * Delete the requested general purpose parameter
	 * @param consumer A unique name for the "user" of this object
	 * @param name Parameter name
	 */
	public void removeSessionParameter(String consumer, String name) {
		_session.remove(gp_scn(name, consumer));
	}

	/**
	 * Fetch the requested general purpose parameter
	 * @param consumer A unique name for the "user" of this object
	 * @param name Parameter name
	 * @return Parameter value (null if none)
	 */
	public Object getSessionValue(String consumer, String name) {
		return _session.get(gp_scn(name, consumer));
	}


	/**
	 * Get the SessionContext object for this user
	 * @return The current SessionContext
	 */
	public SessionContext getSessionContext() {
		return _session;
	}

	/**
	 * Establish a mechanism for handling redirects
	 * @param behavior Specifies the desired behavior.  Use one of:
	 *<ul>
	 *<li> REDIRECT_AUTOMATIC						- <code>URLConnection</code> handles
	 																			all redirects
	 *<li> REDIRECT_MANAGED   					- The <code>submit()</code> code
	 *																	  handles any redirects
	 *<li> REDIRECT_MANAGED_SINGLESTEP	- The caller will handle each redirect
	 *</ul>
	 */
  public void setRedirectBehavior(int behavior) throws SearchException {

    switch (behavior) {
    	case REDIRECT_AUTOMATIC:
		    _followRedirects = true;
		    break;

		  case REDIRECT_MANAGED:
		  case REDIRECT_MANAGED_SINGLESTEP:
		   	_followRedirects = false;
		  	break;

		  default:
		  	throw new SearchException("Invalid redirect behavior: " + behavior);
		}
    _redirectBehavior	= behavior;
  }

	/**
	 * Set the "file preservation state" for getBaseUrlSpecification()
	 * @param state true to preserve URL file portion
	 */
	public void setPreserveBaseUrlFile(boolean state) {
		_transaction.setPreserveBaseUrlFile(state);
	}
	/**
	 * Should URLConnection follow redirects?
	 * @return true if URLConnection should handle redirects
	 */
  public boolean getFollowRedirects() {
    return _followRedirects;
  }

  /**
   * Set up a name=value pair
   * @param name Parameter name
   * @param value Parameter value
   */
  public void setParameter(String name, String value) {
    _transaction.setParameter(name, value);
  }

	/**
	 * Get a named parameter
   * @param name Parameter name
   * @return Parameter value
	 */
  public String getParameter(String name) {
    return _transaction.getParameter(name);
  }

	/**
	 * Get the parameter name associated with the 1st occurance of this value
   * @param value Parameter value
   * @return Parameter name
	 */
  public String getParameterName(String value) {
    return _transaction.getParameterName(value);
  }

	/**
	 * Clear the parameter list
	 */
	public void clearParameters() {
		_transaction.clearParameters();
	}

	/**
	 * Submit a request (POST or GET) and read the response.  Various aspects
	 * of the response can be inspected using the "getXXX()" methods.
	 * @return Submission status code (200 = success)
	 */
  public int submit() throws SearchException {
  	int status;

    /*
     * Send the request
     */
    try {
      _transaction.setFollowRedirects(_followRedirects);
      _transaction.setTransactionType(_method);

  		status = _transaction.doTransaction(_url);

      switch (_redirectBehavior) {
      	case REDIRECT_AUTOMATIC:
      	case REDIRECT_MANAGED_SINGLESTEP:
      		return status;

      	default:
      		break;
      }
			/*
			 * Were we redirected to another page?  If so, try to fetch
			 */
    	while (HttpTransactionUtils.isHttpRedirect(status)) {
    		String 	location 	= _transaction.getResponseHeader("Location");
    		String 	baseUrl		= _transaction.getBaseUrlSpecification();
    		URL			fullUrl		= newFullUrl(baseUrl, location);

    		setUrl(fullUrl);

    		_transaction.setTransactionType("GET");
	  		status = _transaction.doTransaction(fullUrl);
  		}
  		/*
  		 * Done, return final status
  		 */
		  return status;

    } catch (Exception exception) {
    	log.error("Exception seen, the current URL is \"" + getUrl() + "\"");
    	log.error(exception.getMessage(), exception);
      throw new SearchException(exception.toString());
    }
  }

	/**
   * Get the server response text
   * @return The response (as a String)
   */
  public String getResponseString() {
    return _transaction.getResponseString();
  }

	/**
   * Get the server response text
   * @return The response (as a byte array)
   */
  public byte[] getResponseBytes() {
    return _transaction.getResponseBytes();
  }

	/**
	 * Parse the server response (override as required)
	 * @return Response Document
	 */
  public Document getResponseDocument() throws SearchException {
  	try {
    	return DomUtils.parseHtmlBytes(getResponseBytes());

    } catch (Exception exception) {
      throw new SearchException(exception.toString());
    }
  }

  /*
   * Helpers
   */

 	/**
 	 * Locate the HTML BODY element in the page document
 	 * @param pageDocument An HTML page (as a DOM)
 	 * @return The body Element
 	 */
	public Element getBody(Document pageDocument) {
    Element		root = pageDocument.getDocumentElement();

    return DomUtils.getElement(root, "BODY");
	}

	/**
	 * Construct a new URL from base and relative components
	 * @param baseComponent Base URL - the relative URL is added to this
	 * @param relativeComponent A partial (or full) URL that represents our target
	 * @return A full URL composed of the relative URL combined with "missing"
	 * 				 portions taken from the base
	 */
	public URL newFullUrl(String baseComponent, String relativeComponent) {
		try {
 			URL	baseUrl	= new URL(baseComponent);
 			return new URL(baseUrl, relativeComponent);

 		} catch (MalformedURLException exception) {
 			throw new SearchException(exception.toString());
 		}
	}

	/**
	 * Set query parameters based on page-wide INPUTs
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param nameList A list of the parameters we're looking for
	 * @deprecated Replaced by {@link #setParametersFromInputNames()}
	 */
  public void setParametersFromInputs(Document pageDocument, List nameList) {
    setParametersFromInputNames(pageDocument, nameList);
  }

	/**
	 * Set query parameters based on page-wide INPUTs
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param nameList A list of the parameters we're looking for
	 */
  public void setParametersFromInputNames(Document pageDocument, List nameList) {
    setParametersFromNameList(DomUtils.getElementList(getBody(pageDocument), "INPUT"),
    													nameList);
  }

	/**
	 * Set query parameters based on page-wide INPUTs
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param nameList A list of the parameters we're looking for
	 */
  public void setParametersFromInputValues(Document pageDocument, List nameList) {
    setParametersFromValueList(DomUtils.getElementList(getBody(pageDocument), "INPUT"),
    													 nameList);
  }

	/**
	 * Produce a target URL for this query by combining the form "action" value
	 * with the base URL of the query page
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param formName The name of the FORM to lookup
	 *									(eg <code>FORM name="formName"</code>)
	 * @param nameList A list of the parameters we're looking for
	 */
  public void setParametersFromFormInputs(Document 	pageDocument,
  																				String 		formName,
  																				List   		nameList)
  																				throws SearchException {
    Element	formElement;

    if ((formElement = getFormElement(pageDocument, formName)) == null) {
	    throw new SearchException("No such form: " + formName);
		}
		setParametersFromElementInputs(formElement, nameList);
	}

	/**
	 * Set query parameters based on INPUTs within an Element
	 * @param element The base element (often a FORM)
	 * @param nameList A list of the parameters we're looking for
	 */
  private void setParametersFromElementInputs(Element element, List nameList) {
    setParametersFromNameList(DomUtils.getElementList(element, "INPUT"), nameList);
  }

	/**
	 * Set query parameters based on element names (save name=value pairs)
	 * @param nodeList List of Elements to evaluate
	 * @param nameList A list of the parameters we're looking for
	 */
  public void setParametersFromNameList(NodeList nodeList, List nameList) {
  	setParametersFromList(nodeList, KEY, "name", "value", nameList);
  }

	/**
	 * Set query parameters based on element values (save name=value pairs)
	 * @param nodeList List of Elements to evaluate
	 * @param nameList A list of the parameters we're looking for
	 */
  public void setParametersFromValueList(NodeList nodeList, List nameList) {
  	setParametersFromList(nodeList, VALUE, "value", "name", nameList);
  }

	/**
	 * {@link #setParametersFromInputNames()}: Use one of KEY or VALUE as the saved parameter name
	 */
	private static final int KEY 		= 0;
	private static final int VALUE	= 1;

	/**
	 * Set query parameters based on element attributes
	 * @param nodeList List of Elements to evaluate
	 * @param useAsParameterName Use one of KEY or VALUE as the saved parameter name
	 * @param key Parameter "name"
	 * @param value Parameter "value"
	 * @param nameList A list of the parameters we're looking for
	 */
  private void setParametersFromList(NodeList nodeList, int useAsParameterName,
  				  												 String key, String value, List nameList) {
    int	nodeSize	= nodeList.getLength();

    for (int i = 0; i < nodeSize; i++) {
      Element element				= (Element) nodeList.item(i);
      String	fetchedValue	= element.getAttribute(key);

      if (nameList.contains(fetchedValue)) {

      	switch (useAsParameterName) {
      		case KEY:
      		 	setParameter(fetchedValue, element.getAttribute(value));
      		 	break;

      		case VALUE:
      		 	setParameter(element.getAttribute(value), fetchedValue);
      		 	break;

      		default:
      			throw new IllegalArgumentException("Unknown name selection: "
      																			+ 	useAsParameterName);
				}
      }
    }
  }

	/**
	 * Produce a target URL for this query by combining an anchor "href" value
	 * with the base URL of the query page
	 * @param anchor Anchor element
	 */
  public void setUrlFromAnchor(Element anchor) throws SearchException {
    String href = anchor.getAttribute("href");

		try {
			setUrl(newFullUrl(_transaction.getBaseUrlSpecification(), href));

	  } catch (MalformedURLException exception) {
	    throw new SearchException(exception.toString());
	  }
  }

	/**
	 * Produce a target URL for this query by combining the form "action" value
	 * with the base URL of the query page
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param formName The name of the FORM to lookup
	 *									(eg <code>FORM name="formName"</code>)
	 */
  public void setUrlFromForm(Document pageDocument, String formName) throws SearchException {
    Element	form;

    if ((form = getFormElement(pageDocument, formName)) == null) {
	    throw new SearchException("No such form: " + formName);
		}

		try {
			setUrl(newFullUrl(_transaction.getBaseUrlSpecification(),
	     								  form.getAttribute("action")));
	  } catch (MalformedURLException exception) {
	    throw new SearchException(exception.toString());
	  }
  }

	/**
	 * Find a named FORM element
	 * @param pageDocument The search engine query page (as a DOM Document)
	 * @param formName The name of the FORM to lookup
	 *									(eg <code>FORM name="formName"</code>)
	 */
  public Element getFormElement(Document pageDocument, String formName) {
 		return DomUtils.selectFirstElementByAttributeValue(getBody(pageDocument),
 																											 "FORM",
 																											 "name", formName);
  }
 }