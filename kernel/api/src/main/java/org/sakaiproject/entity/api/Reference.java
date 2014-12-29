/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.entity.api;

import java.util.Collection;

/**
 * <p>
 * Reference holds an immutable(?) reference to a Sakai entity.
 * </p>
 */
public interface Reference
{
	/**
	 * Add the AuthzGroup(s) for context as a site.
	 * 
	 * @param rv
	 *        The list of references.
	 */
	void addSiteContextAuthzGroup(Collection<String> rv);

	/**
	 * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
	 * 
	 * @param rv
	 *        The list of references.
	 * @param id
	 *        The user id.
	 */
	void addUserAuthzGroup(Collection<String> rv, String id);

	/**
	 * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
	 * 
	 * @param rv
	 *        The list of references.
	 * @param id
	 *        The user id.
	 */
	void addUserTemplateAuthzGroup(Collection<String> rv, String id);

	/**
	 * Access a single container id, the from most general (or only)
	 * 
	 * @return The single or most general container, if any.
	 */
	String getContainer();

	/**
	 * Access the context id, if any.
	 * Typically this is the site in which the entity is contained, although other contexts are possible.
	 * 
	 * @return the context id, if any.
	 */
	String getContext();

	/**
	 * @return a description of the resource referenced.
	 */
	String getDescription();

	/**
	 * Find the Entity that is referenced.
	 * 
	 * @return The Entity object that this references.
	 */
	Entity getEntity();

	/**
	 * Access the primary id.
	 * 
	 * @return The primary id.
	 */
	String getId();

	/**
	 * Find the ResourceProperties object for this reference.
	 * 
	 * @return A ResourcesProperties object found (or constructed) for this reference.
	 */
	ResourceProperties getProperties();

	/**
	 * Compute the set of AuthzGroup ids associated with this referenced resource.
	 * 
	 * @return List of AuthzGroup ids (String) associated with this referenced resource.
	 */
	Collection<String> getAuthzGroups();

	/**
	 * Compute the set of AuthzGroup ids associated with this referenced resource, perhaps customized for security about this end user.
	 * 
	 * @param userId
	 *        the end user ID, or null if we want the generic set.
	 * @return List of AuthzGroup ids (String) associated with this referenced resource.
	 */
	Collection<String> getAuthzGroups(String userId);

	/**
	 * Access the reference.
	 * 
	 * @return The reference.
	 */
	String getReference();

	/**
	 * Access the subType.
	 * 
	 * @return The subType.
	 */
	String getSubType();

	/**
	 * Access the type, an application id string.  This value must uniquely identify the application responsible for the reference, and must be unchanging over time (it may end up stored in database values).
	 * 
	 * @return The type, an application id string.
	 */
	String getType();

	/**
	 * Access the URL which can be used to access the referenced resource.
	 * 
	 * @return The URL which can be used to access the referenced resource.
	 */
	String getUrl();

	/**
	 * Check if the reference's type is known
	 * 
	 * @return true if known, false if not.
	 */
	boolean isKnownType();

	/**
	 * Accept the settings for a reference - may be rejected if already set
	 * 
	 * @param type
	 * @param subType
	 * @param id
	 * @param container
	 * @param context
	 * @return true if settings are accepted, false if not.
	 */
	boolean set(String type, String subType, String id, String container, String context);

	/**
	 * Update the reference string.
	 *
	 * @param ref The new reference string.
	 */
	void updateReference(String ref);
	
	/**
	 * Access the entity producer responsible for the referenced entity.
	 * 
	 * @return The EntityProducer responsible for the referenced entity, or null if there is none.
	 */
	EntityProducer getEntityProducer();
}
