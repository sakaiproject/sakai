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

import edu.indiana.lib.twinpeaks.util.*;

import lombok.extern.slf4j.Slf4j;
import org.osid.repository.AssetIterator;

import java.net.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Result rendering - base class and helpers
 */
@Slf4j
public abstract class SearchResultBase implements SearchResultInterface
{
	/**
	 * Parse the search engine response and expose pertinent results
	 */
	private	ArrayList				_itemList;
	private AssetIterator		_assetIterator;
	private int							_start;
	private int							_count;

	private String					_nextPreviewPage;
	private String 					_previousPreviewPage;

  protected String  			_searchQuery;
  protected byte					_searchResponseBytes[];
  protected String				_searchResponseString;
  protected Document			_searchResponseDocument;
	protected String				_database;

	protected String				_sessionId;
	protected String				_baseUrl;

	/**
	 * Constructor
	 */
  public SearchResultBase() {
    super();
  }

	/*
	 * Interface methods
	 */

	/**
	 * Save various attributes of the general search request
	 * @param query The QueryBase extension that sent the search request
	 */
  public void initialize(QueryBase query)
  {
  	_searchQuery						= query.getRequestParameter("searchString");
  	_database								= query.getRequestParameter("database");
  	_sessionId							= query.getRequestParameter("guid");
  	_searchResponseString		= query.getResponseString();
  	_searchResponseBytes		= query.getResponseBytes();
    _searchResponseDocument	= parseResponse();

    _itemList 							= new ArrayList();
    _start									= 1;

    saveBaseUrl(query.getUrl());
	}

	/**
	 * Add a MatchItem object
	 * @param item MatchItem to add
	 */
	public void addItem(MatchItem item) {
		_itemList.add(item);
	}

	/**
	 * Fetch the original query text
	 * @return Search string
	 */
	public String getQuery() {
		return _searchQuery;
	}

	/**
	 * Return the starting item number for this search (one based)
	 * @return Starting item number
	 */
	public int getSearchStart() {
		return _start;
	}

	/**
	 * Set the starting item number for this search (one based)
	 * @param start Starting item number
	 */
	public void setSearchStart(int start) {
		_start = start;
	}

	/**
	 * Set the starting item number for this search (one based)
	 * @param start Starting item number
	 */
	public void setSearchStart(String start) {
    try {
      _start = Integer.parseInt(start);
    } catch (NumberFormatException exception) {
      log.warn("Invalid number format: " + start);
      return;
    }
	}

	/**
	 * Return the count of matching items returned
	 * @return Item count
	 */
	public int getMatchCount() {
		return _itemList.size();
	}

	/**
	 * Fetch the "next preview page" reference (used to paginate results
	 * null if none)
	 * @return Next page reference
	 */
	public String getNextPreviewPage() {
		return _nextPreviewPage;
	}

	/**
	 * Set the "next preview page" reference
	 * @param reference Next page reference
	 */
	public void setNextPreviewPage(String reference) {
		_nextPreviewPage = reference;
	}

	/**
	 * Fetch the "previous preview page" reference (used to paginate results,
	 * null if none)
	 * @return Previous page reference
	 */
	public String getPreviousPreviewPage() {
		return _previousPreviewPage;
	}

	/**
	 * Set the "previous preview page" reference
	 * @param reference Previous page reference
	 */
	public void setPreviousPreviewPage(String reference) {
		_previousPreviewPage = reference;
	}

	/**
	 * Can this display be paginated (next/previous pages for display)?
	 * @return true if so
	 */
	public boolean canPaginate() {
		return (_previousPreviewPage != null) || (_nextPreviewPage != null);
	}

	/**
	 * Get an iterator to the result list
	 * @return SearchResult Iterator
	 */
	public Iterator iterator() {
		return _itemList.iterator();
	}

