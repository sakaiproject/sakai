package org.sakaiproject.webapi.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.videotraining.api.model.PagedResponse;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsSummary;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.model.VideoTrainingCourseGroup;
import org.sakaiproject.videotraining.api.model.VideoTrainingLessonLink;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.webapi.beans.VideoCategoryRestBean;
import org.sakaiproject.webapi.beans.VideoTrainingAnalyticsRestBean;
import org.sakaiproject.webapi.beans.VideoTrainingRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
public class VideoTrainingController extends AbstractSakaiApiController {

    @Autowired
    private VideoTrainingService videoTrainingService;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 25;

    @GetMapping(value = {"/videos"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<VideoTrainingRestBean> getGlobalVideos(
        @RequestParam(name = "q", required = false) String q,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        // At least the user is logged in, we won't filter by site or anything
        // just return all videos the user has access to across the entire system.
        checkSakaiSession();

        Session session = checkSakaiSession();
        String userId = session.getUserId();

        String query = StringUtils.trimToEmpty(q);
        int safeSize = normalizePageSize(size);

        Long totalCount = videoTrainingService.countGlobalVideosForUser(userId, query);

        int safePage = normalizePage(page, safeSize, totalCount);

        List<VideoTrainingVideo> paginatedVideoList =
            videoTrainingService.getGlobalVideosForUser(userId, query, safePage, safeSize);

        List<VideoTrainingRestBean> beans = paginatedVideoList.stream()
            .map(video -> new VideoTrainingRestBean(video))
            .collect(Collectors.toList());

        return new PagedResponse<>(beans, totalCount, safePage, safeSize);
    }

    @GetMapping(value = {"/sites/{siteId}/videos"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<VideoTrainingRestBean> getSiteVideos(
        @PathVariable(required = true) String siteId,
        @RequestParam(name = "q", required = false) String q,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "view", defaultValue = "viewable") String view
    ) {
        Session session = checkSakaiSession();
        String userId = session.getUserId();

        String query = StringUtils.trimToEmpty(q);
        int safeSize = normalizePageSize(size);

        if (view != null && !view.equals("manageable") && !view.equals("viewable")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid view parameter");
        }

        Long totalCount = 0L;
        List<VideoTrainingVideo> paginatedVideoList = new ArrayList<>();

        int safePage = normalizePage(page, safeSize, totalCount);

        switch (view) {
            case "manageable":
                totalCount = videoTrainingService.countSiteVideosForUser(siteId, userId, query);
                paginatedVideoList =
                    videoTrainingService.getSiteVideosForUserPage(siteId, userId, query, safePage, safeSize);
                break;
            case "viewable":
                totalCount = videoTrainingService.countSiteViewableVideosForUser(
                    siteId, userId, query
                );
                paginatedVideoList = videoTrainingService.getSiteViewableVideosForUserPage(siteId, userId, query, safePage, safeSize);
                break;
        }

        List<VideoTrainingRestBean> beans = paginatedVideoList.stream()
            .map(video -> new VideoTrainingRestBean(video))
            .collect(Collectors.toList());

        return new PagedResponse<>(beans, totalCount, safePage, safeSize);
    }

    @GetMapping(value = {"/videos/{videoId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public VideoTrainingRestBean getVideoDetails(@PathVariable("videoId") String videoId) {

        Session session = checkSakaiSession();

        Optional<VideoTrainingVideo> optionalVideo = videoTrainingService.getVideoById(videoId);
        VideoTrainingVideo video = optionalVideo.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        String userId = session.getUserId();
        if (!videoTrainingService.canViewVideo(video, userId, Instant.now())
                && !videoTrainingService.canManageLibrary(video.getSiteId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access this video");
        }

        return new VideoTrainingRestBean(video);
    }

    @GetMapping(value = {"/videos/{videoId}/categories"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VideoCategoryRestBean> getVideoCategories(@PathVariable("videoId") String videoId) {
        Session session = checkSakaiSession();

        Optional<VideoTrainingVideo> optionalVideo = videoTrainingService.getVideoById(videoId);
        VideoTrainingVideo video = optionalVideo.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        String userId = session.getUserId();
        if (!videoTrainingService.canViewVideo(video, userId, Instant.now())
                && !videoTrainingService.canManageLibrary(video.getSiteId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access this video");
        }

        return video.getCategories()
            .stream()
            .map(VideoCategoryRestBean::new)
            .toList();
    }

    @GetMapping(value = "/sites/{siteId}/videos/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<VideoTrainingAnalyticsRestBean>> getAnalytics(@PathVariable("siteId") String siteId) {

        Session session = checkSakaiSession();
        checkSite(siteId);

        String userId = session.getUserId();
        if (!videoTrainingService.canViewAnalytics(siteId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access analytics");
        }

        List<VideoTrainingAnalyticsSummary> summaries = videoTrainingService.getSiteAnalyticsSummary(siteId);
        List<VideoTrainingAnalyticsRestBean> beans = summaries.stream().map(this::toAnalyticsRestBean).collect(Collectors.toList());
        return Map.of("analytics", beans);
    }

    @PutMapping(value = {"/videos/{videoId}"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VideoTrainingRestBean updateVideoMetadata(@PathVariable("videoId") String videoId,
            @RequestBody VideoTrainingUpdateRequest request) {

        Session session = checkSakaiSession();

        VideoTrainingVideo video = videoTrainingService.getVideoById(videoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        boolean canManage = videoTrainingService.canManageVideo(videoId, session.getUserId());
        if (!canManage) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage this site's video library");
        }

        applyUpdateRequest(video, request);

        if (!isValidVisibilityWindow(video.getReleaseDate(), video.getRetractDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retract date must be later than release date");
        }

        VideoTrainingVideo saved;
        try {
            saved = videoTrainingService.saveVideo(video);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid update payload", e);
        }

        if (request != null && request.getCategoryIds() != null) {
            videoTrainingService.setVideoCategoryIds(saved.getId(), request.getCategoryIds());
            saved = videoTrainingService.getVideoById(saved.getId()).orElse(saved);
        }
        return new VideoTrainingRestBean(saved);
    }

    @GetMapping(value = "/sites/{siteId}/videos/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<VideoCategoryRestBean> getCategories(
        @PathVariable("siteId") String siteId,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Session session = checkSakaiSession();
        String userId = session.getUserId();
        checkSite(siteId);

        int safeSize = normalizePageSize(size);
        Long totalCount = videoTrainingService.countCategoriesForSite(siteId, userId);
        int safePage = normalizePage(page, safeSize, totalCount);

        boolean hasAccess = siteService.allowAccessSite(siteId);

        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access this site's video categories");
        }

        List<VideoTrainingCategory> categories = videoTrainingService.getCategories(siteId, safePage, safeSize);
        List<VideoCategoryRestBean> beans = categories.stream()
            .map(category -> new VideoCategoryRestBean(category))
            .collect(Collectors.toList());

        return new PagedResponse<>(beans, totalCount, safePage, safeSize);
    }

    @GetMapping(value = "/sites/{siteId}/videos/categories/tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<VideoCategoryRestBean> getCategoryTree(
        @PathVariable("siteId") String siteId,
        @RequestParam(name = "q", required = false) String q,
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Session session = checkSakaiSession();
        String userId = session.getUserId();
        checkSite(siteId);

        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage this site's video categories");
        }

        String query = StringUtils.trimToEmpty(q);
        int safeSize = normalizePageSize(size);
        Long totalCount = videoTrainingService.countTopLevelCategoriesForSite(siteId, userId, query);
        int safePage = normalizePage(page, safeSize, totalCount);

        List<VideoTrainingCategory> categories = videoTrainingService.getCategoryTree(siteId, query, safePage, safeSize);
        List<VideoCategoryRestBean> beans = categories.stream()
            .map(VideoCategoryRestBean::new)
            .collect(Collectors.toList());

        return new PagedResponse<>(beans, totalCount, safePage, safeSize);
    }

    @PostMapping(value = "/sites/{siteId}/videos/categories",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VideoCategoryRestBean saveCategory(@PathVariable("siteId") String siteId,
            @RequestBody VideoTrainingCategoryRequest request) {
        Session session = checkSakaiSession();
        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage this site's video taxonomy");
        }

        VideoTrainingCategory newCategory = new VideoTrainingCategory(
            siteId, request.getName(), request.getParentCategoryId(), request.getSortOrder());

        try {
            return new VideoCategoryRestBean(videoTrainingService.saveCategory(newCategory));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category payload", e);
        }
    }

    @PutMapping("/videos/categories/{categoryId}")
    public VideoCategoryRestBean updateVideoCategory(
            @PathVariable("categoryId") String categoryId,
            @RequestBody VideoTrainingCategoryRequest request) {

        Session session = checkSakaiSession();

        if (request == null || StringUtils.isBlank(categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }

        VideoTrainingCategory category = videoTrainingService.getCategoryById(categoryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category not found with id " + categoryId));

        String siteId = category.getSiteId();

        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage this site's video taxonomy");
        }

        category.setName(request.getName() != null ? request.getName() : category.getName());

        String requestedParentCategoryId = StringUtils.trimToNull(request.getParentCategoryId());
        if (requestedParentCategoryId != null) {
            Optional<VideoTrainingCategory> parentCategory = videoTrainingService.getCategoryById(requestedParentCategoryId);
            if (parentCategory.isPresent() && parentCategory.get().getSiteId().equals(siteId)) {
                category.setParentCategoryId(requestedParentCategoryId);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parentCategoryId");
            }
        } else {
            category.setParentCategoryId(null);
        }

        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder());

        try {
            return new VideoCategoryRestBean(videoTrainingService.saveCategory(category));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category payload", e);
        }
    }

    @PatchMapping("/videos/{videoId}/visibility")
    public VideoTrainingRestBean changeVideoVisibility(
            @PathVariable("videoId") String videoId,
            @RequestParam("visibilityScope") VideoVisibilityScope visibilityScope) {
        checkSakaiSession();

        VideoTrainingVideo updatedVideo = videoTrainingService.updateVideoVisibility(videoId, visibilityScope);

        return new VideoTrainingRestBean(updatedVideo);
    }

    @PatchMapping("/videos/{videoId}/status")
    public VideoTrainingRestBean changeVideoStatus(
            @PathVariable("videoId") String videoId,
            @RequestParam("status") VideoPublicationStatus status) {
        checkSakaiSession();

        VideoTrainingVideo updatedVideo = videoTrainingService.updateVideoStatus(videoId, status);

        return new VideoTrainingRestBean(updatedVideo);
    }

    @PatchMapping("/videos/{videoId}/schedule")
    public VideoTrainingRestBean scheduleVideo(
            @PathVariable("videoId") String videoId,
            @RequestParam(name = "releaseDateEpochMs", required = false) Long releaseDateEpochMs,
            @RequestParam(name = "retractDateEpochMs", required = false) Long retractDateEpochMs) {
        checkSakaiSession();

        Instant releaseDate = releaseDateEpochMs != null ? Instant.ofEpochMilli(releaseDateEpochMs) : null;
        Instant retractDate = retractDateEpochMs != null ? Instant.ofEpochMilli(retractDateEpochMs) : null;

        VideoTrainingVideo updatedVideo = videoTrainingService.updateVideoSchedule(videoId, releaseDate, retractDate);

        return new VideoTrainingRestBean(updatedVideo);
    }

    @DeleteMapping(value = "/videos/categories/{categoryId}")
    public Map<String, Object> deleteCategory(
            @PathVariable("categoryId") String categoryId) {

        Session session = checkSakaiSession();

        try {
            videoTrainingService.deleteCategory(categoryId);
            return Map.of("deleted", true);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (SecurityException e) {
            log.warn("User {} cannot delete category {}",
                    session.getUserId(), categoryId);

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping(value = "/sites/{siteId}/videos/quota", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getQuota(@PathVariable("siteId") String siteId) {
        Session session = checkSakaiSession();
        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage this site's video quota");
        }

        Long maxBytes = videoTrainingService.getSiteStorageQuotaBytes(siteId);
        long usedBytes = videoTrainingService.getSiteStorageUsageBytes(siteId);
        return Map.of("siteId", siteId, "maxBytes", maxBytes, "usedBytes", usedBytes);
    }

    @PostMapping(value = "/lessons/{siteId}/videos/{videoId}/links",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VideoTrainingLessonLink saveLessonLink(@PathVariable("siteId") String siteId,
            @PathVariable("videoId") String videoId,
            @RequestBody LessonLinkRequest request) {
        Session session = checkSakaiSession();
        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage lesson links");
        }
        if (request == null || StringUtils.isBlank(request.getLessonPageId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lessonPageId is required");
        }

        VideoTrainingLessonLink link = new VideoTrainingLessonLink();
        link.setSiteId(siteId);
        link.setVideoId(videoId);
        link.setLessonPageId(StringUtils.trimToEmpty(request.getLessonPageId()));
        link.setLessonItemId(StringUtils.trimToNull(request.getLessonItemId()));
        return videoTrainingService.saveLessonLink(link);
    }

    @PostMapping(value = "/lessons/{siteId}/lesson-links/{lessonLinkId}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> deleteLessonLink(@PathVariable("siteId") String siteId,
            @PathVariable("lessonLinkId") String lessonLinkId) {
        Session session = checkSakaiSession();
        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot manage lesson links");
        }

        videoTrainingService.deleteLessonLink(lessonLinkId);
        return Map.of("deleted", true);
    }

    @PostMapping(value = "/lessons/{siteId}/promote-resource",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VideoTrainingRestBean promoteLessonResource(@PathVariable("siteId") String siteId,
            @RequestBody LessonPromoteRequest request) {
        Session session = checkSakaiSession();
        checkSite(siteId);
        if (!videoTrainingService.canManageLibrary(siteId, session.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot promote lesson resources");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload is required");
        }

        VideoTrainingVideo promoted = videoTrainingService.promoteLessonResource(siteId,
                request.getLessonPageId(),
                request.getLessonItemId(),
                request.getResourceReference(),
                request.getTitle(),
                request.getDescription(),
                request.getFileSizeBytes());

        return new VideoTrainingRestBean(promoted);
    }

    private VideoTrainingAnalyticsRestBean toAnalyticsRestBean(VideoTrainingAnalyticsSummary summary) {
        VideoTrainingAnalyticsRestBean bean = new VideoTrainingAnalyticsRestBean();
        bean.setVideoId(summary.getVideoId());
        bean.setViewCount(summary.getViewCount());
        bean.setUniqueViewerCount(summary.getUniqueViewerCount());
        return bean;
    }

    private int normalizePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(MAX_PAGE_SIZE, requestedSize);
    }

    private int normalizePage(int requestedPage, int pageSize, long totalCount) {
        int safePage = Math.max(requestedPage, 1);
        if (totalCount <= 0) {
            return 1;
        }

        int totalPages = (int) Math.max(1, Math.ceil((double) totalCount / pageSize));
        return Math.min(safePage, totalPages);
    }

    private void applyUpdateRequest(VideoTrainingVideo video, VideoTrainingUpdateRequest request) {
        if (request == null) {
            return;
        }

        if (request.getTitle() != null && !video.isInheritTitleMetadata()) {
            String title = StringUtils.trimToEmpty(request.getTitle());
            if (StringUtils.isBlank(title)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be blank");
            }
            video.setTitle(title);
        }

        if (request.getDescription() != null && !video.isInheritDescriptionMetadata()) {
            video.setDescription(StringUtils.trimToEmpty(request.getDescription()));
        }

        if (request.getVisibilityScope() != null) {
            try {
                video.setVisibilityScope(VideoVisibilityScope.valueOf(request.getVisibilityScope()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid visibilityScope", e);
            }
        }

        if (request.getPublicationStatus() != null) {
            try {
                video.setPublicationStatus(VideoPublicationStatus.valueOf(request.getPublicationStatus()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid publicationStatus", e);
            }
        }

        if (Boolean.TRUE.equals(request.getClearReleaseDate())) {
            video.setReleaseDate(null);
        } else if (request.getReleaseDateEpochMs() != null) {
            video.setReleaseDate(Instant.ofEpochMilli(request.getReleaseDateEpochMs()));
        }

        if (Boolean.TRUE.equals(request.getClearRetractDate())) {
            video.setRetractDate(null);
        } else if (request.getRetractDateEpochMs() != null) {
            video.setRetractDate(Instant.ofEpochMilli(request.getRetractDateEpochMs()));
        }
    }

    private boolean isValidVisibilityWindow(Instant releaseDate, Instant retractDate) {
        return releaseDate == null || retractDate == null || releaseDate.isBefore(retractDate);
    }

    public static class VideoTrainingUpdateRequest {

        private String title;
        private String description;
        private String visibilityScope;
        private String publicationStatus;
        private Long releaseDateEpochMs;
        private Long retractDateEpochMs;
        private Boolean clearReleaseDate;
        private Boolean clearRetractDate;
        private Long fileSizeBytes;
        private List<String> categoryIds;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVisibilityScope() {
            return visibilityScope;
        }

        public void setVisibilityScope(String visibilityScope) {
            this.visibilityScope = visibilityScope;
        }

        public String getPublicationStatus() {
            return publicationStatus;
        }

        public void setPublicationStatus(String publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        public Long getReleaseDateEpochMs() {
            return releaseDateEpochMs;
        }

        public void setReleaseDateEpochMs(Long releaseDateEpochMs) {
            this.releaseDateEpochMs = releaseDateEpochMs;
        }

        public Long getRetractDateEpochMs() {
            return retractDateEpochMs;
        }

        public void setRetractDateEpochMs(Long retractDateEpochMs) {
            this.retractDateEpochMs = retractDateEpochMs;
        }

        public Boolean getClearReleaseDate() {
            return clearReleaseDate;
        }

        public void setClearReleaseDate(Boolean clearReleaseDate) {
            this.clearReleaseDate = clearReleaseDate;
        }

        public Boolean getClearRetractDate() {
            return clearRetractDate;
        }

        public void setClearRetractDate(Boolean clearRetractDate) {
            this.clearRetractDate = clearRetractDate;
        }

        public Long getFileSizeBytes() {
            return fileSizeBytes;
        }

        public void setFileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
        }

        public List<String> getCategoryIds() {
            return categoryIds;
        }

        public void setCategoryIds(List<String> categoryIds) {
            this.categoryIds = categoryIds;
        }
    }

    public static class VideoTrainingCategoryRequest {

        private String name;
        private String parentCategoryId;
        private Integer sortOrder;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParentCategoryId() {
            return parentCategoryId;
        }

        public void setParentCategoryId(String parentCategoryId) {
            this.parentCategoryId = parentCategoryId;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public static class LessonLinkRequest {

        private String lessonPageId;
        private String lessonItemId;

        public String getLessonPageId() {
            return lessonPageId;
        }

        public void setLessonPageId(String lessonPageId) {
            this.lessonPageId = lessonPageId;
        }

        public String getLessonItemId() {
            return lessonItemId;
        }

        public void setLessonItemId(String lessonItemId) {
            this.lessonItemId = lessonItemId;
        }
    }

    public static class LessonPromoteRequest {

        private String lessonPageId;
        private String lessonItemId;
        private String resourceReference;
        private String title;
        private String description;
        private Long fileSizeBytes;

        public String getLessonPageId() {
            return lessonPageId;
        }

        public void setLessonPageId(String lessonPageId) {
            this.lessonPageId = lessonPageId;
        }

        public String getLessonItemId() {
            return lessonItemId;
        }

        public void setLessonItemId(String lessonItemId) {
            this.lessonItemId = lessonItemId;
        }

        public String getResourceReference() {
            return resourceReference;
        }

        public void setResourceReference(String resourceReference) {
            this.resourceReference = resourceReference;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getFileSizeBytes() {
            return fileSizeBytes;
        }

        public void setFileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
        }
    }
}
