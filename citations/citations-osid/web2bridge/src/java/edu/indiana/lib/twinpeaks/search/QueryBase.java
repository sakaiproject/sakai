/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.search;

import edu.indiana.lib.twinpeaks.util.*;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.w3c.dom.*;

/**
 * This module provides a single, core implementation of QueryInterface.
 * It provides "lowest common denominator" functionality.
 *
 * In reality, each search application should extend QueryBase
 * and implement appropriate methods.  See HttpTransactionQueryBase.java
 * for an example.
 */
public abstract class QueryBase implements QueryInterface {

	private static org.apache.commons.logging.Log	_log 	= LogUtils.getLog(QueryBase.class);
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