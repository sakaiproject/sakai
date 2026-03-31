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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_ANALYTICS;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_APPROVE_PUBLISH;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_CATEGORIES_MANAGE;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_GLOBAL;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_MANAGE;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_MANAGE_ALL;
import static org.sakaiproject.videotraining.api.VideoTrainingConstants.PERMISSION_VIEW;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsEvent;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsSummary;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategoryDeleteImpact;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategoryOrderUpdate;
import org.sakaiproject.videotraining.api.model.VideoTrainingCourseGroup;
import org.sakaiproject.videotraining.api.model.VideoTrainingLessonLink;
import org.sakaiproject.videotraining.api.model.VideoTrainingUserVideoPreference;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.repository.VideoTrainingAnalyticsEventRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingCategoryRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingLessonLinkRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingUserVideoPreferenceRepository;
import org.sakaiproject.videotraining.api.repository.VideoTrainingVideoRepository;
import org.sakaiproject.videotraining.api.service.ExternalProviderRegistry;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.videotraining.api.util.ContentResourceHelper;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class VideoTrainingServiceImpl implements VideoTrainingService {

    private static final long LIST_CACHE_TTL_MILLIS = 30_000L;

    @Setter private VideoTrainingVideoRepository videoRepository;
    @Setter private VideoTrainingAnalyticsEventRepository analyticsEventRepository;
    @Setter private VideoTrainingCategoryRepository categoryRepository;
    @Setter private VideoTrainingLessonLinkRepository lessonLinkRepository;
    @Setter private VideoTrainingUserVideoPreferenceRepository userVideoPreferenceRepository;
    @Setter private ContentHostingService contentHostingService;
    @Setter private ContentResourceHelper contentResourceHelper;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private FunctionManager functionManager;
    @Setter private SecurityService securityService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private MemoryService memoryService;
    @Setter private ExternalProviderRegistry externalProviderRegistry;

    private final ConcurrentMap<ListCacheKey, CacheEntry<Long>> countCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<ListCacheKey, CacheEntry<List<String>>> firstPageCache = new ConcurrentHashMap<>();
    private Cache<String, Long> distributedCountCache;
    private Cache<String, List<String>> distributedFirstPageCache;

    public void init() {
        functionManager.registerFunction(PERMISSION_VIEW, true);
        functionManager.registerFunction(PERMISSION_MANAGE, true);
        functionManager.registerFunction(PERMISSION_MANAGE_ALL, true);
        functionManager.registerFunction(PERMISSION_ANALYTICS, true);
        functionManager.registerFunction(PERMISSION_CATEGORIES_MANAGE, true);
        functionManager.registerFunction(PERMISSION_APPROVE_PUBLISH, true);
        functionManager.registerFunction(PERMISSION_GLOBAL, true);
        initializeDistributedCaches();
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveThumbnailUrl(VideoTrainingVideo video) {
        if (video == null) {
            return "";
        }

        if (video.getProviderType() == VideoProviderType.NATIVE) {
            String src = StringUtils.trimToEmpty(video.getSourceReference());
            if (StringUtils.isNotBlank(src) && contentResourceHelper != null) {
                String cr = contentResourceHelper.getContentUrl(src);
                if (StringUtils.isNotBlank(cr)) {
                    if (cr.contains("master.m3u8")) {
                        return cr.replace("master.m3u8", "thumbnail.jpg");
                    }
                    return cr;
                }
            }

            String siteId = video.getSiteId();
            if (StringUtils.isNotBlank(siteId) && StringUtils.isNotBlank(video.getId())) {
                return "/access/content/group/" + siteId + "/" + resolveBaseFolder() + "/" + video.getId() + "/thumbnail.jpg";
            }

            return "";
        }

        if (video.getProviderType() == VideoProviderType.EXTERNAL) {
            String sourceRef = StringUtils.trimToEmpty(video.getSourceReference());
            String youtube = extractYouTubeVideoId(sourceRef);
            if (StringUtils.isNotBlank(youtube)) {
                return "https://img.youtube.com/vi/" + youtube + "/hqdefault.jpg";
            }
            return "";
        }

        if (video.getProviderType() == VideoProviderType.YOUTUBE_UPLOAD) {
            String sourceRef = StringUtils.trimToEmpty(video.getSourceReference());
            String youtube = extractYouTubeVideoId(sourceRef);
            if (StringUtils.isNotBlank(youtube)) {
                return "https://img.youtube.com/vi/" + youtube + "/hqdefault.jpg";
            }
            return "";
        }

        if (video.getProviderType() == VideoProviderType.HLS_UPLOAD) {
            String src = StringUtils.trimToEmpty(video.getSourceReference());
            if (StringUtils.isNotBlank(src) && contentResourceHelper != null) {
                String cr = contentResourceHelper.getContentUrl(src);
                if (StringUtils.isNotBlank(cr)) {
                    if (cr.contains("master.m3u8")) {
                        return cr.replace("master.m3u8", "thumbnail.jpg");
                    }
                    return cr;
                }
            }

            String siteId = video.getSiteId();
            if (StringUtils.isNotBlank(siteId) && StringUtils.isNotBlank(video.getId())) {
                return "/access/content/group/" + siteId + "/" + resolveBaseFolder() + "/" + video.getId() + "/thumbnail.jpg";
            }

            return "";
        }

        return "";
    }

    private String extractYouTubeVideoId(String sourceReference) {
        if (StringUtils.isBlank(sourceReference)) {
            return "";
        }
        Matcher matcher = VideoTrainingConstants.YOUTUBE_PATTERN.matcher(sourceReference);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @Override
    public VideoTrainingVideo updateVideoVisibility(String videoId, VideoVisibilityScope newScope) {
        if (StringUtils.isBlank(videoId)) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        if (newScope == null) {
            throw new IllegalArgumentException("newScope must not be null");
        }

        VideoTrainingVideo existing = getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown videoId"));
        existing.setVisibilityScope(newScope);
        return saveVideo(existing);
    }

    @Override
    public VideoTrainingVideo updateVideoStatus(String videoId, VideoPublicationStatus newStatus) {
        if (StringUtils.isBlank(videoId)) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        VideoTrainingVideo existing = getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown videoId"));

        // Check and update source state first. If the source was detected missing we will
        // mark it as deleted and allow state transitions (except to PUBLISHED).
        boolean sourceStateChanged = ensureSourceState(existing);

        // If user tries to publish but source is missing, prevent it.
        VideoPublicationStatus requested = normalizePublicationStatus(newStatus);
        if (requested == VideoPublicationStatus.PUBLISHED
            && existing.isSourceDeleted()
            && (existing.getProviderType() == org.sakaiproject.videotraining.api.model.VideoProviderType.NATIVE
                || existing.getProviderType() == org.sakaiproject.videotraining.api.model.VideoProviderType.HLS_UPLOAD
                || existing.getProviderType() == org.sakaiproject.videotraining.api.model.VideoProviderType.RESOURCES)) {
            throw new IllegalStateException("video.training.cannotPublishSourceUnavailable");
        }

        if (!sourceStateChanged) {
            validatePublicationStatusTransition(normalizePublicationStatus(existing.getPublicationStatus()), normalizePublicationStatus(newStatus), existing.getVisibilityScope());
        }

        VideoPublicationStatus previous = normalizePublicationStatus(existing.getPublicationStatus());
        existing.setPublicationStatus(newStatus);

        VideoTrainingVideo saved = videoRepository.save(existing);
        String currentUserId = sessionManager.getCurrentSessionUserId();
        registerAudit(saved.getSiteId(), currentUserId, "VIDEO_UPDATED", saved.getId(), StringUtils.defaultString(saved.getSourceReference()));
        registerAudit(saved.getSiteId(), currentUserId, "PUBLICATION_STATUS_CHANGED", saved.getId(), previous.name() + "->" + normalizePublicationStatus(newStatus).name());
        invalidateListCaches();
        syncExternalProviderPrivacy(saved);
        return saved;
    }

    @Override
    public boolean ensureSourceState(VideoTrainingVideo video) {
        if (video == null) {
            return false;
        }

        boolean changed = false;
        try {
            if (video.getProviderType() == null) {
                return false;
            }
            switch (video.getProviderType()) {
                case NATIVE:
                case HLS_UPLOAD:
                case RESOURCES:
                    ContentResource resource = null;
                    if (contentResourceHelper != null) {
                        try {
                            resource = contentResourceHelper.getContentResource(video.getSourceReference());
                        } catch (IdUnusedException | PermissionException | TypeException e) {
                            resource = null;
                        }
                    }

                    if (resource == null) {
                        boolean needsUpdate = !video.isSourceDeleted() || video.getPublicationStatus() != VideoPublicationStatus.DRAFT;
                        if (needsUpdate) {
                            video.setSourceDeleted(true);
                            video.setPublicationStatus(VideoPublicationStatus.DRAFT);
                            video.setModifiedOn(Instant.now());
                            changed = true;
                        }
                    } else {
                        if (video.isSourceDeleted()) {
                            video.setSourceDeleted(false);
                            video.setModifiedOn(Instant.now());
                            changed = true;
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (RuntimeException e) {
            // Do not change state on unexpected runtime errors
        }

        return changed;
    }

    @Override
    public VideoTrainingVideo updateVideoSchedule(String videoId, Instant releaseDate, Instant retractDate) {
        if (StringUtils.isBlank(videoId)) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        VideoTrainingVideo existingVideo = getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown videoId"));

        boolean canManage = canManageVideo(videoId, videoId);

        if (!canManage) {
            throw new SecurityException("User cannot manage video " + videoId);
        }

        if (releaseDate != null && retractDate != null && releaseDate.isAfter(retractDate)) {
            throw new IllegalArgumentException("releaseDate must be before retractDate");
        }

        existingVideo.setReleaseDate(releaseDate);
        existingVideo.setRetractDate(retractDate);
        return saveVideo(existingVideo);
    }

    @Override
    public VideoTrainingVideo saveVideo(VideoTrainingVideo video) {
        if (video == null) {
            throw new IllegalArgumentException("video must not be null");
        }
        if (StringUtils.isBlank(video.getSiteId()) || StringUtils.isBlank(video.getTitle()) || StringUtils.isBlank(video.getSourceReference())) {
            throw new IllegalArgumentException("siteId, title and sourceReference are required");
        }

        if (video.getProviderType() == null) {
            video.setProviderType(VideoProviderType.NATIVE);
        }

        String sourceReference = StringUtils.trimToEmpty(video.getSourceReference());
        if (video.getProviderType() == VideoProviderType.EXTERNAL
            && !(sourceReference.regionMatches(true, 0, "http://", 0, 7)
            || sourceReference.regionMatches(true, 0, "https://", 0, 8))) {
            throw new IllegalArgumentException("external provider requires an absolute HTTP(S) sourceReference");
        }

        String currentUserId = sessionManager.getCurrentSessionUserId();
        String siteRefForVideo = siteService.siteReference(video.getSiteId());
        // Allow full site managers, or users with owner-level manage permission (for creation/edit of their own videos)
        if (StringUtils.isNotBlank(video.getId())) {
            // editing existing video: permit if manage all, superuser, or manage+owner
            VideoTrainingVideo existingCheck = getVideoById(video.getId()).orElse(null);
            if (!(securityService.isSuperUser(currentUserId)
                    || securityService.unlock(currentUserId, PERMISSION_MANAGE_ALL, siteRefForVideo)
                    || (securityService.unlock(currentUserId, PERMISSION_MANAGE, siteRefForVideo)
                        && existingCheck != null && Objects.equals(existingCheck.getOwnerId(), currentUserId)))) {
                throw new SecurityException("User cannot manage video library for site " + video.getSiteId());
            }
        } else {
            // creating new video: allow if superuser or has either manage_all or manage
            if (!(securityService.isSuperUser(currentUserId)
                    || securityService.unlock(currentUserId, PERMISSION_MANAGE_ALL, siteRefForVideo)
                    || securityService.unlock(currentUserId, PERMISSION_MANAGE, siteRefForVideo))) {
                throw new SecurityException("User cannot manage video library for site " + video.getSiteId());
            }
        }

        Instant now = Instant.now();
        VideoTrainingVideo existing = null;
        if (StringUtils.isBlank(video.getId())) {
            video.setCreatedOn(now);
            if (StringUtils.isBlank(video.getOwnerId())) {
                video.setOwnerId(currentUserId);
            }
        } else {
            existing = getVideoById(video.getId()).orElse(null);
            if (existing != null && StringUtils.isBlank(video.getOwnerId())) {
                video.setOwnerId(existing.getOwnerId());
            }
        }

        if (StringUtils.isBlank(video.getRequiredViewPermission())) {
            video.setRequiredViewPermission(VideoTrainingConstants.PERMISSION_VIEW);
        }

        if (video.getVisibilityScope() == null) {
            video.setVisibilityScope(VideoVisibilityScope.COURSE);
        }

        if (video.getVisibilityScope() == VideoVisibilityScope.GLOBAL
                && !canUseGlobalVisibility(video.getSiteId(), currentUserId)) {
            throw new SecurityException("User cannot set global visibility for site " + video.getSiteId());
        }

        VideoVisibilityScope previousVisibilityScope = existing != null && existing.getVisibilityScope() != null
            ? existing.getVisibilityScope()
            : VideoVisibilityScope.COURSE;

        boolean previousSourceDeleted = existing != null && existing.isSourceDeleted();

        VideoPublicationStatus requestedStatus = normalizePublicationStatus(video.getPublicationStatus());
        video.setPublicationStatus(requestedStatus);

        if (existing == null) {
            if (requestedStatus != VideoPublicationStatus.DRAFT) {
                throw new IllegalArgumentException("New videos must start in DRAFT status");
            }
        } else {
            VideoPublicationStatus currentStatus = normalizePublicationStatus(existing.getPublicationStatus());
            validatePublicationStatusTransition(currentStatus, requestedStatus, video.getVisibilityScope());
        }

        if (video.getProviderType() == VideoProviderType.NATIVE) {
            Long existingSizeBytes = existing != null ? existing.getFileSizeBytes() : null;
            long currentSize = existingSizeBytes != null ? existingSizeBytes : 0L;
            long requestedSize = video.getFileSizeBytes() != null ? Math.max(video.getFileSizeBytes(), 0L) : currentSize;
            video.setFileSizeBytes(requestedSize);
        }

        video.setModifiedOn(now);
        VideoTrainingVideo saved = videoRepository.save(video);
        if (existing != null && previousVisibilityScope != saved.getVisibilityScope()) {
            String visibilityChangeDetails = previousVisibilityScope.name() + "->" + saved.getVisibilityScope().name();
            registerAudit(saved.getSiteId(), currentUserId, "VISIBILITY_SCOPE_CHANGED", saved.getId(), visibilityChangeDetails);
        }
        if (existing != null && normalizePublicationStatus(existing.getPublicationStatus()) != normalizePublicationStatus(saved.getPublicationStatus())) {
            String publicationChangeDetails = normalizePublicationStatus(existing.getPublicationStatus()).name() + "->" + normalizePublicationStatus(saved.getPublicationStatus()).name();
            registerAudit(saved.getSiteId(), currentUserId, "PUBLICATION_STATUS_CHANGED", saved.getId(), publicationChangeDetails);
        }
        if (existing != null && previousSourceDeleted != saved.isSourceDeleted()) {
            String sourceChangeDetails = Boolean.toString(previousSourceDeleted) + "->" + saved.isSourceDeleted();
            registerAudit(saved.getSiteId(), currentUserId, "SOURCE_STATE_CHANGED", saved.getId(), sourceChangeDetails);
        }
        String action = existing == null ? "VIDEO_CREATED" : "VIDEO_UPDATED";
        registerAudit(saved.getSiteId(), currentUserId, action, saved.getId(), saved.getTitle());
        invalidateListCaches();
        return saved;
    }

    @Override
    public VideoTrainingVideo saveVideoWithCategoryIds(VideoTrainingVideo video, List<String> categoryIds) {
        VideoTrainingVideo saved = saveVideo(video);
        setVideoCategoryIds(saved.getId(), categoryIds);
        return getVideoById(saved.getId()).orElse(saved);
    }

    @Override
    public void syncExternalProviderPrivacy(VideoTrainingVideo video) {
        if (video == null || video.getProviderType() != VideoProviderType.YOUTUBE_UPLOAD
                || externalProviderRegistry == null || StringUtils.isBlank(video.getSourceReference())) {
            return;
        }

        externalProviderRegistry.findProviderByUrl(video.getSourceReference()).ifPresent(provider -> {
            try {
                provider.syncPrivacy(video.getSourceReference(), video.getPublicationStatus(), video.getVisibilityScope());
            } catch (RuntimeException ex) {
                log.warn("Failed to sync external provider privacy for video {}: {}", video.getId(), ex.toString());
            }
        });
    }

    @Override
    public VideoTrainingVideo persistVideoChanges(VideoTrainingVideo video) {
        return videoRepository.save(video);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> findAllVideos(String searchText, int offset, int size) {
        return videoRepository.findAll(searchText, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllVideos(String searchText) {
        return videoRepository.countAll(searchText);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoTrainingVideo> getVideoById(String videoId) {
        if (StringUtils.isBlank(videoId)) {
            return Optional.empty();
        }
        return videoRepository.findById(videoId);
    }

    @Override
    public void deleteVideo(String videoId) {
        VideoTrainingVideo existing = getVideoById(videoId).orElse(null);
        if (existing == null) {
            return;
        }

        String currentUserId = sessionManager.getCurrentSessionUserId();
        String siteRef = siteService.siteReference(existing.getSiteId());
        if (!(securityService.isSuperUser(currentUserId)
                || securityService.unlock(currentUserId, PERMISSION_MANAGE_ALL, siteRef)
                || (securityService.unlock(currentUserId, PERMISSION_MANAGE, siteRef) && Objects.equals(existing.getOwnerId(), currentUserId)))) {
            throw new SecurityException("User cannot delete video " + videoId);
        }

        List<VideoTrainingAnalyticsEvent> events = analyticsEventRepository.findByVideoIdOrderByEventTimeDesc(videoId);
        for (VideoTrainingAnalyticsEvent event : events) {
            analyticsEventRepository.delete(event);
        }

        lessonLinkRepository.deleteByVideoId(videoId);
        if (userVideoPreferenceRepository != null) {
            userVideoPreferenceRepository.deleteByVideoId(videoId);
        }

        existing.getCategories().clear();

        videoRepository.delete(existing);
        registerAudit(existing.getSiteId(), currentUserId, "VIDEO_DELETED", videoId, existing.getTitle());
        invalidateListCaches();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibrary(String siteId) {
        return videoRepository.findBySiteIdOrderByModifiedOnDesc(siteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryPage(String siteId, String searchText, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);

        if (safePage == 1) {
            ListCacheKey cacheKey = ListCacheKey.firstPageManage(siteId, normalizedSearchText, safeSize);
            List<VideoTrainingVideo> cached = readCachedList(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        int offset = (safePage - 1) * safeSize;
        List<VideoTrainingVideo> result = videoRepository.findBySiteIdOrderByModifiedOnDesc(siteId, normalizedSearchText, offset, safeSize);
        if (safePage == 1) {
            ListCacheKey cacheKey = ListCacheKey.firstPageManage(siteId, normalizedSearchText, safeSize);
            writeCachedList(cacheKey, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryPage(String siteId, String searchText, String categoryId, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);

        int offset = (safePage - 1) * safeSize;
        return videoRepository.findBySiteIdOrderByModifiedOnDesc(siteId, normalizedSearchText, categoryIds, offset, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryPageForOwner(String siteId, String ownerId, String searchText, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);

        if (safePage == 1) {
            // no caching for owner-specific lists for simplicity
        }

        int offset = (safePage - 1) * safeSize;
        return videoRepository.findBySiteIdAndOwnerIdOrderByModifiedOnDesc(siteId, ownerId, normalizedSearchText, offset, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryForOwner(String siteId, String ownerId) {
        return videoRepository.findBySiteIdAndOwnerIdOrderByModifiedOnDesc(siteId, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryPageForOwner(String siteId, String ownerId, String searchText, String categoryId, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);

        int offset = (safePage - 1) * safeSize;
        return videoRepository.findBySiteIdAndOwnerIdOrderByModifiedOnDesc(siteId, ownerId, normalizedSearchText, categoryIds, offset, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibraryCursor(String siteId, String searchText, Instant cursorModifiedOn, String cursorVideoId, int size) {
        int safeSize = sanitizePageSize(size);
        String normalizedSearchText = normalizeSearchText(searchText);
        return videoRepository.findBySiteIdOrderByModifiedOnDescCursor(siteId, normalizedSearchText, cursorModifiedOn, cursorVideoId, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibrarySorted(String siteId, String searchText, int offset, int size,
            String sortField, boolean ascending) {
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        return videoRepository.findBySiteIdSorted(siteId, normalizedSearchText, safeOffset, safeSize, sortField, ascending);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibrarySorted(String siteId, String searchText, String categoryId, int offset, int size,
            String sortField, boolean ascending) {
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);
        return videoRepository.findBySiteIdSorted(siteId, normalizedSearchText, categoryIds, safeOffset, safeSize, sortField, ascending);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibrarySortedForOwner(String siteId, String ownerId, String searchText, int offset, int size,
            String sortField, boolean ascending) {
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        return videoRepository.findBySiteIdAndOwnerIdSorted(siteId, ownerId, normalizedSearchText, safeOffset, safeSize, sortField, ascending);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteLibrarySortedForOwner(String siteId, String ownerId, String searchText, String categoryId, int offset, int size,
            String sortField, boolean ascending) {
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);
        return videoRepository.findBySiteIdAndOwnerIdSorted(siteId, ownerId, normalizedSearchText, categoryIds, safeOffset, safeSize, sortField, ascending);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteLibrary(String siteId, String searchText) {
        String normalizedSearchText = normalizeSearchText(searchText);
        ListCacheKey cacheKey = ListCacheKey.countManage(siteId, normalizedSearchText);
        Long cached = readCachedCount(cacheKey);
        if (cached != null) {
            return cached;
        }

        long count = videoRepository.countBySiteId(siteId, normalizedSearchText);
        writeCachedCount(cacheKey, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteLibrary(String siteId, String searchText, String categoryId) {
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);
        return videoRepository.countBySiteId(siteId, normalizedSearchText, categoryIds);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteLibraryForOwner(String siteId, String ownerId, String searchText) {
        String normalizedSearchText = normalizeSearchText(searchText);
        // no caching for owner-specific counts for simplicity
        return videoRepository.countBySiteIdAndOwnerId(siteId, ownerId, normalizedSearchText);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteLibraryForOwner(String siteId, String ownerId, String searchText, String categoryId) {
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);
        return videoRepository.countBySiteIdAndOwnerId(siteId, ownerId, normalizedSearchText, categoryIds);
    }

    @Override
    @Transactional(readOnly = true)
    public long countGlobalVideosForUser(String userId, String searchText) {
        String query = normalizeSearchText(searchText);

        if (securityService.isSuperUser(userId)) {
            return adminCountAllGlobal(query);
        } else {
            return countGlobalVideos(query);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteVideosForUser(String siteId, String userId, String searchText) {
        String query = normalizeSearchText(searchText);

        // Is the user a full site manager (manage_all / superuser)? If so, count all videos.
        if (canManageLibrary(siteId, userId)) {
            return countSiteLibrary(siteId, query);
        }

        // Is the user an owner-level manager (manage)? If so, count their videos.
        if (hasManagePermission(siteId, userId)) {
            return countSiteLibraryForOwner(siteId, userId, query);
        }

        // Otherwise, count the visible videos for the user.
        return countVisibleVideosForUser(siteId, userId, Instant.now(), query);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCategoriesForSite(String siteId, String userId) {
        boolean hasAccess = siteService.allowAccessSite(siteId);

        if (!hasAccess) {
            return 0L;
        }

        return categoryRepository.countBySiteId(siteId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTopLevelCategoriesForSite(String siteId, String userId, String searchText) {
        if (StringUtils.isBlank(siteId) || !siteService.allowAccessSite(siteId)) {
            return 0L;
        }

        return getCategoryTree(siteId, searchText, 1, Integer.MAX_VALUE).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSiteViewableVideosForUser(String siteId, String userId, String searchText) {
        String query = normalizeSearchText(searchText);

        // Full site managers see all videos
        if (canManageLibrary(siteId, userId)) {
            return countSiteLibrary(siteId, query);
        }

        // Owner-level managers should be treated as viewers for this count (show visible videos)
        if (hasManagePermission(siteId, userId) || hasViewPermission(siteId, userId)) {
            return countVisibleVideosForUser(siteId, userId, Instant.now(), query);
        }

        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long countGlobalVideos(String searchText) {
        String normalizedSearchText = normalizeSearchText(searchText);
        ListCacheKey cacheKey = new ListCacheKey("count-global", null, null, normalizedSearchText, 0, 0L);
        Long cached = readCachedCount(cacheKey);
        if (cached != null) {
            return cached;
        }

        long count = videoRepository.countByGlobal(normalizedSearchText);
        writeCachedCount(cacheKey, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUser(String siteId, String userId, Instant now) {
        Instant effectiveNow = now != null ? now : Instant.now();
        List<VideoTrainingVideo> visible = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAt(siteId, effectiveNow)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                visible.add(video);
            }
        }
        visible.sort(Comparator.comparing(VideoTrainingVideo::getModifiedOn, Comparator.nullsLast(Comparator.reverseOrder())));
        return visible;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUserPage(String siteId, String userId, Instant now, String searchText, int page, int size) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);
        long visibilityBucket = visibilityBucket(effectiveNow);

        if (safePage == 1) {
            ListCacheKey cacheKey = ListCacheKey.firstPageVisible(siteId, userId, normalizedSearchText, safeSize, visibilityBucket);
            List<VideoTrainingVideo> cached = readCachedList(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        int offset = (safePage - 1) * safeSize;

        List<VideoTrainingVideo> results = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAt(siteId, effectiveNow, normalizedSearchText, offset, safeSize)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                results.add(video);
            }
        }

        if (safePage == 1) {
            ListCacheKey cacheKey = ListCacheKey.firstPageVisible(siteId, userId, normalizedSearchText, safeSize, visibilityBucket);
            writeCachedList(cacheKey, results);
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUserPage(String siteId, String userId, Instant now, String searchText, String categoryId, int page, int size) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);

        int offset = (safePage - 1) * safeSize;
        List<VideoTrainingVideo> results = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAt(siteId, effectiveNow, normalizedSearchText, categoryIds, offset, safeSize)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                results.add(video);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getGlobalVideosForUser(String userId, String searchText, int page, int size) {
        String query = normalizeSearchText(searchText);
        int safePage = Math.max(1, page);
        int safeSize = sanitizePageSize(size);

        if (securityService.isSuperUser(userId)) {
            return getAdminAllGlobalVideosPage(query, safePage, safeSize);
        } else {
            return getVisibleGlobalVideosPage(query, safePage, safeSize);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteVideosForUserPage(String siteId, String userId, String searchText, int page, int size) {
        String query = normalizeSearchText(searchText);
        int safePage = Math.max(1, page);
        int safeSize = sanitizePageSize(size);

        // Is the user a full site manager (manage_all / superuser)? If so, show all videos.
        if (canManageLibrary(siteId, userId)) {
            return getSiteLibraryPage(siteId, query, safePage, safeSize);
        }

        // Is the user an owner-level manager (manage)? If so, show their videos.
        if (hasManagePermission(siteId, userId)) {
            return getSiteLibraryPageForOwner(siteId, userId, query, safePage, safeSize);
        }

        if (hasViewPermission(siteId, userId)) {
            // Otherwise, show the visible videos for the user.
            return getVisibleVideosForUserPage(siteId, userId, Instant.now(), query, safePage, safeSize);
        }

        // If the user doesn't even have view permission for the site, return an empty list.
        return Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getSiteViewableVideosForUserPage(String siteId, String userId, String searchText, int page, int size) {
        String query = normalizeSearchText(searchText);
        int safePage = Math.max(1, page);
        int safeSize = sanitizePageSize(size);

        // Full site managers (manage_all / superuser) still see all videos.
        if (canManageLibrary(siteId, userId)) {
            return getSiteLibraryPage(siteId, query, safePage, safeSize);
        }

        // If the user has owner-level manage permission, for this view we treat it as a student
        if (hasManagePermission(siteId, userId) || hasViewPermission(siteId, userId)) {
            return getVisibleVideosForUserPage(siteId, userId, Instant.now(), query, safePage, safeSize);
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleGlobalVideosPage(String searchText, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);

        int offset = (safePage - 1) * safeSize;

        List<VideoTrainingVideo> results = videoRepository.findVisibleByGlobal(normalizedSearchText, offset, safeSize)
                .stream().toList();

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getAdminAllGlobalVideosPage(String searchText, int page, int size) {
        int safeSize = sanitizePageSize(size);
        int safePage = sanitizePage(page);
        String normalizedSearchText = normalizeSearchText(searchText);

        int offset = (safePage - 1) * safeSize;

        return videoRepository.adminFindAllGlobal(normalizedSearchText, offset, safeSize).stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long adminCountAllGlobal(String searchText) {
        String normalizedSearchText = normalizeSearchText(searchText);
        ListCacheKey cacheKey = new ListCacheKey("count-admin-global", null, null, normalizedSearchText, 0, 0L);
        Long cached = readCachedCount(cacheKey);
        if (cached != null) {
            return cached;
        }

        long count = videoRepository.adminCountAllGlobal(normalizedSearchText);
        writeCachedCount(cacheKey, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUserCursor(String siteId, String userId, Instant now, String searchText,
            Instant cursorModifiedOn, String cursorVideoId, int size) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeSize = sanitizePageSize(size);
        String normalizedSearchText = normalizeSearchText(searchText);

        List<VideoTrainingVideo> results = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAtCursor(siteId, effectiveNow, normalizedSearchText,
                cursorModifiedOn, cursorVideoId, safeSize)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                results.add(video);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUserSorted(String siteId, String userId, Instant now, String searchText,
            int offset, int size, String sortField, boolean ascending) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);

        List<VideoTrainingVideo> results = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAtSorted(siteId, effectiveNow, normalizedSearchText,
                safeOffset, safeSize, sortField, ascending)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                results.add(video);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVisibleVideosForUserSorted(String siteId, String userId, Instant now, String searchText, String categoryId,
            int offset, int size, String sortField, boolean ascending) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);

        List<VideoTrainingVideo> results = new ArrayList<>();
        for (VideoTrainingVideo video : videoRepository.findVisibleBySiteIdAtSorted(siteId, effectiveNow, normalizedSearchText, categoryIds,
                safeOffset, safeSize, sortField, ascending)) {
            if (canViewVideo(video, userId, effectiveNow)) {
                results.add(video);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public long countVisibleVideosForUser(String siteId, String userId, Instant now, String searchText) {
        Instant effectiveNow = now != null ? now : Instant.now();
        String normalizedSearchText = normalizeSearchText(searchText);
        long visibilityBucket = visibilityBucket(effectiveNow);

        String siteRef = siteService.siteReference(siteId);
        if (!securityService.isSuperUser(userId)
                && !securityService.unlock(userId, PERMISSION_MANAGE_ALL, siteRef)
                && !securityService.unlock(userId, PERMISSION_VIEW, siteRef)) {
            return 0;
        }

        ListCacheKey cacheKey = ListCacheKey.countVisible(siteId, userId, normalizedSearchText, visibilityBucket);
        Long cached = readCachedCount(cacheKey);
        if (cached != null) {
            return cached;
        }

        long count = videoRepository.countVisibleBySiteIdAt(siteId, effectiveNow, normalizedSearchText);
        writeCachedCount(cacheKey, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countVisibleVideosForUser(String siteId, String userId, Instant now, String searchText, String categoryId) {
        Instant effectiveNow = now != null ? now : Instant.now();
        String normalizedSearchText = normalizeSearchText(searchText);
        List<String> categoryIds = resolveCategoryFilterIds(siteId, categoryId);
        return videoRepository.countVisibleBySiteIdAt(siteId, effectiveNow, normalizedSearchText, categoryIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getGlobalVideosCursor(String searchText, Instant cursorModifiedOn, String cursorVideoId, int size) {
        int safeSize = sanitizePageSize(size);
        String normalizedSearchText = normalizeSearchText(searchText);
        return videoRepository.findGlobalPublishedCursor(normalizedSearchText, cursorModifiedOn, cursorVideoId, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getGlobalVideosSorted(String searchText, int offset, int size,
            String sortField, boolean ascending) {
        int safeSize = sanitizePageSize(size);
        int safeOffset = Math.max(0, offset);
        String normalizedSearchText = normalizeSearchText(searchText);
        return videoRepository.findGlobalPublishedSorted(normalizedSearchText, safeOffset, safeSize, sortField, ascending);
    }

    @Override
    public void registerView(String siteId, String videoId, String userId, Instant when) {
        registerView(siteId, videoId, userId, when, null);
    }

    @Override
    public void registerView(String siteId, String videoId, String userId, Instant when, String lessonPageId) {
        if (StringUtils.isAnyBlank(siteId, videoId, userId)) {
            return;
        }

        VideoTrainingVideo video = getVideoById(videoId).orElse(null);
        if (video == null || !Objects.equals(video.getSiteId(), siteId)) {
            return;
        }

        if (!canViewVideo(video, userId, when, lessonPageId)) {
            return;
        }

        VideoTrainingAnalyticsEvent event = new VideoTrainingAnalyticsEvent();
        event.setSiteId(siteId);
        event.setVideoId(videoId);
        event.setUserId(userId);
        event.setEventType("view");
        event.setEventTime(when != null ? when : Instant.now());
        analyticsEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingAnalyticsEvent> getEventsForVideo(String videoId) {
        return analyticsEventRepository.findByVideoIdOrderByEventTimeDesc(videoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingAnalyticsSummary> getSiteAnalyticsSummary(String siteId) {
        return getSiteAnalyticsSummary(siteId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingAnalyticsSummary> getSiteAnalyticsSummary(String siteId, String categoryId) {
        List<VideoTrainingAnalyticsEvent> events = analyticsEventRepository.findBySiteIdAndEventType(siteId, "view");
        Set<String> filteredVideoIds = resolveAnalyticsVideoIds(siteId, categoryId);
        if (!filteredVideoIds.isEmpty()) {
            List<VideoTrainingAnalyticsEvent> filteredEvents = new ArrayList<>();
            for (VideoTrainingAnalyticsEvent event : events) {
                if (event != null && filteredVideoIds.contains(event.getVideoId())) {
                    filteredEvents.add(event);
                }
            }
            events = filteredEvents;
        }

        Map<String, Long> totalViewsByVideo = new HashMap<>();
        Map<String, Set<String>> usersByVideo = new HashMap<>();

        for (VideoTrainingAnalyticsEvent event : events) {
            totalViewsByVideo.merge(event.getVideoId(), 1L, Long::sum);
            usersByVideo.computeIfAbsent(event.getVideoId(), key -> new HashSet<>()).add(event.getUserId());
        }

        List<VideoTrainingAnalyticsSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totalViewsByVideo.entrySet()) {
            Set<String> users = usersByVideo.getOrDefault(entry.getKey(), Set.of());
            summaries.add(new VideoTrainingAnalyticsSummary(entry.getKey(), entry.getValue(), users.size()));
        }

        summaries.sort(Comparator.comparing(VideoTrainingAnalyticsSummary::getViewCount).reversed());
        return summaries;
    }

    @Override
    public boolean canManageLibrary(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_MANAGE_ALL, siteService.siteReference(siteId));
    }

    @Override
    public boolean hasManagePermission(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_MANAGE, siteService.siteReference(siteId)) || securityService.unlock(userId, PERMISSION_MANAGE_ALL, siteService.siteReference(siteId));
    }

    @Override
    public boolean hasViewPermission(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_VIEW, siteService.siteReference(siteId));
    }

    @Override
    public boolean canViewVideo(VideoTrainingVideo video, String userId, Instant now) {
        return canViewVideo(video, userId, now, null);
    }

    @Override
    public boolean canViewVideo(VideoTrainingVideo video, String userId, Instant now, String lessonPageId) {
        if (video == null || StringUtils.isBlank(userId)) {
            return false;
        }

        if (canManageLibrary(video.getSiteId(), userId)) {
            return true;
        }

        // Owner-level managers can view videos they own (including drafts/unpublished)
        if (hasManagePermission(video.getSiteId(), userId) && Objects.equals(video.getOwnerId(), userId)) {
            return true;
        }

        if (!isPublishedForEndUsers(video)) {
            return false;
        }

        Instant effectiveNow = now != null ? now : Instant.now();
        if (video.getReleaseDate() != null && effectiveNow.isBefore(video.getReleaseDate())) {
            return false;
        }
        if (video.getRetractDate() != null && !effectiveNow.isBefore(video.getRetractDate())) {
            return false;
        }

        if (video.getVisibilityScope() == VideoVisibilityScope.GLOBAL) {
            return true;
        }

        if (video.getVisibilityScope() == VideoVisibilityScope.LESSON) {
            return StringUtils.isNotBlank(lessonPageId);
        }

        if (!isCatalogVisibleScope(video)) {
            return false;
        }

        String siteRef = siteService.siteReference(video.getSiteId());

        if (!securityService.unlock(userId, PERMISSION_VIEW, siteRef)) {
            return false;
        }

        String requiredPermission = StringUtils.defaultIfBlank(video.getRequiredViewPermission(), VideoTrainingConstants.PERMISSION_VIEW);
        return securityService.unlock(userId, requiredPermission, siteRef);
    }

    @Override
    public boolean canManageCategories(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_CATEGORIES_MANAGE, siteService.siteReference(siteId));
    }

    private boolean canUseGlobalVisibility(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_GLOBAL, siteService.siteReference(siteId));
    }

    private boolean isSharedSiteId(String siteId) {
        return StringUtils.isNotBlank(siteId) && (siteId.startsWith("!") || siteId.startsWith("~"));
    }

    private boolean isSharedCategory(VideoTrainingCategory category) {
        if (category == null || StringUtils.isBlank(category.getSiteId())) {
            return false;
        }

        if (category.getSiteId().startsWith("!")) {
            return true;
        }

        return category.getSiteId().startsWith("~") && securityService.isSuperUser(category.getCreatedBy());
    }

    private List<VideoTrainingCategory> loadSharedCategories() {
        List<VideoTrainingCategory> sharedCategories = new ArrayList<>(categoryRepository.findBySiteIdStartingWithOrderBySortOrderAscNameAsc("!", 0, 0));
        for (VideoTrainingCategory category : categoryRepository.findBySiteIdStartingWithOrderBySortOrderAscNameAsc("~", 0, 0)) {
            if (isSharedCategory(category)) {
                sharedCategories.add(category);
            }
        }
        return sharedCategories;
    }

    @Override
    public boolean canViewAnalytics(String siteId, String userId) {
        if (securityService.isSuperUser(userId)) {
            return true;
        }
        return securityService.unlock(userId, PERMISSION_ANALYTICS, siteService.siteReference(siteId));
    }

    @Override
    public Long getSiteStorageQuotaBytes(String siteId) {
        if (StringUtils.isBlank(siteId)) {
            throw new IllegalArgumentException("siteId is required");
        }

        try {
            String siteCollectionId = contentHostingService.getSiteCollection(siteId);
            ContentCollection siteCollection = contentHostingService.getCollection(siteCollectionId);
            long quotaKb = contentHostingService.getQuota(siteCollection);
            if (quotaKb > 0) {
                return quotaKb * 1024L;
            }
        } catch (Exception e) {
            return 0L;
        }

        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long getSiteStorageUsageBytes(String siteId) {
        if (StringUtils.isBlank(siteId)) {
            return 0L;
        }

        try {
            String siteCollectionId = contentHostingService.getSiteCollection(siteId);
            ContentCollection siteCollection = contentHostingService.getCollection(siteCollectionId);
            return siteCollection.getBodySizeK() * 1024L;
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public void registerAudit(String siteId, String userId, String action, String videoId, String details) {
        if (StringUtils.isAnyBlank(siteId, userId, action)) {
            return;
        }

        if (eventTrackingService == null) {
            return;
        }

        String normalizedAction = action.toLowerCase(Locale.ROOT).replace('_', '.');
        String eventName = "video.training." + normalizedAction;
        String reference = "/video-training/" + siteId + "/" + StringUtils.defaultIfBlank(videoId, "-");
        eventTrackingService.post(eventTrackingService.newEvent(
                eventName,
                reference,
                siteId,
                true,
            NotificationService.NOTI_OPTIONAL));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingCategory> getCategories(String siteId, int page, int size) {
        int offset = (page - 1) * size;
        return categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingCategory> getSelectableCategoryTree(String siteId, String searchText, int page, int size) {
        if (StringUtils.isBlank(siteId) || !siteService.allowAccessSite(siteId)) {
            return Collections.emptyList();
        }

        List<VideoTrainingCategory> allCategories = new ArrayList<>(categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, 0, 0));
        if (!isSharedSiteId(siteId)) {
            allCategories.addAll(loadSharedCategories());
        }
        return buildCategoryTree(allCategories, searchText, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingCategory> getCategoryTree(String siteId, String searchText, int page, int size) {
        if (StringUtils.isBlank(siteId) || !siteService.allowAccessSite(siteId)) {
            return Collections.emptyList();
        }

        List<VideoTrainingCategory> allCategories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, 0, 0);
        return buildCategoryTree(allCategories, searchText, page, size);
    }

    private List<VideoTrainingCategory> buildCategoryTree(List<VideoTrainingCategory> allCategories, String searchText, int page, int size) {
        if (allCategories.isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedSearch = normalizeSearchText(searchText);
        Map<String, VideoTrainingCategory> byId = new HashMap<>();
        for (VideoTrainingCategory category : allCategories) {
            category.setChildren(new ArrayList<>());
            category.setHasChildren(false);
            category.setVideoCount(0L);
            byId.put(category.getId(), category);
        }

        List<VideoTrainingCategory> roots = new ArrayList<>();
        for (VideoTrainingCategory category : allCategories) {
            String parentId = category.getParentCategoryId();
            VideoTrainingCategory parent = StringUtils.isNotBlank(parentId) ? byId.get(parentId) : null;
            if (parent == null) {
                roots.add(category);
            } else {
                parent.getChildren().add(category);
                parent.setHasChildren(true);
            }
        }

        for (VideoTrainingCategory category : allCategories) {
            category.setVideoCount(countUniqueVideos(category));
        }

        List<VideoTrainingCategory> filteredRoots = new ArrayList<>();
        for (VideoTrainingCategory root : roots) {
            VideoTrainingCategory filtered = filterCategoryBranch(root, normalizedSearch);
            if (filtered != null) {
                filteredRoots.add(filtered);
            }
        }

        int safeSize = Math.max(1, size);
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * safeSize;
        if (offset >= filteredRoots.size()) {
            return Collections.emptyList();
        }

        int endIndex = Math.min(filteredRoots.size(), offset + safeSize);
        return new ArrayList<>(filteredRoots.subList(offset, endIndex));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoTrainingCategory> getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getVideosByCategoryId(String siteId, String categoryId) {
        if (StringUtils.isAnyBlank(siteId, categoryId)) {
            return Collections.emptyList();
        }

        VideoTrainingCategory selectedCategory = categoryRepository.findById(categoryId).orElse(null);
        if (selectedCategory == null || (!Objects.equals(selectedCategory.getSiteId(), siteId) && !isSharedCategory(selectedCategory))) {
            return Collections.emptyList();
        }

        String categorySiteId = selectedCategory.getSiteId();
        List<VideoTrainingCategory> categories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(categorySiteId, 0, 0);
        Map<String, List<VideoTrainingCategory>> childrenByParent = new HashMap<>();
        for (VideoTrainingCategory category : categories) {
            childrenByParent.computeIfAbsent(category.getParentCategoryId(), ignored -> new ArrayList<>()).add(category);
        }

        Set<String> categoryIds = new HashSet<>();
        collectCategoryIds(selectedCategory.getId(), childrenByParent, categoryIds);
    return isSharedCategory(selectedCategory)
        ? videoRepository.findByCategoryIds(new ArrayList<>(categoryIds))
        : videoRepository.findBySiteIdAndCategoryIds(siteId, new ArrayList<>(categoryIds));
    }

    @Override
    @Transactional(readOnly = true)
    public VideoTrainingCategoryDeleteImpact getCategoryDeleteImpact(String categoryId) {
        return buildCategoryDeleteImpact(categoryId);
    }

    @Override
    public VideoTrainingCategory saveCategory(VideoTrainingCategory category) {
        if (category == null || StringUtils.isBlank(category.getSiteId()) || StringUtils.isBlank(category.getName())) {
            throw new IllegalArgumentException("Invalid category payload");
        }

        String userId = sessionManager.getCurrentSessionUserId();
        if (!canManageCategories(category.getSiteId(), userId)) {
            throw new SecurityException("User cannot manage categories for site " + category.getSiteId());
        }

        VideoTrainingCategory existing = StringUtils.isBlank(category.getId())
                ? null
                : categoryRepository.findById(category.getId()).orElse(null);
        if (existing != null && !Objects.equals(existing.getSiteId(), category.getSiteId())) {
            throw new IllegalArgumentException("Category does not belong to the requested site");
        }

        validateCategoryHierarchy(category, existing);

        Instant now = Instant.now();
        if (existing != null) {
            if (category.getCreatedOn() == null) {
                category.setCreatedOn(existing.getCreatedOn());
            }
            if (StringUtils.isBlank(category.getCreatedBy())) {
                category.setCreatedBy(existing.getCreatedBy());
            }
        } else {
            if (category.getCreatedOn() == null) {
                category.setCreatedOn(now);
            }
            if (StringUtils.isBlank(category.getCreatedBy())) {
                category.setCreatedBy(userId);
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(nextCategorySortOrder(category.getSiteId(), category.getParentCategoryId()));
            }
        }
        category.setModifiedOn(now);
        category.setModifiedBy(userId);
        if (existing != null && category.getSortOrder() == null) {
            category.setSortOrder(existing.getSortOrder());
        }

        VideoTrainingCategory saved = categoryRepository.save(category);
        registerAudit(saved.getSiteId(), userId, "CATEGORY_SAVED", null, saved.getName());
        return saved;
    }

    @Override
    public void reorderCategories(String siteId, List<VideoTrainingCategoryOrderUpdate> categoryOrder) {
        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(siteId) || !canManageCategories(siteId, userId)) {
            throw new SecurityException("User cannot manage categories for site " + siteId);
        }

        List<VideoTrainingCategory> allCategories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, 0, 0);
        Map<String, VideoTrainingCategory> byId = new HashMap<>();
        for (VideoTrainingCategory category : allCategories) {
            byId.put(category.getId(), category);
        }

        if (categoryOrder == null || categoryOrder.size() != allCategories.size()) {
            throw new IllegalArgumentException("Category order payload is incomplete");
        }

        Set<String> seen = new HashSet<>();
        for (VideoTrainingCategoryOrderUpdate update : categoryOrder) {
            if (update == null || StringUtils.isBlank(update.getId()) || !seen.add(update.getId())) {
                throw new IllegalArgumentException("Invalid category order payload");
            }

            VideoTrainingCategory category = byId.get(update.getId());
            if (category == null) {
                throw new IllegalArgumentException("Unknown category in reorder payload");
            }

            String requestedParentId = StringUtils.trimToNull(update.getParentCategoryId());
            if (requestedParentId != null) {
                if (Objects.equals(requestedParentId, category.getId())) {
                    throw new IllegalArgumentException("A category cannot be its own parent");
                }
                VideoTrainingCategory parent = byId.get(requestedParentId);
                if (parent == null || !Objects.equals(parent.getSiteId(), siteId) || StringUtils.isNotBlank(parent.getParentCategoryId())) {
                    throw new IllegalArgumentException("Invalid parent category in reorder payload");
                }
                if (hasChildCategories(siteId, category.getId())) {
                    throw new IllegalArgumentException("Categories with children cannot be moved below another category");
                }
            }

            category.setParentCategoryId(requestedParentId);
            category.setSortOrder(update.getSortOrder() != null ? update.getSortOrder() : 0);
            category.setModifiedOn(Instant.now());
            category.setModifiedBy(userId);
            categoryRepository.save(category);
        }

        if (seen.size() != allCategories.size()) {
            throw new IllegalArgumentException("Category order payload is incomplete");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        VideoTrainingCategoryDeleteImpact impact = buildCategoryDeleteImpact(categoryId);
        String userId = sessionManager.getCurrentSessionUserId();

        Set<String> categoryIdsToDelete = new HashSet<>();
        for (VideoTrainingCategory category : impact.getCategoriesToDelete()) {
            categoryIdsToDelete.add(category.getId());
        }

        for (VideoTrainingVideo video : impact.getAffectedVideos()) {
            video.getCategories().removeIf(category -> categoryIdsToDelete.contains(category.getId()));
            videoRepository.save(video);
        }

        for (VideoTrainingCategory category : impact.getCategoriesToDelete()) {
            categoryRepository.delete(category);
        }

        registerAudit(impact.getCategory().getSiteId(), userId, "CATEGORY_DELETED", null, impact.getCategory().getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVideoCategoryIds(String videoId) {

        VideoTrainingVideo video = getVideoById(videoId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown video"));

        return video.getCategories()
            .stream()
            .map(VideoTrainingCategory::getId)
            .toList();
    }

    private VideoTrainingCategoryDeleteImpact buildCategoryDeleteImpact(String categoryId) {
        VideoTrainingCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            throw new IllegalArgumentException("Unknown category");
        }

        String userId = sessionManager.getCurrentSessionUserId();
        if (!canManageCategories(category.getSiteId(), userId)) {
            throw new SecurityException("User cannot manage categories for site " + category.getSiteId());
        }

        List<VideoTrainingCategory> allCategories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(category.getSiteId(), 0, 0);
        Map<String, List<VideoTrainingCategory>> childrenByParent = new HashMap<>();
        for (VideoTrainingCategory candidate : allCategories) {
            childrenByParent.computeIfAbsent(candidate.getParentCategoryId(), ignored -> new ArrayList<>()).add(candidate);
        }

        List<VideoTrainingCategory> categoriesToDelete = new ArrayList<>();
        collectCategoriesForDelete(category, childrenByParent, categoriesToDelete);

        Map<String, VideoTrainingVideo> affectedVideosById = new HashMap<>();
        for (VideoTrainingCategory categoryToDelete : categoriesToDelete) {
            if (categoryToDelete.getVideos() != null) {
                for (VideoTrainingVideo video : categoryToDelete.getVideos()) {
                    if (video != null && StringUtils.isNotBlank(video.getId())) {
                        affectedVideosById.putIfAbsent(video.getId(), video);
                    }
                }
            }
        }

        if (isSharedCategory(category)) {
            List<String> categoryIds = categoriesToDelete.stream().map(VideoTrainingCategory::getId).toList();
            for (VideoTrainingVideo video : videoRepository.findByCategoryIds(categoryIds)) {
                if (video != null && StringUtils.isNotBlank(video.getId())) {
                    affectedVideosById.putIfAbsent(video.getId(), video);
                }
            }
        }

        VideoTrainingCategoryDeleteImpact impact = new VideoTrainingCategoryDeleteImpact();
        impact.setCategory(category);
        impact.setCategoriesToDelete(categoriesToDelete);
        impact.setAffectedVideos(new ArrayList<>(affectedVideosById.values()));
        return impact;
    }

    private Set<String> resolveAnalyticsVideoIds(String siteId, String categoryId) {
        if (StringUtils.isAnyBlank(siteId, categoryId)) {
            return Collections.emptySet();
        }

        VideoTrainingCategory selectedCategory = categoryRepository.findById(categoryId).orElse(null);
        if (selectedCategory == null || (!Objects.equals(selectedCategory.getSiteId(), siteId) && !isSharedCategory(selectedCategory))) {
            return Collections.emptySet();
        }

        String categorySiteId = selectedCategory.getSiteId();
        List<VideoTrainingCategory> categories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(categorySiteId, 0, 0);
        Map<String, List<VideoTrainingCategory>> childrenByParent = new HashMap<>();
        for (VideoTrainingCategory category : categories) {
            childrenByParent.computeIfAbsent(category.getParentCategoryId(), ignored -> new ArrayList<>()).add(category);
        }

        Set<String> categoryIdsToInclude = new HashSet<>();
        collectCategoryIds(selectedCategory.getId(), childrenByParent, categoryIdsToInclude);

        List<VideoTrainingVideo> videos = isSharedCategory(selectedCategory)
                ? videoRepository.findByCategoryIds(new ArrayList<>(categoryIdsToInclude))
                : videoRepository.findBySiteIdAndCategoryIds(siteId, new ArrayList<>(categoryIdsToInclude));
        Set<String> videoIds = new HashSet<>();
        for (VideoTrainingVideo video : videos) {
            if (video != null && StringUtils.isNotBlank(video.getId())) {
                videoIds.add(video.getId());
            }
        }
        return videoIds;
    }

    private List<String> resolveCategoryFilterIds(String siteId, String categoryId) {
        if (StringUtils.isAnyBlank(siteId, categoryId)) {
            return Collections.emptyList();
        }

        VideoTrainingCategory selectedCategory = categoryRepository.findById(categoryId).orElse(null);
        if (selectedCategory == null || (!Objects.equals(selectedCategory.getSiteId(), siteId) && !isSharedCategory(selectedCategory))) {
            return Collections.emptyList();
        }

        String categorySiteId = selectedCategory.getSiteId();
        List<VideoTrainingCategory> categories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(categorySiteId, 0, 0);
        Map<String, List<VideoTrainingCategory>> childrenByParent = new HashMap<>();
        for (VideoTrainingCategory category : categories) {
            childrenByParent.computeIfAbsent(category.getParentCategoryId(), ignored -> new ArrayList<>()).add(category);
        }

        Set<String> categoryIds = new HashSet<>();
        collectCategoryIds(selectedCategory.getId(), childrenByParent, categoryIds);
        return new ArrayList<>(categoryIds);
    }

    private void collectCategoryIds(String categoryId, Map<String, List<VideoTrainingCategory>> childrenByParent, Set<String> categoryIds) {
        if (StringUtils.isBlank(categoryId) || !categoryIds.add(categoryId)) {
            return;
        }

        for (VideoTrainingCategory child : childrenByParent.getOrDefault(categoryId, Collections.emptyList())) {
            collectCategoryIds(child.getId(), childrenByParent, categoryIds);
        }
    }

    private Integer nextCategorySortOrder(String siteId, String parentCategoryId) {
        List<VideoTrainingCategory> categories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, 0, 0);
        int maxSortOrder = -1;
        String normalizedParentId = StringUtils.trimToNull(parentCategoryId);
        for (VideoTrainingCategory category : categories) {
            if (Objects.equals(StringUtils.trimToNull(category.getParentCategoryId()), normalizedParentId)) {
                maxSortOrder = Math.max(maxSortOrder, category.getSortOrder() != null ? category.getSortOrder() : 0);
            }
        }
        return maxSortOrder + 1;
    }

    private void collectCategoriesForDelete(VideoTrainingCategory currentCategory, Map<String, List<VideoTrainingCategory>> childrenByParent, List<VideoTrainingCategory> categoriesToDelete) {
        List<VideoTrainingCategory> children = childrenByParent.getOrDefault(currentCategory.getId(), Collections.emptyList());

        for (VideoTrainingCategory child : children) {
            collectCategoriesForDelete(child, childrenByParent, categoriesToDelete);
        }

        categoriesToDelete.add(currentCategory);
    }

    @Override
    public void setVideoCategoryIds(String videoId, List<String> categoryIds) {
        VideoTrainingVideo video = getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown video"));
        String userId = sessionManager.getCurrentSessionUserId();
        if (!canManageVideo(videoId, userId)) {
            throw new SecurityException("User cannot manage categories for this video");
        }

        video.getCategories().clear();

        if (categoryIds != null) {
            for (String categoryId : categoryIds) {
                if (StringUtils.isBlank(categoryId)) {
                    continue;
                }
                VideoTrainingCategory category = categoryRepository.findById(categoryId).orElse(null);
                if (category == null || (!Objects.equals(category.getSiteId(), video.getSiteId()) && !isSharedCategory(category))) {
                    continue;
                }
                video.getCategories().add(category);
            }
        }
        videoRepository.save(video);
        registerAudit(video.getSiteId(), userId, "VIDEO_CATEGORIES_UPDATED", videoId, String.join(",", getVideoCategoryIds(videoId)));
    }

    @Override
    public boolean canManageVideo(String videoId, String userId) {
        VideoTrainingVideo video = getVideoById(videoId).orElse(null);
        if (video == null) {
            return false;
        }

        String siteRefForVideo = siteService.siteReference(video.getSiteId());

        return securityService.isSuperUser(userId)
            || securityService.unlock(userId, PERMISSION_MANAGE_ALL, siteRefForVideo)
            || (securityService.unlock(userId, PERMISSION_MANAGE, siteRefForVideo)
                && video != null
                && Objects.equals(video.getOwnerId(), userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingCourseGroup> getCourseGroupsForSites(List<String> siteIds, String userId, Instant now, int limitPerSite) {
        Instant effectiveNow = now != null ? now : Instant.now();
        int safeLimit = Math.max(1, Math.min(100, limitPerSite <= 0 ? 10 : limitPerSite));
        List<VideoTrainingCourseGroup> groups = new ArrayList<>();
        if (siteIds == null) {
            return groups;
        }

        for (String siteId : siteIds) {
            if (StringUtils.isBlank(siteId)) {
                continue;
            }

            boolean canManage = canManageLibrary(siteId, userId);
            if (!canManage && !securityService.unlock(userId, PERMISSION_VIEW, siteService.siteReference(siteId))) {
                continue;
            }

            VideoTrainingCourseGroup group = new VideoTrainingCourseGroup();
            group.setSiteId(siteId);
            try {
                group.setSiteTitle(siteService.getSite(siteId).getTitle());
            } catch (Exception e) {
                group.setSiteTitle(siteId);
            }

            if (canManage) {
                group.setTotalVideos(countSiteLibrary(siteId, ""));
                group.setVideos(getSiteLibraryPage(siteId, "", 1, safeLimit));
            } else {
                group.setTotalVideos(countVisibleVideosForUser(siteId, userId, effectiveNow, ""));
                group.setVideos(getVisibleVideosForUserPage(siteId, userId, effectiveNow, "", 1, safeLimit));
            }
            groups.add(group);
        }

        return groups;
    }

    @Override
    public VideoTrainingLessonLink saveLessonLink(VideoTrainingLessonLink lessonLink) {
        if (lessonLink == null || StringUtils.isAnyBlank(lessonLink.getSiteId(), lessonLink.getVideoId(), lessonLink.getLessonPageId())) {
            throw new IllegalArgumentException("Invalid lesson link payload");
        }

        String userId = sessionManager.getCurrentSessionUserId();
        if (!canManageLibrary(lessonLink.getSiteId(), userId)) {
            throw new SecurityException("User cannot manage lesson links for site " + lessonLink.getSiteId());
        }

        VideoTrainingVideo video = getVideoById(lessonLink.getVideoId()).orElseThrow(() -> new IllegalArgumentException("Unknown video"));
        if (!Objects.equals(video.getSiteId(), lessonLink.getSiteId())) {
            throw new IllegalArgumentException("Video does not belong to site");
        }

        lessonLink.setCreatedOn(Instant.now());
        VideoTrainingLessonLink saved = lessonLinkRepository.save(lessonLink);
        registerAudit(saved.getSiteId(), userId, "LESSON_LINK_SAVED", saved.getVideoId(), saved.getLessonPageId());
        return saved;
    }

    @Override
    public void deleteLessonLink(String lessonLinkId) {
        if (StringUtils.isBlank(lessonLinkId)) {
            return;
        }

        VideoTrainingLessonLink link = lessonLinkRepository.findById(lessonLinkId).orElse(null);
        if (link == null) {
            return;
        }

        String userId = sessionManager.getCurrentSessionUserId();
        if (!canManageLibrary(link.getSiteId(), userId)) {
            throw new SecurityException("User cannot manage lesson links for site " + link.getSiteId());
        }

        lessonLinkRepository.delete(link);
        registerAudit(link.getSiteId(), userId, "LESSON_LINK_DELETED", link.getVideoId(), link.getLessonPageId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingLessonLink> getLessonLinksForVideo(String videoId) {
        return lessonLinkRepository.findByVideoIdOrderByCreatedOnDesc(videoId);
    }

    @Override
    public VideoTrainingVideo promoteLessonResource(String siteId, String lessonPageId, String lessonItemId,
            String resourceReference, String title, String description, Long fileSizeBytes) {
        if (StringUtils.isAnyBlank(siteId, lessonPageId, resourceReference, title)) {
            throw new IllegalArgumentException("siteId, lessonPageId, resourceReference and title are required");
        }

        VideoTrainingVideo video = new VideoTrainingVideo();
        video.setSiteId(siteId);
        video.setTitle(StringUtils.trimToEmpty(title));
        video.setDescription(StringUtils.trimToEmpty(description));
        video.setProviderType(VideoProviderType.NATIVE);
        video.setSourceReference(StringUtils.trimToEmpty(resourceReference));
        video.setVisibilityScope(VideoVisibilityScope.LESSON);
        video.setPublicationStatus(VideoPublicationStatus.DRAFT);
        video.setRequiredViewPermission(VideoTrainingConstants.PERMISSION_VIEW);
        video.setFileSizeBytes(fileSizeBytes);

        VideoTrainingVideo saved = saveVideo(video);

        VideoTrainingLessonLink link = new VideoTrainingLessonLink();
        link.setSiteId(siteId);
        link.setVideoId(saved.getId());
        link.setLessonPageId(lessonPageId);
        link.setLessonItemId(StringUtils.trimToNull(lessonItemId));
        saveLessonLink(link);

        String userId = sessionManager.getCurrentSessionUserId();
        registerAudit(siteId, userId, "LESSON_RESOURCE_PROMOTED", saved.getId(), resourceReference);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoTrainingUserVideoPreference> getUserVideoPreference(String siteId, String videoId, String userId) {
        if (StringUtils.isAnyBlank(siteId, videoId, userId) || userVideoPreferenceRepository == null) {
            return Optional.empty();
        }
        return userVideoPreferenceRepository.findBySiteIdAndUserIdAndVideoId(siteId, userId, videoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, VideoTrainingUserVideoPreference> getUserVideoPreferences(String siteId, String userId, List<String> videoIds) {
        Map<String, VideoTrainingUserVideoPreference> byVideoId = new HashMap<>();
        if (StringUtils.isAnyBlank(siteId, userId) || videoIds == null || videoIds.isEmpty() || userVideoPreferenceRepository == null) {
            return byVideoId;
        }

        for (VideoTrainingUserVideoPreference preference : userVideoPreferenceRepository.findBySiteIdAndUserIdAndVideoIds(siteId, userId, videoIds)) {
            byVideoId.put(preference.getVideoId(), preference);
        }
        return byVideoId;
    }

    @Override
    public void setUserFavorite(String siteId, String videoId, String userId, boolean favorite) {
        updateUserPreference(siteId, videoId, userId, favorite, null);
    }

    @Override
    public void setUserWatchLater(String siteId, String videoId, String userId, boolean watchLater) {
        updateUserPreference(siteId, videoId, userId, null, watchLater);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getUserFavoriteVideos(String siteId, String userId, Instant now) {
        return getPreferredVideos(siteId, userId, now, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoTrainingVideo> getUserWatchLaterVideos(String siteId, String userId, Instant now) {
        return getPreferredVideos(siteId, userId, now, false);
    }

    private void updateUserPreference(String siteId, String videoId, String userId, Boolean favorite, Boolean watchLater) {
        if (StringUtils.isAnyBlank(siteId, videoId, userId)) {
            throw new IllegalArgumentException("siteId, videoId and userId are required");
        }
        if (userVideoPreferenceRepository == null) {
            throw new IllegalStateException("userVideoPreferenceRepository is not available");
        }

        VideoTrainingVideo video = getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown video"));
        if (!Objects.equals(siteId, video.getSiteId())) {
            throw new IllegalArgumentException("Video does not belong to site");
        }
        if (!canViewVideo(video, userId, Instant.now())) {
            throw new SecurityException("User cannot set preferences for this video");
        }

        VideoTrainingUserVideoPreference preference = userVideoPreferenceRepository
                .findBySiteIdAndUserIdAndVideoId(siteId, userId, videoId)
                .orElseGet(VideoTrainingUserVideoPreference::new);

        if (preference.getId() == null) {
            preference.setSiteId(siteId);
            preference.setUserId(userId);
            preference.setVideoId(videoId);
            preference.setCreatedOn(Instant.now());
        }

        if (favorite != null) {
            preference.setFavorite(favorite);
        }
        if (watchLater != null) {
            preference.setWatchLater(watchLater);
        }
        preference.setModifiedOn(Instant.now());

        if (!preference.isFavorite() && !preference.isWatchLater()) {
            if (preference.getId() != null) {
                userVideoPreferenceRepository.delete(preference);
            }
            return;
        }

        userVideoPreferenceRepository.save(preference);
        registerAudit(siteId, userId, "USER_VIDEO_PREFERENCE_UPDATED", videoId, "favorite=" + preference.isFavorite() + ",watchLater=" + preference.isWatchLater());
    }

    private List<VideoTrainingVideo> getPreferredVideos(String siteId, String userId, Instant now, boolean favoritesOnly) {
        if (StringUtils.isAnyBlank(siteId, userId) || userVideoPreferenceRepository == null) {
            return Collections.emptyList();
        }

        Instant effectiveNow = now != null ? now : Instant.now();
        List<VideoTrainingUserVideoPreference> preferences = favoritesOnly
                ? userVideoPreferenceRepository.findBySiteIdAndUserIdAndFavoriteTrueOrderByModifiedOnDesc(siteId, userId)
                : userVideoPreferenceRepository.findBySiteIdAndUserIdAndWatchLaterTrueOrderByModifiedOnDesc(siteId, userId);

        List<VideoTrainingVideo> videos = new ArrayList<>();
        for (VideoTrainingUserVideoPreference preference : preferences) {
            VideoTrainingVideo video = getVideoById(preference.getVideoId()).orElse(null);
            if (video == null) {
                continue;
            }
            if (!Objects.equals(video.getSiteId(), siteId)) {
                continue;
            }
            if (canViewVideo(video, userId, effectiveNow)) {
                videos.add(video);
            }
        }

        return videos;
    }

    private int sanitizePage(int page) {
        return Math.max(page, 1);
    }

    private int sanitizePageSize(int size) {
        int safe = size <= 0 ? 24 : size;
        return Math.max(1, Math.min(100, safe));
    }

    private String normalizeSearchText(String searchText) {
        return StringUtils.trimToEmpty(searchText);
    }

    private boolean isPublishedForEndUsers(VideoTrainingVideo video) {
        VideoPublicationStatus status = video.getPublicationStatus();
        return status == VideoPublicationStatus.PUBLISHED;
    }

    private long countUniqueVideos(VideoTrainingCategory category) {
        if (category == null) {
            return 0L;
        }

        Set<String> videoIds = new HashSet<>();
        collectVideoIds(category, videoIds);
        return videoIds.size();
    }

    private void collectVideoIds(VideoTrainingCategory category, Set<String> videoIds) {
        if (category == null) {
            return;
        }

        if (category.getVideos() != null) {
            for (VideoTrainingVideo video : category.getVideos()) {
                if (video != null && StringUtils.isNotBlank(video.getId())) {
                    videoIds.add(video.getId());
                }
            }
        }

        if (category.getChildren() != null) {
            for (VideoTrainingCategory child : category.getChildren()) {
                collectVideoIds(child, videoIds);
            }
        }
    }

    private boolean hasChildCategories(String siteId, String categoryId) {
        if (StringUtils.isAnyBlank(siteId, categoryId)) {
            return false;
        }

        List<VideoTrainingCategory> allCategories = categoryRepository.findBySiteIdOrderBySortOrderAscNameAsc(siteId, 0, 0);
        for (VideoTrainingCategory candidate : allCategories) {
            if (Objects.equals(categoryId, candidate.getParentCategoryId())) {
                return true;
            }
        }
        return false;
    }

    private void validateCategoryHierarchy(VideoTrainingCategory category, VideoTrainingCategory existing) {
        if (StringUtils.isBlank(category.getSiteId())) {
            return;
        }

        String parentCategoryId = StringUtils.trimToNull(category.getParentCategoryId());
        if (parentCategoryId == null) {
            category.setParentCategoryId(null);
            return;
        }

        VideoTrainingCategory parent = categoryRepository.findById(parentCategoryId).orElse(null);
        if (parent == null || !Objects.equals(parent.getSiteId(), category.getSiteId())) {
            throw new IllegalArgumentException("Invalid parent category");
        }

        if (StringUtils.isNotBlank(parent.getParentCategoryId())) {
            throw new IllegalArgumentException("Categories can only have two levels");
        }

        if (existing != null
                && !Objects.equals(existing.getParentCategoryId(), parentCategoryId)
                && hasChildCategories(existing.getSiteId(), existing.getId())
                && StringUtils.isBlank(existing.getParentCategoryId())
                && StringUtils.isNotBlank(parentCategoryId)) {
            throw new IllegalArgumentException("Categories with subcategories cannot be moved below a parent");
        }
    }

    private VideoTrainingCategory filterCategoryBranch(VideoTrainingCategory category, String searchText) {
        if (category == null) {
            return null;
        }

        boolean matches = matchesCategorySearch(category, searchText);
        List<VideoTrainingCategory> matchingChildren = new ArrayList<>();
        for (VideoTrainingCategory child : category.getChildren()) {
            VideoTrainingCategory filteredChild = filterCategoryBranch(child, searchText);
            if (filteredChild != null) {
                matchingChildren.add(filteredChild);
            }
        }

        if (!matches && matchingChildren.isEmpty()) {
            return null;
        }

        VideoTrainingCategory copy = copyCategory(category);
        if (matches) {
            copy.setChildren(copyCategoryList(category.getChildren()));
        } else {
            copy.setChildren(matchingChildren);
        }
        copy.setHasChildren(!copy.getChildren().isEmpty());
        copy.setVideoCount(countUniqueVideos(copy));
        return copy;
    }

    private boolean matchesCategorySearch(VideoTrainingCategory category, String searchText) {
        if (StringUtils.isBlank(searchText)) {
            return true;
        }

        String normalized = StringUtils.lowerCase(searchText);
        return StringUtils.contains(StringUtils.lowerCase(StringUtils.defaultString(category.getName())), normalized);
    }

    private VideoTrainingCategory copyCategory(VideoTrainingCategory source) {
        VideoTrainingCategory copy = new VideoTrainingCategory();
        copy.setId(source.getId());
        copy.setSiteId(source.getSiteId());
        copy.setName(source.getName());
        copy.setParentCategoryId(source.getParentCategoryId());
        copy.setSortOrder(source.getSortOrder());
        copy.setCreatedOn(source.getCreatedOn());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setModifiedOn(source.getModifiedOn());
        copy.setModifiedBy(source.getModifiedBy());
        copy.setVideoCount(source.getVideoCount());
        copy.setHasChildren(source.isHasChildren());
        copy.setVideos(source.getVideos());
        copy.setChildren(new ArrayList<>());
        return copy;
    }

    private List<VideoTrainingCategory> copyCategoryList(List<VideoTrainingCategory> categories) {
        List<VideoTrainingCategory> copies = new ArrayList<>();
        if (categories == null) {
            return copies;
        }

        for (VideoTrainingCategory category : categories) {
            VideoTrainingCategory copy = copyCategory(category);
            copy.setChildren(new ArrayList<>());
            copies.add(copy);
        }
        return copies;
    }

    private VideoPublicationStatus normalizePublicationStatus(VideoPublicationStatus status) {
        return status != null ? status : VideoPublicationStatus.DRAFT;
    }

    @Override
    public VideoPublicationStatus[] getValidPublicationStatusTransitions(VideoPublicationStatus currentStatus,
            VideoVisibilityScope visibilityScope) {
        VideoPublicationStatus normalized = normalizePublicationStatus(currentStatus);
        boolean moderationRequired = isModerationRequired(visibilityScope);
        List<VideoPublicationStatus> validTargets = new ArrayList<>();

        switch (normalized) {
            case DRAFT:
                if (moderationRequired) {
                    validTargets.add(VideoPublicationStatus.DRAFT);
                    validTargets.add(VideoPublicationStatus.PENDING_APPROVAL);
                } else {
                    validTargets.add(VideoPublicationStatus.DRAFT);
                    validTargets.add(VideoPublicationStatus.PUBLISHED);
                }
                break;
            case PENDING_APPROVAL:
                validTargets.add(VideoPublicationStatus.PENDING_APPROVAL);
                validTargets.add(VideoPublicationStatus.PUBLISHED);
                validTargets.add(VideoPublicationStatus.DRAFT);
                break;
            case PUBLISHED:
                validTargets.add(VideoPublicationStatus.PUBLISHED);
                validTargets.add(VideoPublicationStatus.WITHDRAWN);
                validTargets.add(VideoPublicationStatus.ARCHIVED);
                break;
            case WITHDRAWN:
                if (moderationRequired) {
                    validTargets.add(VideoPublicationStatus.WITHDRAWN);
                    validTargets.add(VideoPublicationStatus.PENDING_APPROVAL);
                } else {
                    validTargets.add(VideoPublicationStatus.WITHDRAWN);
                    validTargets.add(VideoPublicationStatus.PUBLISHED);
                }
                break;
            case ARCHIVED:
                validTargets.add(VideoPublicationStatus.ARCHIVED);
                validTargets.add(VideoPublicationStatus.DRAFT);
                break;
            default:
                break;
        }

        return validTargets.toArray(VideoPublicationStatus[]::new);
    }

    private void validatePublicationStatusTransition(VideoPublicationStatus currentStatus,
            VideoPublicationStatus targetStatus,
            VideoVisibilityScope visibilityScope) {
        if (currentStatus == targetStatus) {
            return;
        }

        boolean moderationRequired = isModerationRequired(visibilityScope);
        boolean valid;

        switch (currentStatus) {
            case DRAFT:
                valid = moderationRequired
                        ? targetStatus == VideoPublicationStatus.PENDING_APPROVAL
                        : targetStatus == VideoPublicationStatus.PUBLISHED;
                break;
            case PENDING_APPROVAL:
                valid = targetStatus == VideoPublicationStatus.PUBLISHED
                        || targetStatus == VideoPublicationStatus.DRAFT;
                break;
            case PUBLISHED:
                valid = targetStatus == VideoPublicationStatus.WITHDRAWN
                        || targetStatus == VideoPublicationStatus.ARCHIVED;
                break;
            case WITHDRAWN:
                valid = moderationRequired
                        ? targetStatus == VideoPublicationStatus.PENDING_APPROVAL
                        : targetStatus == VideoPublicationStatus.PUBLISHED;
                break;
            case ARCHIVED:
                valid = targetStatus == VideoPublicationStatus.DRAFT;
                break;
            default:
                valid = false;
        }

        if (!valid) {
            throw new IllegalArgumentException("Invalid publication status transition from "
                    + currentStatus + " to " + targetStatus);
        }
    }

    private boolean isModerationRequired(VideoVisibilityScope visibilityScope) {
        if (serverConfigurationService == null) {
            return VideoTrainingConstants.DEFAULT_MODERATION_ENABLED;
        }
        return serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED);
    }

    private String resolveBaseFolder() {
        if (serverConfigurationService == null) {
            return VideoTrainingConstants.DEFAULT_BASE_FOLDER;
        }
        return serverConfigurationService.getString(VideoTrainingConstants.BASE_FOLDER_PROPERTY, VideoTrainingConstants.DEFAULT_BASE_FOLDER);
    }

    private boolean isCatalogVisibleScope(VideoTrainingVideo video) {
        VideoVisibilityScope scope = video.getVisibilityScope();
        return scope == null || scope != VideoVisibilityScope.LESSON;
    }

    private long visibilityBucket(Instant now) {
        return now.toEpochMilli() / 60_000L;
    }

    private void invalidateListCaches() {
        countCache.clear();
        firstPageCache.clear();
    }

    private void initializeDistributedCaches() {
        if (memoryService == null) {
            return;
        }

        SimpleConfiguration<String, Long> countConfig = new SimpleConfiguration<>(20000, 30, 30);
        SimpleConfiguration<String, List<String>> firstPageConfig = new SimpleConfiguration<>(20000, 30, 30);

        try {
            distributedCountCache = memoryService.createCache(VideoTrainingConstants.COUNT_CACHE_NAME, countConfig);
        } catch (Exception e) {
            distributedCountCache = memoryService.getCache(VideoTrainingConstants.COUNT_CACHE_NAME);
        }

        try {
            distributedFirstPageCache = memoryService.createCache(VideoTrainingConstants.FIRST_PAGE_CACHE_NAME, firstPageConfig);
        } catch (Exception e) {
            distributedFirstPageCache = memoryService.getCache(VideoTrainingConstants.FIRST_PAGE_CACHE_NAME);
        }
    }

    private Long readCachedCount(ListCacheKey key) {
        String cacheKey = key.asCacheKey();
        if (distributedCountCache != null) {
            Long cached = distributedCountCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        CacheEntry<Long> entry = countCache.get(key);
        if (entry == null || entry.isExpired()) {
            countCache.remove(key);
            return null;
        }
        return entry.value();
    }

    private void writeCachedCount(ListCacheKey key, long value) {
        String cacheKey = key.asCacheKey();
        if (distributedCountCache != null) {
            distributedCountCache.put(cacheKey, value);
        }
        countCache.put(key, new CacheEntry<>(value));
    }

    private List<VideoTrainingVideo> readCachedList(ListCacheKey key) {
        String cacheKey = key.asCacheKey();
        if (distributedFirstPageCache != null) {
            List<String> ids = distributedFirstPageCache.get(cacheKey);
            if (ids != null) {
                return restoreVideosFromIds(ids);
            }
        }

        CacheEntry<List<String>> entry = firstPageCache.get(key);
        if (entry == null || entry.isExpired()) {
            firstPageCache.remove(key);
            return null;
        }
        return restoreVideosFromIds(entry.value());
    }

    private void writeCachedList(ListCacheKey key, List<VideoTrainingVideo> value) {
        String cacheKey = key.asCacheKey();
        List<String> ids = value.stream().map(VideoTrainingVideo::getId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toList());
        List<String> immutableIds = Collections.unmodifiableList(new ArrayList<>(ids));
        if (distributedFirstPageCache != null) {
            distributedFirstPageCache.put(cacheKey, immutableIds);
        }
        firstPageCache.put(key, new CacheEntry<>(immutableIds));
    }

    private List<VideoTrainingVideo> restoreVideosFromIds(List<String> ids) {
        List<VideoTrainingVideo> videos = new ArrayList<>();
        for (String id : ids) {
            videoRepository.findById(id).ifPresent(videos::add);
        }
        return videos;
    }

    private record CacheEntry<T>(T value, long createdAtMillis) {

        private CacheEntry(T value) {
            this(value, System.currentTimeMillis());
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - createdAtMillis > LIST_CACHE_TTL_MILLIS;
        }
    }

    private record ListCacheKey(String mode, String siteId, String userId, String searchText, int size, long bucket) implements Serializable {

        private String asCacheKey() {
            return mode + "|" + StringUtils.defaultString(siteId) + "|" + StringUtils.defaultString(userId)
                    + "|" + StringUtils.defaultString(searchText) + "|" + size + "|" + bucket;
        }

        private static ListCacheKey countManage(String siteId, String searchText) {
            return new ListCacheKey("count-manage", siteId, "", searchText, 0, 0L);
        }

        private static ListCacheKey firstPageManage(String siteId, String searchText, int size) {
            return new ListCacheKey("first-manage", siteId, "", searchText, size, 0L);
        }

        private static ListCacheKey countVisible(String siteId, String userId, String searchText, long bucket) {
            return new ListCacheKey("count-visible", siteId, StringUtils.defaultString(userId), searchText, 0, bucket);
        }

        private static ListCacheKey firstPageVisible(String siteId, String userId, String searchText, int size, long bucket) {
            return new ListCacheKey("first-visible", siteId, StringUtils.defaultString(userId), searchText, size, bucket);
        }
    }
}
