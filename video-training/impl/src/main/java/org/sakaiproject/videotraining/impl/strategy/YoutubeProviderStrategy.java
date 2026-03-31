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

package org.sakaiproject.videotraining.impl.strategy;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

public class YoutubeProviderStrategy implements ExternalVideoProviderStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private ServerConfigurationService serverConfigurationService;
    private VideoTrainingOAuthCredentialsService credentialsService;

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setCredentialsService(VideoTrainingOAuthCredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Override
    public VideoProviderType getProviderType() {
        return VideoProviderType.YOUTUBE_UPLOAD;
    }

    @Override
    public MetadataFetchResult fetchMetadata(String url) {
        String videoId = extractVideoId(url);
        if (StringUtils.isBlank(videoId)) {
            return new MetadataFetchResult("", "");
        }

        String apiKey = resolveYoutubeApiKey();
        if (StringUtils.isBlank(apiKey)) {
            return new MetadataFetchResult("", "");
        }

        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="
                    + URLEncoder.encode(videoId, StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode items = root.path("items");
                if (items.isArray() && items.size() > 0) {
                    JsonNode snippet = items.get(0).path("snippet");
                    String title = snippet.path("title").asText("");
                    String description = snippet.path("description").asText("");
                    return new MetadataFetchResult(title, description);
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new MetadataFetchResult("", "");
    }

    @Override
    public String uploadVideo(VideoTrainingVideo video, Path inputFile) throws IOException {
        if (video == null) {
            throw new IOException("Video is required for external upload");
        }
        if (inputFile == null) {
            throw new IOException("Temporary upload file is required for external upload");
        }

        VideoTrainingOAuthCredentials credentials = resolveYoutubeCredentials();
        if (credentials == null || StringUtils.isBlank(credentials.getClientId())
                || StringUtils.isBlank(credentials.getClientSecret())
                || StringUtils.isBlank(credentials.getRefreshToken())) {
            throw new IOException("OAuth credentials are not configured for " + getProviderType());
        }

        String contentType = detectContentType(inputFile);
        YouTube youtube = buildYouTubeClient(credentials);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(StringUtils.defaultIfBlank(video.getTitle(), VideoTrainingConstants.DEFAULT_APPLICATION_NAME));
        snippet.setDescription(StringUtils.defaultString(video.getDescription()));
        snippet.setCategoryId(serverConfigurationService.getString(
                VideoTrainingConstants.PROVIDER_YOUTUBE_CATEGORY_ID_PROPERTY,
                VideoTrainingConstants.DEFAULT_PROVIDER_YOUTUBE_CATEGORY_ID));

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(VideoTrainingConstants.YOUTUBE_PRIVACY_PRIVATE);
        status.setSelfDeclaredMadeForKids(false);

        Video body = new Video();
        body.setSnippet(snippet);
        body.setStatus(status);

        FileContent mediaContent = new FileContent(contentType, inputFile.toFile());
        com.google.api.services.youtube.YouTube.Videos.Insert insert = youtube.videos().insert(Arrays.asList("snippet", "status"), body, mediaContent);
        insert.getMediaHttpUploader().setDirectUploadEnabled(false);
        Video uploaded = insert.execute();
        if (uploaded == null || StringUtils.isBlank(uploaded.getId())) {
            throw new IOException("YouTube did not return a video id");
        }

        return "https://www.youtube.com/watch?v=" + uploaded.getId();
    }

    @Override
    public VideoPublicationStatus fetchProviderPublicationStatus(String sourceReference, VideoVisibilityScope scope) {
        String videoId = extractVideoId(sourceReference);
        if (StringUtils.isBlank(videoId)) {
            return null;
        }

        VideoTrainingOAuthCredentials credentials = resolveYoutubeCredentials();
        if (credentials == null || StringUtils.isBlank(credentials.getClientId())
                || StringUtils.isBlank(credentials.getClientSecret())
                || StringUtils.isBlank(credentials.getRefreshToken())) {
            return null;
        }

        try {
            YouTube youtube = buildYouTubeClient(credentials);
            VideoListResponse response = youtube.videos().list(Arrays.asList("status")).setId(Arrays.asList(videoId)).execute();
            if (response.getItems() == null || response.getItems().isEmpty() || response.getItems().get(0).getStatus() == null) {
                return null;
            }

            String privacyStatus = StringUtils.defaultString(response.getItems().get(0).getStatus().getPrivacyStatus());
            return mapPrivacyStatusToPublicationStatus(privacyStatus, scope);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean syncPrivacy(String sourceReference, VideoPublicationStatus publicationStatus, VideoVisibilityScope visibilityScope) {
        String videoId = extractVideoId(sourceReference);
        if (StringUtils.isBlank(videoId)) {
            return false;
        }

        String desiredPrivacy = resolveDesiredPrivacy(publicationStatus, visibilityScope);
        if (StringUtils.isBlank(desiredPrivacy)) {
            return false;
        }

        VideoTrainingOAuthCredentials credentials = resolveYoutubeCredentials();
        if (credentials == null || StringUtils.isBlank(credentials.getClientId())
                || StringUtils.isBlank(credentials.getClientSecret())
                || StringUtils.isBlank(credentials.getRefreshToken())) {
            return false;
        }

        try {
            YouTube youtube = buildYouTubeClient(credentials);
            VideoListResponse response = youtube.videos().list(Arrays.asList("status")).setId(Arrays.asList(videoId)).execute();
            if (response.getItems() == null || response.getItems().isEmpty() || response.getItems().get(0).getStatus() == null) {
                return false;
            }

            String currentPrivacy = StringUtils.defaultString(response.getItems().get(0).getStatus().getPrivacyStatus());
            if (currentPrivacy.equalsIgnoreCase(desiredPrivacy)) {
                return false;
            }

            Video update = new Video();
            update.setId(videoId);
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus(desiredPrivacy);
            status.setEmbeddable(true);
            status.setSelfDeclaredMadeForKids(false);
            update.setStatus(status);
            youtube.videos().update(Arrays.asList("status"), update).execute();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String extractVideoId(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        String trimmed = url.trim();
        Matcher youTubeMatcher = VideoTrainingConstants.YOUTUBE_PATTERN.matcher(trimmed);
        if (youTubeMatcher.find()) {
            return youTubeMatcher.group(1);
        }
        Matcher iframeMatcher = VideoTrainingConstants.IFRAME_PATTERN.matcher(trimmed);
        if (iframeMatcher.find()) {
            return extractVideoId(iframeMatcher.group(1));
        }
        return null;
    }

    private YouTube buildYouTubeClient(VideoTrainingOAuthCredentials credentials) throws IOException {
        GoogleCredential googleCredential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(credentials.getClientId(), credentials.getClientSecret())
                .build();
        googleCredential.setRefreshToken(credentials.getRefreshToken());
        if (!googleCredential.refreshToken()) {
            throw new IOException("Unable to refresh Google access token");
        }

        HttpRequestInitializer initializer = request -> {
            googleCredential.initialize(request);
            request.setConnectTimeout(120000);
            request.setReadTimeout(120000);
            request.setNumberOfRetries(3);
        };

        return new YouTube.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), initializer)
                .setApplicationName(VideoTrainingConstants.DEFAULT_APPLICATION_NAME)
                .build();
    }

    private VideoTrainingOAuthCredentials resolveYoutubeCredentials() {
        try {
            if (credentialsService == null) {
                return null;
            }
            return credentialsService.getCredentials(getProviderType()).orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveYoutubeApiKey() {
        try {
            if (serverConfigurationService != null) {
                String configuredApiKey = serverConfigurationService.getString(
                        VideoTrainingConstants.PROVIDER_YOUTUBE_API_KEY_PROPERTY,
                        VideoTrainingConstants.DEFAULT_PROVIDER_YOUTUBE_API_KEY);
                if (StringUtils.isNotBlank(configuredApiKey)) {
                    return trimToEmpty(configuredApiKey);
                }
            }

            if (credentialsService == null) {
                return "";
            }

            return credentialsService.getCredentials(getProviderType())
                    .map(VideoTrainingOAuthCredentials::getApiKey)
                    .map(this::trimToEmpty)
                    .orElse("");
        } catch (Exception ex) {
            return "";
        }
    }

    String resolveDesiredPrivacy(VideoPublicationStatus publicationStatus, VideoVisibilityScope visibilityScope) {
        if (publicationStatus == VideoPublicationStatus.PUBLISHED) {
            return "unlisted";
        }

        return VideoTrainingConstants.YOUTUBE_PRIVACY_PRIVATE;
    }

    private VideoPublicationStatus mapPrivacyStatusToPublicationStatus(String privacyStatus, VideoVisibilityScope scope) {
        if (StringUtils.isBlank(privacyStatus)) {
            return null;
        }

        if ("public".equalsIgnoreCase(privacyStatus) || "unlisted".equalsIgnoreCase(privacyStatus)) {
            return VideoPublicationStatus.PUBLISHED;
        }

        if ("private".equalsIgnoreCase(privacyStatus)) {
            return scope == VideoVisibilityScope.GLOBAL ? VideoPublicationStatus.WITHDRAWN : VideoPublicationStatus.DRAFT;
        }

        return null;
    }

    private String detectContentType(Path inputFile) {
        try {
            String contentType = Files.probeContentType(inputFile);
            return StringUtils.defaultIfBlank(contentType, VideoTrainingConstants.DEFAULT_MP4_CONTENT_TYPE);
        } catch (IOException e) {
            return VideoTrainingConstants.DEFAULT_MP4_CONTENT_TYPE;
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
