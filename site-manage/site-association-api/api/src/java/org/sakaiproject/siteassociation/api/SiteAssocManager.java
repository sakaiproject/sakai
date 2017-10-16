/**
 * Copyright (c) 2003-2009 The Apereo Foundation
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
package org.sakaiproject.siteassociation.api;

import java.util.List;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public interface SiteAssocManager {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public Site getSite(String context);
	
	/**
	 * 
	 * @param siteService
	 */
	public void setSiteService(SiteService siteService);
	
	/**
	 * Method to get a list of contexts from which associations have been made
	 * to the given context.
	 * 
	 * @param context
	 *            The context that associations have been made to.
	 * @return A list of contexts.
	 */
	public List<String> getAssociatedTo(String context);
	
	
	/**
	 * Method to get a list of contexts that the given context has been
	 * associated to.
	 * 
	 * @param context
	 *            The context that associations have been made from.
	 * @return A list of contexts.
	 */
	public List<String> getAssociatedFrom(String context);

	
	/**
	 * Method to create a unidirectional association from one context to
	 * another.
	 * 
	 * @param fromContext
	 *            The context from which to make the association.
	 * @param toContext
	 *            The context to make the association to.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public void addAssociation(String fromContext, String toContext);
	
	/**
	 * Method to remove the unidirectional association from one context to
	 * another.
	 * 
	 * @param fromContext
	 *            The context the association was made from.
	 * @param toContext
	 *            The context the association was made to.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public void removeAssociation(String fromContext, String toContext);
	
}
