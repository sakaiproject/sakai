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

import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.portal.util.URLUtils;

public class PortalBean
{

    private String caller = "";

    public void setCaller(String caller)
    {
        this.caller = caller;
    }

	/**
	 * Returns an absolute URL for "/library" servlet with CDN path as necessary
	 */
	public String getLibraryPath()
	{
        return PortalUtils.getLibraryPath();
	}

	/**
	 * Returns an absolute for "/library/js" servlet with CDN path as necessary
	 */
	public String getScriptPath()
	{
        return PortalUtils.getScriptPath();
	}

	/**
	 * Returns the CDN Path or empty string (i.e. never null)
	 */
	public String getCDNPath()
	{
        return PortalUtils.getCDNPath();
	}

	/**
	 * Returns the CDN query string or empty string (i.e. never null)
	 */
	public String getCDNQuery()
	{
        return PortalUtils.getCDNQuery();
	}

	/**
	 * Returns the text to intelligently include the latest version of jQuery
	 */
	public String getLatestJQuery()
	{
        return PortalUtils.includeLatestJQuery(caller);
	}

}

