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

import java.util.List;
import java.util.Optional;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.videotraining.api.model.VideoTrainingUserVideoPreference;

public interface VideoTrainingUserVideoPreferenceRepository extends SpringCrudRepository<VideoTrainingUserVideoPreference, String> {

    Optional<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndVideoId(String siteId, String userId, String videoId);

    List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndVideoIds(String siteId, String userId, List<String> videoIds);

    List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndFavoriteTrueOrderByModifiedOnDesc(String siteId, String userId);

    List<VideoTrainingUserVideoPreference> findBySiteIdAndUserIdAndWatchLaterTrueOrderByModifiedOnDesc(String siteId, String userId);

    void deleteByVideoId(String videoId);
}
