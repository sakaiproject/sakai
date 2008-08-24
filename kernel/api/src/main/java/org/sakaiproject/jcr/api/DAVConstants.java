/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.api;


/**
 * @author ieb
 */
public class DAVConstants
{
	/* these are here to make windows XP work properly */
	public static final String DAV_CREATIONDATE = "DAV:creationdate";

	public static final String DAV_DISPLAYNAME = "DAV:displayname";

	public static final String DAV_GETCONTENTLANGUAGE = "DAV:getcontentlanguage";

	public static final String DAV_GETCONTENTLENGTH = "DAV:getcontentlength";

	public static final String DAV_GETCONTENTTYPE = "DAV:getcontenttype";

	public static final String DAV_GETETAG = "DAV:getetag";

	public static final String DAV_GETLASTMODIFIED = "DAV:getlastmodified";

	public static final String DAV_LOCKDISCOVERY = "DAV:lockdiscovery";

	public static final String DAV_RESOURCETYPE = "DAV:resourcetype";

	public static final String DAV_SOURCE = "DAV:source";

	public static final String DAV_SUPPORTEDLOCK = "DAV:supportedlock";

	/* property use by microsoft that are not specified in the RFC 2518 */
	public static final String DAV_ISCOLLECTION = "DAV:iscollection";

	// ---< Date Format Constants
	// >----------------------------------------------
	/**
	 * modificationDate date format per RFC 1123
	 */
	//public static DateFormat modificationDateFormat = new HttpDateFormat(
	//		"EEE, dd MMM yyyy HH:mm:ss z");

	/**
	 * Simple date format for the creation date ISO representation (partial).
	 */
	//public static DateFormat creationDateFormat = new HttpDateFormat(
	//		"yyyy-MM-dd'T'HH:mm:ss'Z'");

}
