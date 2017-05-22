/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/LinkManager.java $
 * $Id: LinkManager.java 46822 2008-03-17 16:19:47Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
package org.sakaiproject.taggable.api;

import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;

public interface LinkManager
{
	
	/**
	 * Search a given list of links for one containing this tagCriteria 
	 * @param links
	 * @param tagCriteriaRef
	 * @return
	 */
	public Link lookupLink(List<Link> links, String tagCriteriaRef);

	/**
	 * Method to add a new link.
	 * 
	 * @param activityRef
	 *            A reference for the activity from which this link is being
	 *            created.
	 * @param tagCriteriaRef
	 *            The tagCriteriaRef to which this link is being created.
	 * @param rationale
	 *            The rationale for creating this link.
	 * @param rubric
	 *            Some rubric value.
	 * @param visible
	 *            True if this link should be made visible to those with
	 *            appropriate permissions, false otherwise.
	 * @param locked
	 *            True if this link should be locked, false otherwise.
	 * @return The link that has been added.
	 */
	public Link persistLink(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked);
	
	
	/**
	 * Method to get a link uniquely identified by the given reference.
	 * 
	 * @param ref
	 *            The link's reference.
	 * @return The link that matches the given reference.
	 * @throws IdUnusedException
	 *             Exception thrown if the id from the reference doesn't match
	 *             any records.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public Link getLink(String ref) throws IdUnusedException,
			PermissionException;

	/**
	 * Method to get the link between the activity identified by the given
	 * reference and the given tagCriteria.
	 * 
	 * @param activityRef
	 *            A reference for the activity to which this link is being
	 *            created.
	 * @param tagCriteriaRef
	 *            The tagCriteriaRef to search for.
	 * @return The matching link.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public Link getLink(String activityRef, String tagCriteriaRef)
			throws PermissionException;

	/**
	 * Method to get a list of links to the activity identified by the given
	 * activity reference.
	 * 
	 * @param activityRef
	 *            A reference for the activity from which the links have been
	 *            created.
	 * @param any
	 *            True to return all links, false to return only visible links.
	 * @param toContext
	 *            The context containing the criteria to which the links to be
	 *            viewed have been created.
	 * @return A list of links.
	 * @throws PermissionException
	 *             Exception thrown if current user doesn't have permission to
	 *             perform this action.
	 */
	public List getLinks(String activityRef, boolean any, String toContext)
			throws PermissionException;
	
	/**
	 * Method to get a list of links to the criteria ref
	 * @param criteriaRef The tagCriteriaRef to search for.
	 * @param any True to return all links, false to return only visible links.
	 * @return
	 * @throws PermissionException
	 */
	public List<Link> getLinks(String criteriaRef, boolean any) throws PermissionException;
	
	/**
	 * Method to remove individual links
	 * @param link The link to remove.
	 */
	public void removeLink(Link link);
	
	/**
	 * Method to remove all links for a specific activity
	 * @param activityRef The activity's reference
	 */
	public void removeLinks(String activityRef);

}
