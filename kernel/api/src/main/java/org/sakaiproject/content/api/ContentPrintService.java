/**********************************************************************************
 * $URL:  $
 * $Id: ContentPrintService.java 132652 2013-12-17 03:15:15Z zqian@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.content.api;

import java.util.List;
import java.util.HashMap;

/**
* <p>ContentCollection is the interface for print out resource object  .</p>
*
* @version $Revision:  $
*/
public interface ContentPrintService
{
	/**
	 * the print server url
	 */
	public static String CONTENT_PRINT_SERVICE_URL = "content_print_server_url";
	
	/**
	 * the print success result string
	 */
	public static String CONTENT_PRINT_SUCCESS = "content_print_success";
	
	/**
	 * the default print call not-implemented string
	 */
	public static String CONTENT_PRINT_NOT_IMPLEMENTED = "content_print_not_implemented";
	
	/**
	 * the content print call response status Strings
	 */
	public static String CONTENT_PRINT_RESPONSE_STATUS = "print_status";
	public static String CONTENT_PRINT_RESPONSE_STATUS_SUCCESS = "print_status_success";
	public static String CONTENT_PRINT_RESPONSE_STATUS_FAILURE = "print_status_failure";
	
	/**
	 * the content print call response message
	 */
	public static String CONTENT_PRINT_RESPONSE_MESSAGE = "print_message";
	
	/**
	 * the content print all response url
	 */
	public static String CONTENT_PRINT_RESPONSE_URL = "print_url";
	public static String CONTENT_PRINT_RESPONSE_URL_TITLE = "print_url_title";
	
	/**
	* Whether a Resource object is printable or not.
	* @return true if the resource is printable; false otherwise.
	*/
	public boolean isPrintable(ContentResource r);

	/**
	* Print the resource
	* @param params the parameters needed for printing
	* @return the information of print result (i.e. status code, message, URL, etc.)
	*/
	public HashMap<String, String> printResource(ContentResource r, List<Object> params);

}	// ContentPrintService



