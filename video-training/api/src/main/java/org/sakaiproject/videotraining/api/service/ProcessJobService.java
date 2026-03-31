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

package org.sakaiproject.videotraining.api.service;

import java.util.List;

import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJob;

public interface ProcessJobService {

    VideoTrainingProcessJob save(VideoTrainingProcessJob processJob);

    void delete(VideoTrainingProcessJob processJob);

    List<VideoTrainingProcessJob> findByVideoIdOrderByModifiedOnDesc(String videoId);

    List<VideoTrainingProcessJob> findBySubmitterUserIdOrderByModifiedOnDesc(String submitterUserId);

    void queueManagedUploadJob(String videoId, String ownerId, String tempFilePath, VideoProviderType providerType);

    void cleanupProcessJobs(String videoId);

    boolean isProcessingManagedUpload(String videoId, VideoProviderType providerType);
}
