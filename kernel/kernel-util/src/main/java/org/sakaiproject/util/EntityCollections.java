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
import java.util.Iterator;

import org.sakaiproject.entity.api.Entity;

/**
 * <p>
 * EntityUtil collects some entity utility methods dealing with collections of entities and entity references.
 * </p>
 */
public class EntityCollections
{
	/**
	 * See if the collection of entity reference strings has at least one entity that is in the collection of Entity objects.
	 * 
	 * @param entityRefs
	 *        The collection (String) of entity references.
	 * @param entities
	 *        The collection (Entity) of entity objects.
	 * @return true if there is interesection, false if not.
	 */
	public static boolean isIntersectionEntityRefsToEntities(Collection<String> entityRefs, Collection<Entity> entities)
	{
		for (Iterator<String> iRefs = entityRefs.iterator(); iRefs.hasNext();)
		{
			String findThisEntityRef = iRefs.next();
			for (Iterator<Entity> iEntities = entities.iterator(); iEntities.hasNext();)
			{
				String thisEntityRef = ((Entity) iEntities.next()).getReference();
				if (thisEntityRef.equals(findThisEntityRef))
				{
					return true;
				}
			}
		}

		return false;
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
	public static boolean isContainedEntityRefsToEntities(Collection<String> entityRefs, Collection<Entity> entities)
	{
		for (Iterator<String> iRefs = entityRefs.iterator(); iRefs.hasNext();)
		{
			String findThisEntityRef = iRefs.next();
			if (!entityCollectionContainsRefString(entities, findThisEntityRef)) return false;
		}

		return true;
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
	public static boolean isEqualEntityRefsToEntities(Collection<String> entityRefs, Collection<Entity> entities)
	{
		// if the colletions are the same size
		if (entityRefs.size() != entities.size()) return false;

		// and each ref is found
		for (Iterator<String> iRefs = entityRefs.iterator(); iRefs.hasNext();)
		{
			String entityRef = iRefs.next();
			if (!entityCollectionContainsRefString(entities, entityRef)) {
				return false;
			}
		}
		// we need the second loop incase there is duplication of enements
		for (Iterator<Entity> iEntities = entities.iterator(); iEntities.hasNext();)
		{
			String findThisEntityRef = iEntities.next().getReference();
			if(!entityRefs.contains(findThisEntityRef)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Test a collection of Entity object for the specified entity reference
	 * 
	 * @param entities
	 *        The collection (Entity) of entities
	 * @param entityRef
	 *        The string entity reference to find.
	 * @return true if found, false if not.
	 */
	public static boolean entityCollectionContainsRefString(Collection<Entity> entities, String entityRef)
	{
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();)
		{
			Entity entity = i.next();
			if (entity.getReference().equals(entityRef)) return true;
		}

		return false;
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
	public static boolean refCollectionContainsEntity(Collection<String> refs, Entity entity)
	{
		String targetRef = entity.getReference();
		for (Iterator<String> i = refs.iterator(); i.hasNext();)
		{
			String entityRef = i.next();
			if (entityRef.equals(targetRef)) return true;
		}

		return false;
	}

	/**
	 * Set the refs collection to the entity reference strings from the entities collection (and nothing more)
	 * 
	 * @param refs
	 *        The collection (String) of entity references to modify.
	 * @param entities
	 *        The collection (Entity) of entity objects to use.
	 */
	public static void setEntityRefsFromEntities(Collection<String> refs, Collection<Entity> entities)
	{
		refs.clear();
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();)
		{
			Entity entity = i.next();
			refs.add(entity.getReference());
		}
	}

	/**
	 * Fill in the two collections of Entity reference strings - those added in newEntities that were not in oldEntityRefs, and those removed, i.e. in oldEntityRefs not in newEntities.
	 */
	public static void computeAddedRemovedEntityRefsFromNewEntitiesOldRefs(Collection<String> addedEntities, Collection<String> removedEntities,
			Collection<Entity> newEntities, Collection<String> oldEntityRefs)
	{
		// added
		for (Iterator<Entity> i = newEntities.iterator(); i.hasNext();)
		{
			Entity entity = i.next();
			if (!refCollectionContainsEntity(oldEntityRefs, entity))
			{
				addedEntities.add(entity.getReference());
			}
		}

		// removed
		for (Iterator<String> i = oldEntityRefs.iterator(); i.hasNext();)
		{
			String entityRef = i.next();
			if (!entityCollectionContainsRefString(newEntities, entityRef))
			{
				removedEntities.add(entityRef);
			}
		}
	}
}
