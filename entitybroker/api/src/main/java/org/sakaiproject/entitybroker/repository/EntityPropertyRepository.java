/**
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
 */

package org.sakaiproject.entitybroker.repository;

import java.util.List;

import org.sakaiproject.entitybroker.model.EntityProperty;
import org.sakaiproject.springframework.data.SpringCrudRepository;

/**
 * Repository interface for EntityProperty operations
 */
public interface EntityPropertyRepository extends SpringCrudRepository<EntityProperty, Long> {

    /**
     * Find distinct entity references matching search criteria with dynamic AND/OR logic
     * @param properties the persistent object properties
     * @param values the values to match against the properties
     * @param comparisons the type of comparisons to make between property and value
     * @param relations the relation to the previous search param (must be "and" or "or")
     * @return a list of unique entity reference strings
     */
    List<String> findDistinctEntityRefs(List<String> properties, List<String> values,
                                        List<Integer> comparisons, List<String> relations);

    /**
     * Find all properties for a given entity reference
     * @param entityRef the entity reference
     * @return list of entity properties
     */
    List<EntityProperty> findByEntityRef(String entityRef);

    /**
     * Find properties for a given entity reference and property name
     * @param entityRef the entity reference
     * @param propertyName the property name
     * @return list of entity properties
     */
    List<EntityProperty> findByEntityRefAndPropertyName(String entityRef, String propertyName);

    /**
     * Delete all properties for a given entity reference
     * @param entityRef the entity reference
     * @return the number of properties deleted
     */
    int deleteByEntityRef(String entityRef);

    /**
     * Delete properties for a given entity reference and property name
     * @param entityRef the entity reference
     * @param propertyName the property name
     * @return the number of properties deleted
     */
    int deleteByEntityRefAndPropertyName(String entityRef, String propertyName);
}
