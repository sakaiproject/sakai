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

import java.io.IOException;
import java.nio.file.Path;

import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;

public interface ExternalVideoProviderStrategy {

    VideoProviderType getProviderType();

    MetadataFetchResult fetchMetadata(String url);

    String uploadVideo(VideoTrainingVideo video, Path inputFile) throws IOException;

    VideoPublicationStatus fetchProviderPublicationStatus(String sourceReference, VideoVisibilityScope scope);

    boolean syncPrivacy(String sourceReference, VideoPublicationStatus publicationStatus, VideoVisibilityScope visibilityScope);

    String extractVideoId(String url);
}
