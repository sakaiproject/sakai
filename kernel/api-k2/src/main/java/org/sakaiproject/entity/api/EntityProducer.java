/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.entity.api;

import java.util.List;

import org.sakaiproject.entity.api.Entity.Permission;


/**
 * <p>
 * Services which implement EntityProducer declare themselves as producers of Sakai entities.
 * </p>
 */
public interface EntityProducer
{
	/**
	 * @return a short string identifying the resources kept here, good for a file name or label.
	 */
	String getLabel();

	/**
	 * Check that a permission is valid for the current user
	 * @param alias 
	 *        The alias
	 * @param test
	 *        The security permission being tested
	 * @return true if the current user has permission on the alias in question.
	 */
	boolean checkPermission(String alias, Permission test);


	/**
	 * If the service recognizes the reference as its own, parse it and fill in the Reference
	 * 
	 * @param reference
	 *        The reference string to examine.
	 * @param ref
	 *        The Reference object to set with the results of the parse from a recognized reference.
	 * @return true if the reference belonged to the service, false if not.
	 */
	boolean parseEntityReference(String reference, Reference ref);

	/**
	 * Create an entity description for the entity referenced - the entity will belong to the service.
	 * 
	 * @param ref
	 *        The entity reference.
	 * @return The entity description, or null if one cannot be made.
	 */
	String getEntityDescription(Reference ref);

	/**
	 * Access the resource properties for the referenced entity - the entity will belong to the service.
	 * 
	 * @param ref
	 *        The entity reference.
	 * @return The ResourceProperties object for the entity, or null if it has none.
	 */
	ResourceProperties getEntityResourceProperties(Reference ref);

	/**
	 * Access the referenced Entity - the entity will belong to the service.
	 * 
	 * @param ref
	 *        The entity reference.
	 * @return The Entity, or null if not found.
	 */
	Entity getEntity(Reference ref);

	/**
	 * Access a URL for the referenced entity - the entity will belong to the service.
	 * 
	 * @param ref
	 *        The entity reference.
	 * @return The entity's URL, or null if it does not have one.
	 */
	String getEntityUrl(Reference ref);

	/**
	 * Access a collection of authorization group ids for security on the for the referenced entity - the entity will belong to the service.
	 * 
	 * @param ref
	 *        The entity reference.
	 * @param userId
	 *        The userId for a user-specific set of groups, or null for the generic set.
	 * @return The entity's collection of authorization group ids, or null if this cannot be done.
	 */
	List<String> getEntityAuthzGroups(Reference ref, String userId);

	/**
	 * Get the HttpAccess object that supports entity access via the access servlet for my entities.
	 * 
	 * @return The HttpAccess object for my entities, or null if I do not support access.
	 */
	HttpAccess getHttpAccess();
}
