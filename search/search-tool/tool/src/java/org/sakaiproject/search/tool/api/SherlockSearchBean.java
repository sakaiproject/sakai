/**
 * Copyright (c) 2003-2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.search.tool.api;

import javax.servlet.http.HttpServletResponse;

public interface SherlockSearchBean
{
	/**
	 * Get the site name
	 * @return
	 */
	String getSiteName();

	/**
	 * get the search url
	 * @return
	 */
	String getSearchURL();

	/**
	 * get the update Url
	 * @return
	 */
	String getUpdateURL();

	/**
	 * get the Icon URL
	 * @return
	 */
	String getUpdateIcon();
	
	/**
	 * Stream the Icon
	 *
	 */
	void sendIcon(HttpServletResponse response);
	 /**
	 * get the name of the system
	 * @return
	 */
	String getSystemName();
}
