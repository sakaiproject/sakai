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

public interface QueryInterface {


	/**
	 * Common initialization
	 * @param session Session context for this query
	 */
  public void initialize(SessionContext session);

	/**
	 * Do a query
	 */
  public void doQuery();

	/**
	 * Parse request parameters
	 * @param parameterMap A map of request details (name=value pairs)
	 */
  public void parseRequest(Map parameterMap);

	/**
	 * Fetch a request parameter by name
	 * @param name Parameter name
	 * @return Parameter value
	 */
  public String	getRequestParameter(String name);

  /**
   * Get query results
   * @return The results page (as a byte array)
   */
  public byte[] getResponseBytes();

  /**
   * Get query results
   * @return The results page (as a String)
   */
  public String getResponseString();
}
