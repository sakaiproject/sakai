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

package org.sakaiproject.portal.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Site;

/**
 * @author ieb
 *
 */
public interface PageFilter
{

	/**
	 * @param newPages
	 * @param site
	 * @return
	 */
	List filter(List newPages, Site site);

	/**
	 * Filter the list of placements, potentially making them hierachical if required
	 * @param l
	 * @param site
	 * @return
	 */
	List<Map> filterPlacements(List<Map> l, Site site);

}
