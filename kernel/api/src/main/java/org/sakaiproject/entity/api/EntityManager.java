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

import java.util.List;

/**
 * <p>
 * EntityManager is the API for managing EntityProducer services / managers.
 * </p>
 */
public interface EntityManager
{
	/**
	 * Access the list of managers that are registered EntityProducer.
	 * 
	 * @return List (EntityProducer) of managers that are registered EntityProducer.
	 */
	List<EntityProducer> getEntityProducers();

	/**
	 * Register this as an EntityProducer.
	 * 
	 * @param manager
	 *        The EntityProducer manager to register.
	 * @param referenceRoot
	 *        The prefix of all entity references handeled by this producer (i.e. "content" if you handle "/content/..." references)
	 */
	void registerEntityProducer(EntityProducer manager, String referenceRoot);

	/**
	 * Create a new Reference object, from the given reference string.
	 * 
	 * @param refString
	 *        The reference string.
	 * @return a new reference object made from the given reference string.
	 */
	Reference newReference(String refString);

	/**
	 * Create a new Reference object, as a copy of the given Reference object.
	 * 
	 * @param copyMe
	 *        The Reference object to copy
	 * @return a new Reference object, as a copy of the given Reference object.
	 */
	Reference newReference(Reference copyMe);

	/**
	 * Create a new List specially designed to hold References.
	 * 
	 * @return a new List specially designed to hold References.
	 */
	List<Reference> newReferenceList();

	/**
	 * Create a new List specially designed to hold References, as a copy of another.
	 * 
	 * @param copyMe
	 *        Make the new list contain a copy of this list.
	 * @return a new List specially designed to hold References, as a copy of another.
	 */
	List<Reference> newReferenceList(List<Reference> copyMe);

	/**
	 * Check for a valid reference.
	 * 
	 * @param ref
	 *        a reference string.
	 * @return true if the reference is valid, false if not.
	 */
	boolean checkReference(String ref);
}
