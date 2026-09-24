/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

public interface TagCollectionRepository extends SpringCrudRepository<TagCollection, String> {
    /** Insert a collection with an assigned ID; reject duplicates instead of merging existing data. */
    TagCollection create(TagCollection collection);
    List<TagCollection> findAllOrdered(int offset, int limit);
    Optional<TagCollection> findByName(String name);
    Optional<TagCollection> findByExternalSourceName(String externalSourceName);
}
