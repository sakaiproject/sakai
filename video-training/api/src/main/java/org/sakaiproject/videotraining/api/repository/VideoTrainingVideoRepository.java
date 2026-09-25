/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.api.repository;

import java.time.Instant;
import java.util.List;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;

public interface VideoTrainingVideoRepository extends SpringCrudRepository<VideoTrainingVideo, String> {

    List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId);

    List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId, String searchText, int offset, int limit);

    List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDesc(String siteId, String searchText, List<String> categoryIds, int offset, int limit);

    List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId);

    List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId, String searchText, int offset, int limit);

    List<VideoTrainingVideo> findBySiteIdAndOwnerIdOrderByModifiedOnDesc(String siteId, String ownerId, String searchText, List<String> categoryIds, int offset, int limit);

    List<VideoTrainingVideo> findBySiteIdAndOwnerIdSorted(String siteId, String ownerId, String searchText, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findBySiteIdAndOwnerIdSorted(String siteId, String ownerId, String searchText, List<String> categoryIds, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findBySiteIdSorted(String siteId, String searchText, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findBySiteIdSorted(String siteId, String searchText, List<String> categoryIds, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findBySiteIdOrderByModifiedOnDescCursor(String siteId, String searchText, Instant cursorModifiedOn, String cursorVideoId, int limit);

    long countBySiteId(String siteId, String searchText);

    long countBySiteId(String siteId, String searchText, List<String> categoryIds);

    long countBySiteIdAndOwnerId(String siteId, String ownerId, String searchText);

    long countBySiteIdAndOwnerId(String siteId, String ownerId, String searchText, List<String> categoryIds);

    long countByGlobal(String searchText);

    List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now);

    List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now, String searchText, int offset, int limit);

    List<VideoTrainingVideo> findVisibleBySiteIdAt(String siteId, Instant now, String searchText, List<String> categoryIds, int offset, int limit);

    List<VideoTrainingVideo> findVisibleBySiteIdAtSorted(String siteId, Instant now, String searchText, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findVisibleBySiteIdAtSorted(String siteId, Instant now, String searchText, List<String> categoryIds, int offset, int limit, String sortField, boolean ascending);

    List<VideoTrainingVideo> findVisibleBySiteIdAtCursor(String siteId, Instant now, String searchText, Instant cursorModifiedOn, String cursorVideoId, int limit);

    long countVisibleBySiteIdAt(String siteId, Instant now, String searchText);

    long countVisibleBySiteIdAt(String siteId, Instant now, String searchText, List<String> categoryIds);

    List<VideoTrainingVideo> findVisibleByGlobal(String searchText, int offset, int size);

    List<VideoTrainingVideo> findBySiteIdAndCategoryIds(String siteId, List<String> categoryIds);

    List<VideoTrainingVideo> findByCategoryIds(List<String> categoryIds);

    long adminCountAllGlobal(String searchText);

    List<VideoTrainingVideo> adminFindAllGlobal(String searchText, int offset, int size);

    long countAll(String searchText);

    List<VideoTrainingVideo> findAll(String searchText, int offset, int size);

    List<VideoTrainingVideo> findGlobalPublishedCursor(String searchText, Instant cursorModifiedOn, String cursorVideoId, int limit);

    List<VideoTrainingVideo> findGlobalPublishedSorted(String searchText, int offset, int limit, String sortField, boolean ascending);

    long sumNativeStorageBytesBySiteId(String siteId);
}
