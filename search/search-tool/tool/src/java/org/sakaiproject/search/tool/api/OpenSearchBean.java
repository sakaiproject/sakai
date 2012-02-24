/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.tool.api;

public interface OpenSearchBean
{
	/**
	 * get the human readable site name for OpenSearch
	 * @return
	 */
	  String getSiteName();
	  /**
	   * get the absoute icon url
	   * @return
	   */
	  String getIconUrl();
	  /**
	   * Get the attribution text
	   * @return
	   */
	  String getAttibution();
	  /**
	   * get the Sindication Rights
	   * @return
	   */
	  String getSindicationRight();
	  /**
	   * get the adult content setting (defaults to false)
	   * @return
	   */
	  String getAdultContent();
	  /**
	   * get the absolute search URL for HTML output
	   * @return
	   */
	  String getHTMLSearchTemplate();
	  /**
	   * get the absolute search URL for RSS output
	   * @return
	   */
	  String getRSSSearchTemplate();
	  /**
	   * get the Search Form URL
	   * @return
	   */
	  String getHTMLSearchFormUrl();
	  /**
		 * get the name of the system
		 * @return
		 */
		String getSystemName();

}
