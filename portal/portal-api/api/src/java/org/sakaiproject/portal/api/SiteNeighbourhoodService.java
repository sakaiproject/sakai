/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;

/**
 * The SiteNeighbourhoodService provides a list of sites in the neighbourhood of the current context.
 * This might be all sites, or it might be just the children, siblings and drect parents of a site.
 * It is returned as a flat list.
 * @author ieb
 *
 */
public interface SiteNeighbourhoodService
{

	/**
	 * Get a list of sites at the current node as defined by the request
	 * @param request
	 * @param session
	 * @param includeMyWorksite
	 * @return
	 */
	List<Site> getSitesAtNode(HttpServletRequest request, Session session, boolean includeMyWorksite);

}
