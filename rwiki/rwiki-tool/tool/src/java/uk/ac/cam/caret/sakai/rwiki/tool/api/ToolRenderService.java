/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package uk.ac.cam.caret.sakai.rwiki.tool.api;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;


public interface ToolRenderService
{

	/**
	 * Renders a public view of the page
	 * 
	 * @param rwo
	 *        the RWiki object
	 * @param user
	 *        The user
	 * @return and string containing the rendered content
	 */
	String renderPublicPage(RWikiObject rwo, boolean withBreadcrumbs);

	/**
	 * Renders a public view of the page
	 * 
	 * @param rwo
	 *        The RWikiObject representing the page
	 * @param user
	 *        The user making the request
	 * @param realm
	 *        The default realm to render in
	 * @return an string representing the rendered content
	 */
	String renderPublicPage(RWikiObject rwo, String realm,
			boolean withBreadcrumbs);

	/**
	 * Renders a view of the page
	 * 
	 * @param rwo
	 *        the RWiki object
	 * @param user
	 *        The user
	 * @return and string containing the rendered content
	 */
	String renderPage(RWikiObject rwo);

	/**
	 * Renders a view of the page
	 * 
	 * @param rwo
	 *        The RWikiObject representing the page
	 * @param user
	 *        The user making the request
	 * @param defaultRealm
	 *        The default realm to render in
	 * @return an string representing the rendered content
	 */
	String renderPage(RWikiObject rwo, String defaultRealm);

	String renderPage(RWikiObject rwo, boolean b);
}
