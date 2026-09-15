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

package org.sakaiproject.videotraining.impl.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Validator;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.SiteVideoResourceOption;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;
import org.sakaiproject.videotraining.api.service.VideoTrainingUploadService;
import org.sakaiproject.videotraining.api.util.ContentResourceHelper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoTrainingUploadServiceImpl implements VideoTrainingUploadService {

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private ContentHostingService contentHostingService;

    @Setter
    private ContentResourceHelper contentResourceHelper;

    @Setter
    private SessionManager sessionManager;

    @Setter
    private VideoTrainingOAuthCredentialsService oauthCredentialsService;

    @Override
    public String uploadNativeVideo(String siteId, String ownerId, VideoVisibilityScope visibilityScope,
            byte[] content, String originalFilename, String contentType, long size) throws Exception {
        String collectionId = resolveManagedUploadCollectionId(siteId, ownerId, visibilityScope);

        String safeOriginalFilename = StringUtils.defaultIfBlank(originalFilename, "video");
        String extension = "";
        String baseName = safeOriginalFilename;
        int extensionIndex = safeOriginalFilename.lastIndexOf('.');
        if (extensionIndex > 0 && extensionIndex < safeOriginalFilename.length() - 1) {
            extension = safeOriginalFilename.substring(extensionIndex);
            baseName = safeOriginalFilename.substring(0, extensionIndex);
        }

        String safeBaseName = StringUtils.defaultIfBlank(StringUtils.trimToEmpty(baseName), "video")
                + "-"
                + UUID.randomUUID();

        ContentResourceEdit edit = contentHostingService.addResource(collectionId, safeBaseName, extension, 1);
        boolean committed = false;
        try {
            edit.setContent(content);
            edit.setContentLength(size);
            edit.setContentType(StringUtils.defaultIfBlank(contentType, "application/octet-stream"));
            edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, safeOriginalFilename);
            edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_PROPERTY, "true");
            edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_SITE_PROPERTY, siteId);
            edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_OWNER_PROPERTY, ownerId);
            edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_SCOPE_PROPERTY,
                    visibilityScope != null ? visibilityScope.name() : VideoVisibilityScope.COURSE.name());
            contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
            committed = true;
            return edit.getId();
        } finally {
            if (!committed) {
                try {
                    contentHostingService.cancelResource(edit);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public String stageTemporaryManagedUpload(InputStream content) throws Exception {
        Path baseDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "video-training");
        Files.createDirectories(baseDirectory);
        Path workingDirectory = Files.createTempDirectory(baseDirectory, "hls-");
        Path tempFile = Files.createTempFile(workingDirectory, "upload-", ".mp4");
        try (InputStream inputStream = content) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toAbsolutePath().toString();
    }

    @Override
    public void cleanupTemporaryUpload(String tempFilePath) {
        if (StringUtils.isBlank(tempFilePath)) {
            return;
        }

        try {
            Path tempFile = Paths.get(tempFilePath);
            Path workingDirectory = tempFile.getParent();
            if (workingDirectory != null && Files.exists(workingDirectory)) {
                Files.walk(workingDirectory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            } else {
                Files.deleteIfExists(tempFile);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void cleanupManagedNativeResource(String sourceReference) {
        if (StringUtils.isBlank(sourceReference)) {
            return;
        }

        try {
            ContentResource resource = contentResourceHelper.getContentResource(sourceReference);
            ResourceProperties properties = resource.getProperties();
            String managedFlag = properties != null ? properties.getProperty(VideoTrainingConstants.MANAGED_UPLOAD_PROPERTY) : null;
            if (!"true".equalsIgnoreCase(managedFlag)) {
                return;
            }

            contentHostingService.removeResource(contentResourceHelper.toContentResourceId(sourceReference));
        } catch (Exception e) {
            // best effort cleanup path
        }
    }

    @Override
    public String relocateManagedNativeResourceIfNeeded(String sourceReference, String siteId, String ownerId,
            VideoVisibilityScope visibilityScope) {
        if (StringUtils.isBlank(sourceReference) || visibilityScope == null) {
            return sourceReference;
        }

        try {
            ContentResource resource = contentResourceHelper.getContentResource(sourceReference);
            ResourceProperties properties = resource.getProperties();
            String managedFlag = properties != null ? properties.getProperty(VideoTrainingConstants.MANAGED_UPLOAD_PROPERTY) : null;
            if (!"true".equalsIgnoreCase(managedFlag)) {
                return sourceReference;
            }

            if (visibilityScope == VideoVisibilityScope.GLOBAL) {
                return sourceReference;
            }

            String targetCollectionId = resolveManagedUploadCollectionId(siteId, ownerId, visibilityScope);
            String currentResourceId = contentResourceHelper.toContentResourceId(sourceReference);
            if (StringUtils.startsWith(currentResourceId, targetCollectionId)) {
                return sourceReference;
            }

            String originalFilename = properties != null
                    ? properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME)
                    : null;
            String resolvedFilename = StringUtils.defaultIfBlank(originalFilename, resource.getId());
            String extension = "";
            String baseName = resolvedFilename;
            int extensionIndex = resolvedFilename.lastIndexOf('.');
            if (extensionIndex > 0 && extensionIndex < resolvedFilename.length() - 1) {
                extension = resolvedFilename.substring(extensionIndex);
                baseName = resolvedFilename.substring(0, extensionIndex);
            }

            String safeBaseName = StringUtils.defaultIfBlank(StringUtils.trimToEmpty(baseName), "video")
                    + "-"
                    + UUID.randomUUID();

            ContentResourceEdit edit = contentHostingService.addResource(targetCollectionId, safeBaseName, extension, 1);
            boolean committed = false;
            try {
                edit.setContent(resource.getContent());
                edit.setContentLength(resource.getContentLength());
                edit.setContentType(StringUtils.defaultIfBlank(resource.getContentType(), "application/octet-stream"));
                edit.setAvailability(resource.isHidden(), resource.getReleaseDate(), resource.getRetractDate());
                edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, resolvedFilename);
                edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_PROPERTY, "true");
                edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_SITE_PROPERTY, siteId);
                edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_OWNER_PROPERTY, ownerId);
                edit.getPropertiesEdit().addProperty(VideoTrainingConstants.MANAGED_UPLOAD_SCOPE_PROPERTY, visibilityScope.name());
                contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
                committed = true;
                return edit.getId();
            } finally {
                if (!committed) {
                    try {
                        contentHostingService.cancelResource(edit);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e) {
            return sourceReference;
        }
    }

    @Override
    public Long resolveNativeResourceSizeBytes(String sourceReference) {
        if (StringUtils.isBlank(sourceReference)) {
            return null;
        }

        try {
            ContentResource resource = contentResourceHelper.getContentResource(sourceReference);
            return resource.getContentLength();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String resolveNativePlaybackUrl(VideoTrainingVideo video) {
        if (video == null || video.getProviderType() == VideoProviderType.EXTERNAL) {
            return "";
        }

        return resolveContentReferenceFromSourceId(video.getSourceReference());
    }

    @Override
    public String resolveNativeContentType(VideoTrainingVideo video) {
        if (video == null || video.getProviderType() == VideoProviderType.EXTERNAL) {
            return "video/mp4";
        }

        try {
            ContentResource resource = contentResourceHelper.getContentResource(video.getSourceReference());
            return StringUtils.defaultIfBlank(resource.getContentType(), "video/mp4");
        } catch (Exception e) {
            return "video/mp4";
        }
    }

    @Override
    public MetadataFetchResult resolveResourceMetadata(String resourceReference) {
        String normalizedReference = StringUtils.trimToNull(resourceReference);
        if (StringUtils.isBlank(normalizedReference)) {
            return new MetadataFetchResult("", "");
        }

        try {
            ContentResource resource = contentResourceHelper.getContentResource(normalizedReference);
            if (resource == null) {
                return new MetadataFetchResult("", "");
            }

            ResourceProperties properties = resource.getProperties();
            String title = StringUtils.defaultIfBlank(properties != null ? properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME) : null, resource.getId());
            return new MetadataFetchResult(title, "");
        } catch (Exception ex) {
            return new MetadataFetchResult("", "");
        }
    }

    @Override
    public List<SiteVideoResourceOption> getExistingSiteVideoResources(String siteId) {
        List<SiteVideoResourceOption> options = new ArrayList<>();
        String siteCollection = contentHostingService.getSiteCollection(siteId);
        List<ContentEntity> entities = contentHostingService.getAllEntities(siteCollection);

        for (ContentEntity entity : entities) {
            if (!(entity instanceof ContentResource resource)) {
                continue;
            }

            String contentType = StringUtils.defaultString(resource.getContentType()).toLowerCase(Locale.ROOT);
            if (!contentType.startsWith("video/")) {
                continue;
            }

            ResourceProperties properties = resource.getProperties();
            String displayName = properties != null
                    ? StringUtils.defaultIfBlank(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME), resource.getId())
                    : resource.getId();
            options.add(new SiteVideoResourceOption(resource.getId(), displayName, contentType));
        }

        options.sort(Comparator.comparing(SiteVideoResourceOption::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return options;
    }

    @Override
    public boolean isExistingSiteVideoResourceReference(String siteId, String sourceReference) {
        if (StringUtils.isBlank(siteId) || StringUtils.isBlank(sourceReference)) {
            return false;
        }

        for (SiteVideoResourceOption option : getExistingSiteVideoResources(siteId)) {
            if (StringUtils.equals(option.getReference(), sourceReference)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValidNativeUpload(String filename, String contentType, boolean emptyOrNull) {
        if (emptyOrNull) {
            return false;
        }

        String normalizedContentType = StringUtils.trimToEmpty(contentType).toLowerCase(Locale.ROOT);
        if (normalizedContentType.startsWith("video/")) {
            return true;
        }

        String extension = extractLowercaseExtension(filename);
        return VideoTrainingConstants.ALLOWED_NATIVE_VIDEO_EXTENSIONS.contains(extension);
    }

    @Override
    public long getConfiguredMaxNativeUploadBytes() {
        String configuredValue = StringUtils.trimToEmpty(serverConfigurationService.getString(
                VideoTrainingConstants.MAX_NATIVE_UPLOAD_SIZE_PROPERTY,
                String.valueOf(VideoTrainingConstants.DEFAULT_MAX_NATIVE_UPLOAD_MB)));
        try {
            long parsedMegabytes = Long.parseLong(configuredValue);
            long safeMegabytes = parsedMegabytes > 0 ? parsedMegabytes : VideoTrainingConstants.DEFAULT_MAX_NATIVE_UPLOAD_MB;
            return safeMegabytes * VideoTrainingConstants.BYTES_PER_MB;
        } catch (NumberFormatException ex) {
            return VideoTrainingConstants.DEFAULT_MAX_NATIVE_UPLOAD_MB * VideoTrainingConstants.BYTES_PER_MB;
        }
    }

    @Override
    public boolean isManagedUploadProvider(VideoProviderType providerType) {
        return providerType == VideoProviderType.HLS_UPLOAD
                || providerType == VideoProviderType.YOUTUBE_UPLOAD;
    }

    @Override
    public boolean isExternalUploadProvider(VideoProviderType providerType) {
        return providerType == VideoProviderType.YOUTUBE_UPLOAD;
    }

    @Override
    public boolean isHlsUploadEnabled() {
        return serverConfigurationService.getBoolean(VideoTrainingConstants.HLS_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_HLS_ENABLED);
    }

    @Override
    public String defaultUploadProviderType(VideoTrainingVideo video) {
        if (video != null && isManagedUploadProvider(video.getProviderType())) {
            return video.getProviderType().name();
        }

        if (isHlsUploadEnabled()) {
            return VideoProviderType.HLS_UPLOAD.name();
        }
        if (oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD)) {
            return VideoProviderType.YOUTUBE_UPLOAD.name();
        }
        return VideoProviderType.NATIVE.name();
    }

    @Override
    public String resolveManagedUploadCollectionId(String siteId, String ownerId, VideoVisibilityScope visibilityScope) throws Exception {
        String collectionId;
        VideoVisibilityScope effectiveScope = visibilityScope != null ? visibilityScope : VideoVisibilityScope.COURSE;

        if (effectiveScope == VideoVisibilityScope.GLOBAL) {
            String globalRoot = StringUtils.trimToEmpty(serverConfigurationService.getString(VideoTrainingConstants.GLOBAL_ROOT_PROPERTY, VideoTrainingConstants.DEFAULT_GLOBAL_ROOT));
            if (!globalRoot.startsWith("/")) {
                globalRoot = "/" + globalRoot;
            }
            if (!globalRoot.endsWith("/")) {
                globalRoot = globalRoot + "/";
            }

            String normalizedOwner = Validator.escapeResourceName(StringUtils.defaultIfBlank(ownerId, currentUserId()));
            if (StringUtils.isBlank(normalizedOwner)) {
                normalizedOwner = "owner";
            }
            collectionId = globalRoot + normalizedOwner + "/";
            ensureManagedCollectionPath(globalRoot, StringUtils.trimToEmpty(serverConfigurationService.getString(VideoTrainingConstants.GLOBAL_ROOT_BASE_FOLDER_PROPERTY, VideoTrainingConstants.DEFAULT_GLOBAL_ROOT_BASE_FOLDER)), true);
            ensureManagedCollectionPath(collectionId, normalizedOwner, true);
            return collectionId;
        }

        String siteCollectionId = contentHostingService.getSiteCollection(siteId);
        String baseFolderProperty = VideoTrainingConstants.BASE_FOLDER_PROPERTY;
        String baseFolderDefault = VideoTrainingConstants.DEFAULT_BASE_FOLDER;
        String baseFolder = StringUtils.trimToEmpty(serverConfigurationService.getString(baseFolderProperty, baseFolderDefault));

        String normalizedBaseFolder = Validator.escapeResourceName(baseFolder);
        if (StringUtils.isBlank(normalizedBaseFolder)) {
            normalizedBaseFolder = baseFolderDefault;
        }

        collectionId = siteCollectionId + normalizedBaseFolder + "/";
        ensureManagedCollectionPath(collectionId, normalizedBaseFolder, false);
        return collectionId;
    }

    @Override
    public void ensureManagedCollectionPath(String collectionId, String displayName, boolean forceVisible) throws Exception {
        boolean hiddenWithAccessibleContent = !forceVisible && serverConfigurationService.getBoolean(VideoTrainingConstants.FOLDER_HIDDEN_WITH_ACCESS_PROPERTY, VideoTrainingConstants.DEFAULT_FOLDER_HIDDEN_WITH_ACCESS);

        try {
            contentHostingService.checkCollection(collectionId);
        } catch (IdUnusedException idUnusedException) {
            ContentCollectionEdit edit = contentHostingService.addCollection(collectionId);
            if (StringUtils.isNotBlank(displayName)) {
                edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
            }
            edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT,
                    String.valueOf(hiddenWithAccessibleContent));
            contentHostingService.commitCollection(edit);
        }
    }

    private String resolveContentReferenceFromSourceId(String sourceId) {
        if (StringUtils.isBlank(sourceId)) {
            return "";
        }

        try {
            return contentResourceHelper.getContentUrl(sourceId);
        } catch (Exception e) {
            return "";
        }
    }

    private String extractLowercaseExtension(String filename) {
        String normalizedFilename = StringUtils.trimToEmpty(filename);
        int extensionIndex = normalizedFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex >= normalizedFilename.length() - 1) {
            return "";
        }
        return normalizedFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String currentUserId() {
        return sessionManager != null ? sessionManager.getCurrentSessionUserId() : null;
    }
}
