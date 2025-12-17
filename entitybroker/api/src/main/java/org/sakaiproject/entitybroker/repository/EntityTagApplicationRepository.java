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

import org.sakaiproject.entitybroker.model.EntityTagApplication;
import org.sakaiproject.springframework.data.SpringCrudRepository;

/**
 * Repository interface for EntityTagApplication operations
 */
public interface EntityTagApplicationRepository extends SpringCrudRepository<EntityTagApplication, Long> {

    /**
     * Find all tag applications for a given entity reference
     * @param entityRef the entity reference
     * @return list of entity tag applications
     */
    List<EntityTagApplication> findByEntityRef(String entityRef);

    /**
     * Find tag applications for a given entity reference and specific tags
     * @param entityRef the entity reference
     * @param tags the array of tags to filter by
     * @return list of entity tag applications
     */
    List<EntityTagApplication> findByEntityRefAndTags(String entityRef, String[] tags);

    /**
     * Delete all tag applications for a given entity reference
     * @param entityRef the entity reference
     * @return the number of tag applications deleted
     */
    int deleteByEntityRef(String entityRef);

    /**
     * Delete tag applications for a given entity reference and specific tags
     * @param entityRef the entity reference
     * @param tags the array of tags to delete
     * @return the number of tag applications deleted
     */
    int deleteByEntityRefAndTags(String entityRef, String[] tags);

    /**
     * Find tag applications matching tags and prefixes, ordered by entityRef
     * @param tags array of tags to match
     * @param prefixes array of entity prefixes to match (null/empty for all)
     * @param limit maximum number of results (0 for no limit)
     * @param start starting offset (0 for beginning)
     * @return list of entity tag applications
     */
    List<EntityTagApplication> findByTagsAndPrefixes(String[] tags, String[] prefixes, int limit, int start);
}
