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

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

/**
 * This module provides a single, core implementation of QueryInterface.
 * It provides "lowest common denominator" functionality.
 *
 * In reality, each search application should extend QueryBase
 * and implement appropriate methods.  See HttpTransactionQueryBase.java
 * for an example.
 */
@Slf4j
public abstract class QueryBase implements QueryInterface {
  /*
   * Request parameters
   */
	private	Map			_parameterMap	= null;

	/*
	 * Abstract methods
	 */

	/**
	 * Fetch the current search URL
	 * @return The URL (as a String)
	 */
  public abstract String getUrl();

	/**
	 * Fetch the current search text
	 * @return The search string
	 */
  public abstract String getSearchString();

	/*
	 * Base implementations
	 */

	/**
	 * Populate user request parameters
	 * @param parameterMap Request details as a map (name=value pairs)
	 */
	protected void populateRequestParameters(Map parameterMap) {
	 	_parameterMap = parameterMap;
	}

	/**
	 * Parse user request parameters.  This base method supports only
	 * the standard, simple query format.  Override if necessary.
	 * @param parameterMap Request details (name=value pairs)
	 */
  public void parseRequest(Map parameterMap) {

		populateRequestParameters(parameterMap);

  	if (getRequestParameter("database") == null) {
      throw new IllegalArgumentException("Missing database name");
    }

  	if (getRequestParameter("searchString") == null) {
      throw new IllegalArgumentException("Missing search text");
    }
  }

	/**
	 * Fetch a request parameter by name
	 * @param name Parameter name
	 * @return Parameter value
	 */
  public String	getRequestParameter(String name) {
  	return (String) _parameterMap.get(name);
	}

	/**
	 * Fetch a request parameter by name
	 * @param name Parameter name
	 * @return Parameter value (an Integer)
	 */
  public Integer	getIntegerRequestParameter(String name) {
  	return (Integer) _parameterMap.get(name);
	}

	/**
	 * Fetch the entire request parameter Map
	 * @return Parameter Map
	 */
  public Map getRequestParameterMap() {
  	return _parameterMap;
  }
}