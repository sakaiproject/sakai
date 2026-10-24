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
import java.util.List;
import java.util.Optional;

import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;

public interface VideoTrainingOAuthCredentialsService {

    List<VideoTrainingOAuthCredentials> getAllCredentials();

    Optional<VideoTrainingOAuthCredentials> getCredentials(VideoProviderType providerType);

    VideoTrainingOAuthCredentials saveCredentials(VideoProviderType providerType, String clientId, String apiKey, String clientSecret, String refreshToken);

    boolean isConfigured(VideoProviderType providerType);

    boolean isAuthorizationReady(VideoProviderType providerType);

    boolean isYoutubeMetadataConfigured();

    VideoTrainingOAuthCredentials loadCredentialsForForm(VideoProviderType providerType);

    String buildYoutubeAuthorizationUrl(String redirectUri, String state);

    String exchangeYoutubeRefreshToken(String redirectUri, String authorizationCode) throws IOException;
}
