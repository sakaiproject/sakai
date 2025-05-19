/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sakaiproject.entity.api.Entity;

/**
 * <p>
 * EntityCollections provides utility methods for working with collections of entities and entity references
 * in the Sakai platform. This class facilitates common operations such as comparing, testing, and manipulating
 * collections of Entity objects and their reference strings.
 * </p>
 * <p>
 * The methods in this class help with operations like:
 * <ul>
 *   <li>Testing for intersection between collections of entity references and entity objects</li>
 *   <li>Checking if one collection is contained within another</li>
 *   <li>Verifying equality between collections of different types</li>
 *   <li>Converting between collections of entities and their reference strings</li>
 *   <li>Computing differences between collections</li>
 * </ul>
 * </p>
 * <p>
 * These utilities are particularly useful when working with Sakai's entity framework and
 * when implementing entity producers or consumers that need to manipulate collections of entities.
 * </p>
 */
public class EntityCollections {

	/**
	 * See if the collection of entity reference strings has at least one entity that is in the collection of Entity objects.
	 * 
	 * @param entityRefs
	 *        The collection (String) of entity references.
	 * @param entities
	 *        The collection (Entity) of entity objects.
	 * @return true if there is intersection, false if not.
	 */
	public static boolean isIntersectionEntityRefsToEntities(Collection<String> entityRefs, Collection<? extends Entity> entities) {
		if (entityRefs == null || entities == null || entityRefs.isEmpty() || entities.isEmpty()) {
			return false;
		}

		return entityRefs.stream()
			.anyMatch(ref -> entities.stream()
				.map(Entity::getReference)
				.anyMatch(ref::equals));
	}

	/**
	 * See if the collection of entity reference strings is contained in the collection of entities.
	 * 
	 * @param entityRefs
	 *        The collection (String) of entity references.
	 * @param entities
	 *        The collection (Entity) of entity objects.
	 * @return true if there is containment, false if not.
	 */
	public static boolean isContainedEntityRefsToEntities(Collection<String> entityRefs, Collection<? extends Entity> entities) {
		// null or empty sets are never contained
		if (entityRefs == null || entities == null || entities.isEmpty() || entityRefs.isEmpty()) {
			return false;
		}

		// Convert entities to a set of reference strings for more efficient lookups
		Collection<String> entityRefStrings = entities.stream()
			.map(Entity::getReference)
			.collect(Collectors.toSet());

		return entityRefStrings.containsAll(entityRefs);
	}

	/**
	 * See if the collection of entity reference strings matches completely the collection of Entity objects.
	 * 
	 * @param entityRefs
	 *        The collection (String) of entity references.
	 * @param entities
	 *        The collection (Entity) of entity objects.
	 * @return true if there is a match, false if not.
	 */
	public static boolean isEqualEntityRefsToEntities(Collection<String> entityRefs, Collection<? extends Entity> entities) {
		// If the collections are null or not the same size, they can't be equal
		if (entityRefs == null || entities == null || entityRefs.size() != entities.size()) {
			return false;
		}

		// Convert entities to a set of reference strings for more efficient lookups
		Collection<String> entityRefStrings = entities.stream()
			.map(Entity::getReference)
			.collect(Collectors.toSet());

		// Check if all entityRefs are in entityRefStrings
		if (entityRefs.stream().anyMatch(ref -> !entityRefStrings.contains(ref))) {
			return false;
		}

		// Check if all entityRefStrings are in entityRefs
		// This is needed in case there are duplicates in one collection but not the other
		return entityRefs.containsAll(entityRefStrings);
	}

	/**
	 * Test a collection of Entity objects for the specified entity reference
	 * 
	 * @param entities
	 *        The collection (Entity) of entities
	 * @param entityRef
	 *        The string entity reference to find.
	 * @return true if found, false if not.
	 */
	public static boolean entityCollectionContainsRefString(Collection<? extends Entity> entities, String entityRef) {
		if (entities == null || entityRef == null || entities.isEmpty()) {
			return false;
		}

		return entities.stream()
			.map(Entity::getReference)
			.anyMatch(entityRef::equals);
	}

	/**
	 * Test a collection of Entity reference Strings for the specified Entity
	 * 
	 * @param refs
	 *        The collection (String) of entity refs
	 * @param entity
	 *        The Entity to find.
	 * @return true if found, false if not.
	 */
	public static boolean refCollectionContainsEntity(Collection<String> refs, Entity entity) {
		if (refs == null || entity == null || refs.isEmpty()) {
			return false;
		}

		String targetRef = entity.getReference();
		return refs.contains(targetRef);
	}

	/**
	 * Set the refs collection to the entity reference strings from the entities collection (and nothing more)
	 * 
	 * @param refs
	 *        The collection (String) of entity references to modify.
	 * @param entities
	 *        The collection (Entity) of entity objects to use.
	 */
	public static void setEntityRefsFromEntities(Collection<String> refs, Collection<? extends Entity> entities) {
		if (refs == null || entities == null) {
			return;
		}

		refs.clear();

		if (!entities.isEmpty()) {
			entities.stream()
				.map(Entity::getReference)
				.forEach(refs::add);
		}
	}

	/**
	 * Fill in the two collections of Entity reference strings - those added in newEntities that were not in oldEntityRefs, 
	 * and those removed, i.e. in oldEntityRefs not in newEntities.
	 * 
	 * @param addedEntities
	 *        The collection to fill with references to entities that are in newEntities but not in oldEntityRefs.
	 * @param removedEntities
	 *        The collection to fill with references to entities that are in oldEntityRefs but not in newEntities.
	 * @param newEntities
	 *        The collection of new Entity objects to compare against oldEntityRefs.
	 * @param oldEntityRefs
	 *        The collection of old entity reference strings to compare against newEntities.
	 */
	public static void computeAddedRemovedEntityRefsFromNewEntitiesOldRefs(Collection<String> addedEntities,
																		   Collection<String> removedEntities,
																		   Collection<? extends Entity> newEntities,
																		   Collection<String> oldEntityRefs) {
		if (addedEntities == null || removedEntities == null || newEntities == null || oldEntityRefs == null) {
			return;
		}

		// Convert collections to sets for more efficient lookups
		Collection<String> newEntityRefs = newEntities.stream()
			.map(Entity::getReference)
			.collect(Collectors.toSet());

		// Find added entities (in newEntities but not in oldEntityRefs)
		newEntities.stream()
			.map(Entity::getReference)
			.filter(ref -> !oldEntityRefs.contains(ref))
			.forEach(addedEntities::add);

		// Find removed entities (in oldEntityRefs but not in newEntities)
		oldEntityRefs.stream()
			.filter(ref -> !newEntityRefs.contains(ref))
			.forEach(removedEntities::add);
	}
}
