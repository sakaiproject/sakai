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
