/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.util;

import java.util.Date;

import org.sakaiproject.component.cover.ServerConfigurationService;

public class PortalUtils
{

	/**
	 * Returns an absolute URL for "/library" servlet with CDN path as necessary
	 */
	public static String getLibraryPath()
	{
		return getCDNPath() + "/library/";
	}

	/**
	 * Returns an absolute for "/library/js" servlet with CDN path as necessary
	 */
	public static String getScriptPath()
	{
		return getLibraryPath() + "js/";
	}

	/**
	 * Returns the CDN Path or empty string (i.e. never null)
	 */
	public static String getCDNPath()
	{
		return ServerConfigurationService.getString("portal.cdn.path", "");
	}

	/**
	 * Returns the CDN query string or empty string (i.e. never null)
	 */
	public static String getCDNQuery()
	{
	
		long expire = ServerConfigurationService.getInt("portal.cdn.expire",0);
		String version = ServerConfigurationService.getString("portal.cdn.version");
		if ( expire < 1 && version == null ) return "";
		String retval = "?";
		if ( expire > 0 ) {
			Date dt = new Date();
			long timeVal = dt.getTime() / 1000; // Seconds...
			expire = timeVal / expire;
			retval = retval + "expire=" + expire;
			if ( version != null ) retval = retval + "&";
		}
		if ( version != null ) retval = retval + "version=" + version;
		return retval;
	}

}

