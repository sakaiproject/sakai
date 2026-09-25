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

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.SiteVideoResourceOption;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;

/**
 * Encapsulates all Content Hosting interactions needed to upload, stage, relocate and clean up
 * the native/managed video files backing a {@link VideoTrainingVideo}.
 */
public interface VideoTrainingUploadService {

    String uploadNativeVideo(String siteId, String ownerId, VideoVisibilityScope scope, byte[] content,
            String originalFilename, String contentType, long size) throws Exception;

    String stageTemporaryManagedUpload(InputStream content) throws Exception;

    void cleanupTemporaryUpload(String tempFilePath);

    void cleanupManagedNativeResource(String sourceReference);

    String relocateManagedNativeResourceIfNeeded(String sourceReference, String siteId, String ownerId,
            VideoVisibilityScope scope);

    Long resolveNativeResourceSizeBytes(String sourceReference);

    String resolveNativePlaybackUrl(VideoTrainingVideo video);

    String resolveNativeContentType(VideoTrainingVideo video);

    MetadataFetchResult resolveResourceMetadata(String resourceReference);

    List<SiteVideoResourceOption> getExistingSiteVideoResources(String siteId);

    boolean isExistingSiteVideoResourceReference(String siteId, String sourceReference);

    boolean isValidNativeUpload(String filename, String contentType, boolean emptyOrNull);

    long getConfiguredMaxNativeUploadBytes();

    boolean isManagedUploadProvider(VideoProviderType providerType);

    boolean isExternalUploadProvider(VideoProviderType providerType);

    boolean isHlsUploadEnabled();

    String defaultUploadProviderType(VideoTrainingVideo video);

    String resolveManagedUploadCollectionId(String siteId, String ownerId, VideoVisibilityScope visibilityScope) throws Exception;

    void ensureManagedCollectionPath(String collectionId, String displayName, boolean forceVisible) throws Exception;
}
