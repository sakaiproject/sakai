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

/**
 * The interface for the tag Collections sub-service.
 */
public interface TagCollections {

    public String createTagCollection(TagCollection tagCollection);

    public void updateTagCollection(TagCollection tagCollection);

    public void deleteTagCollection(String tagCollectionId);

    public List<TagCollection> getAll();

    public Optional<TagCollection> getForId(String tagCollectionId);

    public Optional<TagCollection> getForName(String tagCollectionId);

    public Optional<TagCollection> getForExternalSourceName(String externalSourceName);

    public List<TagCollection> getTagCollectionsPaginated(int pageNum, int pageSize);

    public int getTotalTagCollections();

}
