/**********************************************************************************
 *
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.api;

import java.util.List;
import java.util.Optional;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TagRepository extends SpringCrudRepository<Tag, String> {
    Tag create(Tag tag);
    List<Tag> findByCollection(String collectionId, int offset, int limit);
    List<Tag> findByLabel(String label, String collectionId);
    List<Tag> findByPartialLabel(String label);
    List<Tag> findByPrefix(String label, int offset, int limit);
    List<Tag> findAllOrdered();
    Optional<Tag> findByExternalId(String externalId, String collectionId);
    long countByCollection(String collectionId);
    long countByPrefix(String label);
    List<Tag> findOlderThan(String collectionId, long timestamp);
    List<Tag> findAllByExternalId(String externalId, String collectionId);
}