	/**
	 * Return the MatchItem list as a simple array
	 * @return MatchItem array
	 */
	public MatchItem[] toArray() {
		return (MatchItem[]) _itemList.toArray(new MatchItem[_itemList.size()]);
	}

	/**
	 * Return search results as a String
	 * @return Result Document
	 */
	public String getSearchResponseString() {
		return _searchResponseString;
	}

	/*
	 * Helpers
	 */

	/**
	 * Return search results as a Document
	 * @return Result Document
	 */
	public Document getSearchResponseDocument() {
		return _searchResponseDocument;
	}

	/**
	 * Parse the search engine response as HTML.
	 * See <code>initialize()</code> (override as reqired)
	 * @return Response as a DOM Document
	 */
	protected Document parseResponse() throws SearchException {
	  try {
		  return DomUtils.parseHtmlBytes(_searchResponseBytes);
		} catch (Exception exception) {
		  throw new SearchException(exception.toString());
		}
	}

  /**
   * Save the request URL base (the server portion only)
   * @param url Request URL (with or without parameters)
   */
  public void saveBaseUrl(String url) {
    _baseUrl = HttpTransactionUtils.getServer(url);
  }

	/**
	 * Form a full URL (protocol, server, arguments) from a base URL and
	 * provided parameters
	 * @param baseUrl The base (or template) URL
	 * @param urlFragment The (possibly) relative URL to be combined with the base
	 * @return A full URL (as a String)
	 */
  public String getFullUrl(String baseUrl, String urlFragment) {
  	String thisUrl = baseUrl;

  	if (thisUrl == null) {
  		thisUrl = _baseUrl;
  	}

		if (thisUrl != null) {
			try {
	 			URL	base = new URL(thisUrl);

	 			return new URL(base, urlFragment).toString();

	 		} catch (MalformedURLException exception) {
	 			throw new SearchException(exception.toString());
	 		}
	 	}
		return urlFragment;
	}

	/**
	 * Form a full URL (protocol, server, arguments) from a provided URL
	 * and a previously provided base URL
	 * @param urlFragment The (possibly) relative URL to be combined with the base
	 * @return A full URL (as a String)
	 */
  public String getFullUrl(String urlFragment) {
		return getFullUrl(null, urlFragment);
	}

	/**
	 * Prepend proxy string (if not already present)
	 * @param url Target URL
	 * @param proxy Proxy specification
	 * @return (Possibly) updated URL string
	 */
	public String prependProxy(String url, String proxy) {
		StringBuilder fullUrl;

		log.debug("prependProxy: proxy [" + proxy + "] vs. [" + url + "]");

		if (StringUtils.isNull(proxy)) {
			return url;
		}

		if (url.startsWith(proxy)) {
			return url;
		}

		fullUrl = new StringBuilder(proxy);
		fullUrl.append(url);

		return fullUrl.toString();
	}

	/**
	 * Verify we have the expected number of Elements in a Node list
	 * @param nodeList List of collected Elements
	 * @param expected Number of Elements we expect to see
	 * @return true If we have the expected number Elements
	 */
	public boolean expectedNodeCount(NodeList nodeList, int expected) {
		String	tag;
		int			length;

		if ((length = nodeList.getLength()) == expected) {
			return true;
		}

		tag = "Element";

		if (length > 0) {
			tag = nodeList.item(0).getNodeName();
		}

    log.debug("Unexpected "
    								+		tag
    								+ 	" count: "
    								+ 	length
    								+ 	" (ignoring entry)");
		return false;
	}

 	/**
	 * Locate select attribute of the first matching image
	 * @param parent Parent element (look here for IMG tag)
	 * @param name Attribute name (src, alt, etc)
	 * @return Image name value (null if none)
	 */
  public String getImageAttribute(Element parent, String name) {
    Element image;
    String	value;

 		if ((image = DomUtils.getElement(parent, "IMG")) == null) {
 		  return null;
 		}

    value = image.getAttribute(name);
    return StringUtils.isNull(value) ? null : value;
	}
}