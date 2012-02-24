/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	 * @return true if the service wants to be part of archive / merge, false if not.
	 */
	boolean willArchiveMerge();

	/**
	 * Archive the resources for the given site.
	 * 
	 * @param siteId
	 *        the id of the site.
	 * @param doc
	 *        The document to contain the xml.
	 * @param stack
	 *        The stack of elements, the top of which will be the containing element of the "service.name" element.
	 * @param archivePath
	 *        The path to the folder where we are writing auxilary files.
	 * @param attachments
	 *        This should be an empty List into which the implementation will put any attachments that are needed to support the 
	 *        archived content. Implementation will may use {@see List#contains(Object)} so choice of implementation should reflect this.
	 * @return A log of status messages from the archive.
	 */
	String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments);

	/**
	 * Merge the resources from the archive into the given site.
	 * 
	 * @param siteId
	 *        The id of the site getting imported into.
	 * @param root
	 *        The XML DOM tree of content to merge.
	 * @param archivePath
	 *        The path to the folder where we are reading auxilary files.
	 * @param fromSiteId
	 *        The site id from which these items were archived.
	 * @param attachmentNames
	 *        An empty map should be supplied and during the merge and any attachments that are renamed will be put into this map the key is the old
	 *        attachment name (as found in the DOM) and the value is the new attachment name.
	 * @param userIdTrans
	 *        A map supplied by the called containing keys of old user IDs and values of new user IDs that the content should be attributed to.
	 * @param userListAllowImport
	 *        A list of user IDs for which the content should be imported. An importer should ignore content if the user ID of the creator isn't in this
	 *        set.
	 * @return A log of status messages from the merge.
	 */
	String merge(String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans,
			Set<String> userListAllowImport);

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
	Collection<String> getEntityAuthzGroups(Reference ref, String userId);

	/**
	 * Get the HttpAccess object that supports entity access via the access servlet for my entities.
	 * 
	 * @return The HttpAccess object for my entities, or null if I do not support access.
	 */
	HttpAccess getHttpAccess();
}
