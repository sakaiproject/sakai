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

package org.sakaiproject.videotraining.tool.mvc;

import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.PagedResponse;
import org.sakaiproject.videotraining.api.model.SiteVideoResourceOption;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategoryDeleteImpact;
import org.sakaiproject.videotraining.api.model.VideoTrainingCategoryOrderUpdate;
import org.sakaiproject.videotraining.api.model.VideoTrainingAnalyticsSummary;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJob;
import org.sakaiproject.videotraining.api.model.VideoTrainingProcessJobStatus;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideoView;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalMetadataService;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.service.ProcessJobService;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;
import org.sakaiproject.videotraining.api.service.VideoTrainingUploadService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping
@Slf4j
public class VideoTrainingController {

    private static final Map<String, String> SORT_FIELD_BY_COLUMN = Map.of(
            "title", "title",
            "scope", "visibilityScope",
            "status", "publicationStatus",
            "release", "releaseDate",
            "retract", "retractDate",
            "modified", "modifiedOn");
    private final MessageSource messageSource;
    private final SessionManager sessionManager;
    private final SiteService siteService;
    private final ToolManager toolManager;
    private final UserTimeService userTimeService;
    private final VideoTrainingService videoTrainingService;
    private final VideoTrainingOAuthCredentialsService oauthCredentialsService;
    private final ProcessJobService processJobService;
    private final org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService;
    private final SecurityService securityService;
    private final ExternalMetadataService externalMetadataService;
    private final VideoTrainingUploadService uploadService;
    private final LocaleService localeService;

    public VideoTrainingController(MessageSource messageSource,
            SessionManager sessionManager,
            SiteService siteService,
            ToolManager toolManager,
            @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService,
            VideoTrainingService videoTrainingService,
            VideoTrainingOAuthCredentialsService oauthCredentialsService,
            ProcessJobService processJobService,
            org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService,
            SecurityService securityService,
            ExternalMetadataService externalMetadataService,
            VideoTrainingUploadService uploadService,
            LocaleService localeService) {
        this.messageSource = messageSource;
        this.sessionManager = sessionManager;
        this.siteService = siteService;
        this.toolManager = toolManager;
        this.userTimeService = userTimeService;
        this.videoTrainingService = videoTrainingService;
        this.oauthCredentialsService = oauthCredentialsService;
        this.processJobService = processJobService;
        this.serverConfigurationService = serverConfigurationService;
        this.securityService = securityService;
        this.externalMetadataService = externalMetadataService;
        this.uploadService = uploadService;
        this.localeService = localeService;
    }

    private Locale resolveLocale(Locale locale) {
        if (locale != null) {
            return locale;
        }
        return localeService.getLocaleForCurrentSiteAndUser();
    }

    @GetMapping(value = VideoTrainingConstants.MANAGEABLE_LIST_PATH, params = { "page" })
    public PagedResponse<VideoTrainingVideoView> getManageableVideos(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "asUser", required = false) String asUser,
            HttpServletRequest request,
            Model model,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (StringUtils.isNotBlank(asUser) && securityService.isSuperUser()) {
            userId = asUser;
        }
        Locale effectiveLocale = resolveLocale(locale);

        boolean isUserSite = siteService.isUserSite(siteId);

        if (!isUserSite && !videoTrainingService.canManageLibrary(siteId, userId) &&
            !videoTrainingService.hasManagePermission(siteId, userId)) {
            model.addAttribute("error",
                messageSource.getMessage("video.training.accessDenied", null, resolveLocale(locale)));
            return new PagedResponse<>(Collections.emptyList(), 0L, page, size);
        }

        Long totalCount = 0L;

        if (isUserSite) {
            // Admin will see all and the rest will see the visible ones.
            totalCount = videoTrainingService.countGlobalVideosForUser(userId, query);
        } else {
            totalCount = videoTrainingService.countSiteVideosForUser(siteId, userId, query);
        }

        int safeSize = normalizePageSize(size);
        int safePage = normalizePage(page, safeSize, totalCount);

        List<VideoTrainingVideo> paginatedVideoList = new ArrayList<>();

        if (isUserSite) {
            // Admin will see all and the rest will see the visible ones.
            paginatedVideoList = videoTrainingService.getGlobalVideosForUser(userId, query, safePage, safeSize);
        } else {
            // Admin/Profe will see all, manage/TA will see their videos,
            // and the rest will see the visible ones.
            paginatedVideoList =
                videoTrainingService.getSiteVideosForUserPage(siteId, userId, query, safePage, safeSize);
        }

        List<VideoTrainingVideoView> videoViews = buildVideoViews(
            paginatedVideoList, isUserSite, effectiveLocale);

        return new PagedResponse<VideoTrainingVideoView>(videoViews, totalCount, safePage, safeSize);
    }

    @GetMapping(value = VideoTrainingConstants.VIEWABLE_LIST_PATH, params = { "page" })
    public PagedResponse<VideoTrainingVideoView> getViewableVideos(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "asUser", required = false) String asUser,
            HttpServletRequest request,
            Model model,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (StringUtils.isNotBlank(asUser) && securityService.isSuperUser()) {
            userId = asUser;
        }
        Locale effectiveLocale = resolveLocale(locale);
        String normalizedQuery = StringUtils.trimToEmpty(query);

        boolean isUserSite = siteService.isUserSite(siteId);

        Long totalCount = 0L;

        if (isUserSite) {
            // Admin will see all and the rest will see the visible ones.
            totalCount = videoTrainingService.countGlobalVideosForUser(userId, query);
        } else {
            totalCount = videoTrainingService.countSiteViewableVideosForUser(
                siteId, userId, normalizedQuery
            );
        }

        int safeSize = normalizePageSize(size);
        int safePage = normalizePage(page, safeSize, totalCount);

        List<VideoTrainingVideo> paginatedVideoList = new ArrayList<>();

        if (isUserSite) {
            // Admin will see all and the rest will see the visible ones.
            paginatedVideoList = videoTrainingService.getGlobalVideosForUser(userId, query, safePage, safeSize);
        } else {
            // Admin will see all, manage/TA will see their videos,
            // and the rest will see the visible ones.
            paginatedVideoList = videoTrainingService.getSiteViewableVideosForUserPage(siteId, userId, normalizedQuery, safePage, safeSize);
        }

        List<VideoTrainingVideoView> videoViews = buildVideoViews(
            paginatedVideoList, isUserSite, effectiveLocale);

        return new PagedResponse<VideoTrainingVideoView>(videoViews, totalCount, safePage, safeSize);
    }

    @GetMapping({"/", "/videos"})
    public String list(@RequestParam(name = "viewMode", required = false) String viewMode,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir,
            @RequestParam(name = "accessMode", required = false) String accessMode,
            Locale locale,
            Model model) {

        String siteId = currentSiteId();
        String userId = currentUserId();
        boolean canManageAll = videoTrainingService.canManageLibrary(siteId, userId);
        boolean canManageOwn = videoTrainingService.hasManagePermission(siteId, userId);
        boolean manageableList = resolveManageableListMode(accessMode, canManageAll, canManageOwn);
        String effectiveAccessMode = manageableList ? VideoTrainingConstants.ACCESS_MODE_MANAGEABLE : VideoTrainingConstants.ACCESS_MODE_VIEWABLE;
        Locale effectiveLocale = resolveLocale(locale);
        String effectiveViewMode = resolveEffectiveViewMode(siteId, manageableList, viewMode);
        String normalizedQuery = StringUtils.trimToEmpty(query);
        String normalizedCategoryId = StringUtils.trimToNull(categoryId);
        boolean isUserSite = siteService.isUserSite(siteId);
        int safeSize = normalizePageSize(size);
        int safeOffset = Math.max(0, offset != null ? offset : 0);
        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortDir = normalizeSortDir(sortDir);
        long totalCount;
        if (isUserSite) {
            totalCount = videoTrainingService.countGlobalVideosForUser(userId, normalizedQuery);
        } else if (manageableList && canManageAll) {
            totalCount = normalizedCategoryId == null
                    ? videoTrainingService.countSiteLibrary(siteId, normalizedQuery)
                    : videoTrainingService.countSiteLibrary(siteId, normalizedQuery, normalizedCategoryId);
        } else if (manageableList && canManageOwn) {
            totalCount = normalizedCategoryId == null
                    ? videoTrainingService.countSiteLibraryForOwner(siteId, userId, normalizedQuery)
                    : videoTrainingService.countSiteLibraryForOwner(siteId, userId, normalizedQuery, normalizedCategoryId);
        } else {
            totalCount = normalizedCategoryId == null
                    ? videoTrainingService.countVisibleVideosForUser(siteId, userId, Instant.now(), normalizedQuery)
                    : videoTrainingService.countVisibleVideosForUser(siteId, userId, Instant.now(), normalizedQuery, normalizedCategoryId);
        }

        int requestedPage = page != null ? page : ((safeOffset / safeSize) + 1);
        int safePage = normalizePage(requestedPage, safeSize, totalCount);
        int pageOffset = (safePage - 1) * safeSize;
        int totalPages = (int) Math.max(1, Math.ceil((double) totalCount / safeSize));

        List<VideoTrainingVideo> videos;
        String sortField = mapSortField(normalizedSortBy, isUserSite);
        boolean ascending = "asc".equals(normalizedSortDir);
        if (isUserSite) {
            videos = videoTrainingService.getGlobalVideosSorted(normalizedQuery, pageOffset, safeSize, sortField, ascending);
        } else if (manageableList && canManageAll) {
            videos = normalizedCategoryId == null
                    ? videoTrainingService.getSiteLibrarySorted(siteId, normalizedQuery, pageOffset, safeSize, sortField, ascending)
                    : videoTrainingService.getSiteLibrarySorted(siteId, normalizedQuery, normalizedCategoryId, pageOffset, safeSize, sortField, ascending);
        } else if (manageableList && canManageOwn) {
            videos = normalizedCategoryId == null
                    ? videoTrainingService.getSiteLibrarySortedForOwner(siteId, userId, normalizedQuery, pageOffset, safeSize, sortField, ascending)
                    : videoTrainingService.getSiteLibrarySortedForOwner(siteId, userId, normalizedQuery, normalizedCategoryId, pageOffset, safeSize, sortField, ascending);
        } else {
            videos = normalizedCategoryId == null
                    ? videoTrainingService.getVisibleVideosForUserSorted(siteId, userId, Instant.now(), normalizedQuery, pageOffset, safeSize, sortField, ascending)
                    : videoTrainingService.getVisibleVideosForUserSorted(siteId, userId, Instant.now(), normalizedQuery, normalizedCategoryId, pageOffset, safeSize, sortField, ascending);
        }

        populateNavigationFlags(model, siteId, userId);
        model.addAttribute("categoryTree", buildVisibleCategoryTree(siteId, userId, manageableList, canManageAll, canManageOwn));
        model.addAttribute("selectedCategoryId", normalizedCategoryId);
        populateVideoPresentationModel(model, videos, siteId, userId, effectiveLocale, isUserSite);
        model.addAttribute("isUserSite", isUserSite);
        model.addAttribute("viewMode", effectiveViewMode);
        model.addAttribute("isCardsView", VideoTrainingConstants.VIEW_MODE_CARDS.equals(effectiveViewMode));
        model.addAttribute("isTableView", VideoTrainingConstants.VIEW_MODE_TABLE.equals(effectiveViewMode));
        model.addAttribute("q", normalizedQuery);
        model.addAttribute("categoryId", normalizedCategoryId);
        model.addAttribute("size", safeSize);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("offset", pageOffset);
        model.addAttribute("sortBy", normalizedSortBy);
        model.addAttribute("sortDir", normalizedSortDir);
        model.addAttribute("siteId", siteId);
        model.addAttribute("siteRef", siteService.siteReference(siteId));
        model.addAttribute("currentPath", "/videos");
        model.addAttribute("isManageableList", manageableList);
        model.addAttribute("isViewableList", !manageableList);
        model.addAttribute("accessMode", effectiveAccessMode);
        model.addAttribute("showAccessModeSwitch", canManageOwn && !canManageAll);
        model.addAttribute("moderationEnabled", isModerationEnabled());
        populatePagerModel(model, safePage, safeSize, totalCount);
        model.addAttribute("title", messageSource.getMessage("video.training.title", null, locale));
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/list";
    }

    private boolean resolveManageableListMode(String accessMode, boolean canManageAll, boolean canManageOwn) {
        if (canManageAll) {
            return true;
        }
        if (!canManageOwn) {
            return false;
        }

        String normalizedAccessMode = StringUtils.lowerCase(StringUtils.trimToEmpty(accessMode));
        return !VideoTrainingConstants.ACCESS_MODE_VIEWABLE.equals(normalizedAccessMode);
    }

    private String normalizeSortBy(String sortBy) {
        String normalized = StringUtils.trimToEmpty(sortBy);
        if ("context".equals(normalized)) {
            return normalized;
        }
        return SORT_FIELD_BY_COLUMN.containsKey(normalized) ? normalized : "modified";
    }

    private String normalizeSortDir(String sortDir) {
        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(sortDir));
        return "asc".equals(normalized) ? "asc" : VideoTrainingConstants.DEFAULT_SORT_DIRECTION;
    }

    private String mapSortField(String sortBy, boolean isUserSite) {
        if ("context".equals(sortBy)) {
            return isUserSite ? "siteId" : "providerType";
        }
        return SORT_FIELD_BY_COLUMN.getOrDefault(sortBy, "modifiedOn");
    }

    @GetMapping("/videos/new")
    public String newVideo(RedirectAttributes redirectAttributes, Locale locale, Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (siteId != null && (siteId.startsWith("~") || siteId.startsWith("!"))) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.global.uploadNotAllowed", null, locale));
            return "redirect:/videos";
        }
        if (!(videoTrainingService.canManageLibrary(siteId, userId) || videoTrainingService.hasManagePermission(siteId, userId))) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        populateNavigationFlags(model, siteId, userId);
        if (!model.containsAttribute("video")) {
            model.addAttribute("video", new VideoTrainingVideo());
        }
        model.addAttribute("isEdit", false);
        if (!model.containsAttribute("releaseDateInput")) {
            model.addAttribute("releaseDateInput", "");
        }
        if (!model.containsAttribute("retractDateInput")) {
            model.addAttribute("retractDateInput", "");
        }
        model.addAttribute("timezoneId", getUserZoneId().getId());
        model.addAttribute("hlsUploadEnabled", uploadService.isHlsUploadEnabled());
        List<SiteVideoResourceOption> existingResources = uploadService.getExistingSiteVideoResources(siteId);
        model.addAttribute("existingVideoResources", existingResources);
        if (!model.containsAttribute("sourceMode")) {
            model.addAttribute("sourceMode", VideoTrainingConstants.SOURCE_MODE_UPLOAD);
        }
        model.addAttribute("nativeUploadMaxBytes", uploadService.getConfiguredMaxNativeUploadBytes());
        model.addAttribute("providerTypes", VideoProviderType.values());
        model.addAttribute("youtubeUploadConfigured", oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD));
        model.addAttribute("youtubeMetadataConfigured", oauthCredentialsService.isYoutubeMetadataConfigured());
        model.addAttribute("defaultUploadProviderType", uploadService.defaultUploadProviderType(null));
        model.addAttribute("visibilityScopes", getVisibilityScopesForUser(siteId, userId));
        model.addAttribute("publicationStatuses", new VideoPublicationStatus[] { VideoPublicationStatus.DRAFT });
        if (!model.containsAttribute("selectedCategoryIds")) {
            populateCategoryModel(model, siteId, null);
        } else {
            model.addAttribute("categoryTree", videoTrainingService.getSelectableCategoryTree(siteId, "", 1, Integer.MAX_VALUE));
        }
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/edit";
    }

    @GetMapping("/credentials")
    public String adminCredentials(RedirectAttributes redirectAttributes, Locale locale, Model model) {
        if (!securityService.isSuperUser()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        model.addAttribute("isAdmin", true);
        model.addAttribute("youtubeCredentials", oauthCredentialsService.loadCredentialsForForm(VideoProviderType.YOUTUBE_UPLOAD));
        model.addAttribute("youtubeAuthorizationReady", oauthCredentialsService.isAuthorizationReady(VideoProviderType.YOUTUBE_UPLOAD));
        return "video-training/credentials";
    }

    @PostMapping("/credentials")
    public String saveAdminCredentials(@RequestParam(name = "youtubeClientId", required = false) String youtubeClientId,
            @RequestParam(name = "youtubeClientSecret", required = false) String youtubeClientSecret,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        if (!securityService.isSuperUser()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        String currentYoutubeRefreshToken = oauthCredentialsService.getCredentials(VideoProviderType.YOUTUBE_UPLOAD)
                .map(VideoTrainingOAuthCredentials::getRefreshToken)
                .orElse(null);
        String currentYoutubeApiKey = oauthCredentialsService.getCredentials(VideoProviderType.YOUTUBE_UPLOAD)
                .map(VideoTrainingOAuthCredentials::getApiKey)
                .orElse(null);
        oauthCredentialsService.saveCredentials(VideoProviderType.YOUTUBE_UPLOAD, youtubeClientId, currentYoutubeApiKey, youtubeClientSecret, currentYoutubeRefreshToken);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.credentials.saved", null, locale));
        return "redirect:/credentials";
    }

    @GetMapping("/credentials/youtube/connect")
    public String connectYoutubeCredentials(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        if (!securityService.isSuperUser()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        VideoTrainingOAuthCredentials credentials = oauthCredentialsService.getCredentials(VideoProviderType.YOUTUBE_UPLOAD).orElse(null);
        if (credentials == null || StringUtils.isBlank(credentials.getClientId()) || StringUtils.isBlank(credentials.getClientSecret())) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.credentials.youtube.notConfigured", null, locale));
            return "redirect:/credentials";
        }

        String state = UUID.randomUUID().toString();
        sessionManager.getCurrentSession().setAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY, state);
        String redirectUri = buildAbsoluteUrl(request, "/credentials/youtube/callback");
        return "redirect:" + oauthCredentialsService.buildYoutubeAuthorizationUrl(redirectUri, state);
    }

    @GetMapping("/credentials/youtube/callback")
    public String youtubeCredentialsCallback(@RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        if (!securityService.isSuperUser()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

            String sessionState = StringUtils.trimToEmpty((String) sessionManager.getCurrentSession().getAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY));
        sessionManager.getCurrentSession().removeAttribute(VideoTrainingConstants.PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY);
        if (StringUtils.isBlank(code) || !StringUtils.equals(sessionState, StringUtils.trimToEmpty(state))) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.credentials.youtube.authorizationFailed", null, locale));
            return "redirect:/credentials";
        }

        VideoTrainingOAuthCredentials current = oauthCredentialsService.getCredentials(VideoProviderType.YOUTUBE_UPLOAD).orElse(null);
        if (current == null || StringUtils.isBlank(current.getClientId()) || StringUtils.isBlank(current.getClientSecret())) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.credentials.youtube.notConfigured", null, locale));
            return "redirect:/credentials";
        }

        try {
            String redirectUri = buildAbsoluteUrl(request, "/credentials/youtube/callback");
            String refreshToken = oauthCredentialsService.exchangeYoutubeRefreshToken(redirectUri, code);
            oauthCredentialsService.saveCredentials(VideoProviderType.YOUTUBE_UPLOAD, current.getClientId(), current.getApiKey(), current.getClientSecret(), refreshToken);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.credentials.youtube.authorized", null, locale));
        } catch (Exception ex) {
            log.warn("Failed to complete YouTube OAuth authorization", ex);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.credentials.youtube.authorizationFailed", null, locale));
        }

        return "redirect:/credentials";
    }

    @GetMapping("/videos/{videoId}/edit")
    public String editVideo(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale,
            Model model) {

        String siteId = currentSiteId();
        String userId = currentUserId();
        VideoTrainingVideo video = videoTrainingService.getVideoById(videoId).orElse(null);
        if (video == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }

        // allow full site managers or owner-level managers for their own videos
        if (!(videoTrainingService.canManageLibrary(siteId, userId)
            || (videoTrainingService.hasManagePermission(siteId, userId) && Objects.equals(video.getOwnerId(), userId)))) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        populateNavigationFlags(model, siteId, userId);
        if (!model.containsAttribute("video")) {
            model.addAttribute("video", video);
        }
        model.addAttribute("isEdit", true);
        if (!model.containsAttribute("releaseDateInput")) {
            model.addAttribute("releaseDateInput", formatInstantForInput(video.getReleaseDate()));
        }
        if (!model.containsAttribute("retractDateInput")) {
            model.addAttribute("retractDateInput", formatInstantForInput(video.getRetractDate()));
        }
        model.addAttribute("timezoneId", getUserZoneId().getId());
        model.addAttribute("hlsUploadEnabled", uploadService.isHlsUploadEnabled());
        List<SiteVideoResourceOption> existingResources = uploadService.getExistingSiteVideoResources(siteId);
        model.addAttribute("existingVideoResources", existingResources);
        if (!model.containsAttribute("sourceMode")) {
            model.addAttribute("sourceMode", determineSourceMode(video, existingResources));
        }
        model.addAttribute("nativeUploadMaxBytes", uploadService.getConfiguredMaxNativeUploadBytes());
        model.addAttribute("providerTypes", VideoProviderType.values());
        model.addAttribute("youtubeUploadConfigured", oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD));
        model.addAttribute("youtubeMetadataConfigured", oauthCredentialsService.isYoutubeMetadataConfigured());
        model.addAttribute("defaultUploadProviderType", uploadService.defaultUploadProviderType(video));
        model.addAttribute("visibilityScopes", getVisibilityScopesForUser(siteId, userId));
        VideoVisibilityScope scope = video.getVisibilityScope() != null ? video.getVisibilityScope() : VideoVisibilityScope.COURSE;
        VideoPublicationStatus[] validTransitions = videoTrainingService.getValidPublicationStatusTransitions(video.getPublicationStatus(), scope);
        model.addAttribute("publicationStatuses", filterPublicationStatusesForUser(siteId, userId, validTransitions));
        model.addAttribute("currentPublicationStatus", video.getPublicationStatus());
        if (!model.containsAttribute("selectedCategoryIds")) {
            populateCategoryModel(model, siteId, video);
        } else {
            model.addAttribute("categoryTree", videoTrainingService.getSelectableCategoryTree(siteId, "", 1, Integer.MAX_VALUE));
        }
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/edit";
    }

    @PostMapping(value = "/videos/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MetadataFetchResult fetchVideoMetadata(
            @RequestParam(name = "sourceMode", required = false) String sourceMode,
            @RequestParam(name = "sourceReference", required = false) String sourceReference,
            @RequestParam(name = "existingResourceReference", required = false) String existingResourceReference,
            @RequestParam(name = "nativeFile", required = false) MultipartFile nativeFile) {

        String effectiveSourceMode = resolveSourceMode(sourceMode, null);
        if (VideoTrainingConstants.SOURCE_MODE_EXTERNAL.equals(effectiveSourceMode)) {
            String normalizedSourceReference = normalizeExternalSourceReference(sourceReference);
            return StringUtils.isBlank(normalizedSourceReference)
                    ? new MetadataFetchResult("", "")
                    : externalMetadataService.fetch(normalizedSourceReference);
        }

        if (VideoTrainingConstants.SOURCE_MODE_RESOURCES.equals(effectiveSourceMode)) {
            return uploadService.resolveResourceMetadata(existingResourceReference);
        }

        return resolveNativeUploadMetadata(nativeFile);
    }

    @PostMapping("/videos")
    public String createVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("providerType") String providerType,
            @RequestParam(name = "sourceMode", required = false) String sourceMode,
            @RequestParam(name = "sourceReference", required = false) String sourceReference,
            @RequestParam(name = "existingResourceReference", required = false) String existingResourceReference,
            @RequestParam(name = "nativeFile", required = false) MultipartFile nativeFile,
            @RequestParam(name = "inheritTitleMetadata", required = false) String inheritTitleMetadata,
            @RequestParam(name = "inheritDescriptionMetadata", required = false) String inheritDescriptionMetadata,
            @RequestParam(name = "visibilityScope", required = false) String visibilityScope,
            @RequestParam(name = "publicationStatus", required = false) String publicationStatus,
            @RequestParam(name = "releaseDate", required = false) String releaseDate,
            @RequestParam(name = "retractDate", required = false) String retractDate,
            @RequestParam(name = "categoryIds", required = false) List<String> categoryIds,
            @RequestParam(name = "categoryIdsProvided", required = false) String categoryIdsProvided,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        String effectiveSourceMode = resolveSourceMode(sourceMode, providerType);
        VideoProviderType parsedProviderType = resolveProviderTypeForSourceMode(effectiveSourceMode, providerType);
        VideoVisibilityScope parsedVisibilityScope;
        VideoPublicationStatus parsedPublicationStatus = VideoPublicationStatus.DRAFT;
        try {
            parsedVisibilityScope = parseVisibilityScope(visibilityScope);
        } catch (IllegalArgumentException ex) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/new";
        }

        String siteId = currentSiteId();
        String userId = currentUserId();
        if (parsedVisibilityScope == VideoVisibilityScope.GLOBAL && !canUseGlobalVisibility(siteId, userId)) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos/new";
        }
        if (isGlobalSiteId(siteId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.global.uploadNotAllowed", null, locale));
            return "redirect:/videos";
        }
        String uploadedSourceReference = null;
        String stagedTempFilePath = null;
        String resolvedSourceReference;
        Long resolvedFileSizeBytes = null;

        if (VideoTrainingConstants.SOURCE_MODE_EXTERNAL.equals(effectiveSourceMode)) {
            resolvedSourceReference = normalizeExternalSourceReference(sourceReference);
            if (StringUtils.isBlank(resolvedSourceReference)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidExternalSource", null, locale));
                return "redirect:/videos/new";
            }

            boolean wantTitle = StringUtils.isNotBlank(inheritTitleMetadata);
            boolean wantDescription = StringUtils.isNotBlank(inheritDescriptionMetadata);
            if (wantTitle || wantDescription) {
                String videoProvider = externalMetadataService.retrieveVideoProvider(resolvedSourceReference);
                try {
                    MetadataFetchResult meta = externalMetadataService.fetch(resolvedSourceReference);
                    if (wantTitle && StringUtils.isBlank(meta.getTitle())) {
                        preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.noTitleFromProvider", new Object[] { videoProvider }, locale));
                        return "redirect:/videos/new";
                    }
                    if (wantDescription && StringUtils.isBlank(meta.getDescription())) {
                        preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.noDescriptionFromProvider", new Object[] { videoProvider }, locale));
                        return "redirect:/videos/new";
                    }
                    if (wantTitle && StringUtils.isNotBlank(meta.getTitle())) {
                        title = meta.getTitle();
                    }
                    if (wantDescription && StringUtils.isNotBlank(meta.getDescription())) {
                        description = meta.getDescription();
                    }
                } catch (Exception ex) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.fetchFailedProvider", new Object[] { videoProvider }, locale));
                    return "redirect:/videos/new";
                }
            }
        } else if (VideoTrainingConstants.SOURCE_MODE_RESOURCES.equals(effectiveSourceMode)) {
            if (parsedVisibilityScope == VideoVisibilityScope.GLOBAL) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidGlobalResourceScope", null, locale));
                return "redirect:/videos/new";
            }
            resolvedSourceReference = StringUtils.trimToEmpty(existingResourceReference);
            if (StringUtils.isBlank(resolvedSourceReference) || !uploadService.isExistingSiteVideoResourceReference(siteId, resolvedSourceReference)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidResourceReference", null, locale));
                return "redirect:/videos/new";
            }
            resolvedFileSizeBytes = uploadService.resolveNativeResourceSizeBytes(resolvedSourceReference);
            parsedProviderType = VideoProviderType.RESOURCES;
        } else {
            if (nativeFile == null || nativeFile.isEmpty()) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadRequired", null, locale));
                return "redirect:/videos/new";
            }
            if (!uploadService.isValidNativeUpload(nativeFile.getOriginalFilename(), nativeFile.getContentType(), false)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadInvalidType", null, locale));
                return "redirect:/videos/new";
            }

            if (parsedProviderType == VideoProviderType.YOUTUBE_UPLOAD && !oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidProvider", null, locale));
                return "redirect:/videos/new";
            }

            Long maxNativeUploadBytes = uploadService.getConfiguredMaxNativeUploadBytes();
            Long margin = 64L * 1024L;
            if (nativeFile.getSize() > (maxNativeUploadBytes + margin)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadTooLarge", null, locale));
                return "redirect:/videos/new";
            }

            if (uploadService.isManagedUploadProvider(parsedProviderType)) {
                try {
                    stagedTempFilePath = uploadService.stageTemporaryManagedUpload(nativeFile.getInputStream());
                    resolvedSourceReference = stagedTempFilePath;
                    resolvedFileSizeBytes = nativeFile.getSize();
                    parsedPublicationStatus = VideoPublicationStatus.DRAFT;
                } catch (Exception ex) {
                    uploadService.cleanupTemporaryUpload(stagedTempFilePath);
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadFailed", null, locale));
                    return "redirect:/videos/new";
                }
            } else {
                try {
                    uploadedSourceReference = uploadService.uploadNativeVideo(siteId, currentUserId(), parsedVisibilityScope,
                            nativeFile.getBytes(), nativeFile.getOriginalFilename(), nativeFile.getContentType(), nativeFile.getSize());
                    resolvedSourceReference = uploadedSourceReference;
                    resolvedFileSizeBytes = nativeFile.getSize();
                    parsedProviderType = VideoProviderType.NATIVE;
                } catch (OverQuotaException ex) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadOverQuota", null, locale));
                    return "redirect:/videos/new";
                } catch (Exception ex) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadFailed", null, locale));
                    return "redirect:/videos/new";
                }
            }
        }

        if (StringUtils.isBlank(title)
                || (parsedProviderType == VideoProviderType.EXTERNAL && StringUtils.isBlank(resolvedSourceReference))
                || (parsedProviderType != VideoProviderType.EXTERNAL && StringUtils.isBlank(resolvedSourceReference))) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/new";
        }

        Instant parsedReleaseDate;
        Instant parsedRetractDate;
        try {
            parsedReleaseDate = parseInputDateTime(releaseDate);
            parsedRetractDate = parseInputDateTime(retractDate);
        } catch (IllegalArgumentException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidDateTime", null, locale));
            return "redirect:/videos/new";
        }

        if (!isValidVisibilityWindow(parsedReleaseDate, parsedRetractDate)) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidVisibilityWindow", null, locale));
            return "redirect:/videos/new";
        }

        VideoTrainingVideo video = new VideoTrainingVideo(
                siteId,
                currentUserId(),
                StringUtils.trimToEmpty(title),
                StringUtils.isNotBlank(inheritTitleMetadata),
                StringUtils.isNotBlank(inheritDescriptionMetadata),
                StringUtils.trimToEmpty(description),
                parsedProviderType,
                resolvedSourceReference,
                resolvedFileSizeBytes,
                parsedVisibilityScope,
                parsedPublicationStatus,
                parsedReleaseDate,
                parsedRetractDate,
                VideoTrainingConstants.PERMISSION_VIEW);

        try {
            VideoTrainingVideo savedVideo = saveVideoWithOptionalCategories(video, categoryIds, categoryIdsProvided != null);
            if (uploadService.isManagedUploadProvider(savedVideo.getProviderType())) {
                processJobService.queueManagedUploadJob(savedVideo.getId(), savedVideo.getOwnerId(), stagedTempFilePath, savedVideo.getProviderType());
                redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.createdProcessing", null, locale));
            } else {
                redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.created", null, locale));
            }
        } catch (SecurityException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        } catch (IllegalArgumentException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/new";
        } catch (RuntimeException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            if (StringUtils.isNotBlank(video.getId())) {
                try {
                    videoTrainingService.deleteVideo(video.getId());
                } catch (Exception ignored) {
                }
            }
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/new";
        }

        return "redirect:/videos";
    }

    private boolean isGlobalSiteId(String siteId) {
        if (StringUtils.isBlank(siteId)) {
            return true;
        }
        return siteId.startsWith("~") || siteId.startsWith("!");
    }

    @PostMapping("/videos/{videoId}")
    public String updateVideo(@PathVariable String videoId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("providerType") String providerType,
            @RequestParam(name = "sourceMode", required = false) String sourceMode,
            @RequestParam(name = "sourceReference", required = false) String sourceReference,
            @RequestParam(name = "existingResourceReference", required = false) String existingResourceReference,
            @RequestParam(name = "nativeFile", required = false) MultipartFile nativeFile,
            @RequestParam(name = "inheritTitleMetadata", required = false) String inheritTitleMetadata,
            @RequestParam(name = "inheritDescriptionMetadata", required = false) String inheritDescriptionMetadata,
            @RequestParam(name = "visibilityScope", required = false) String visibilityScope,
            @RequestParam(name = "publicationStatus", required = false) String publicationStatus,
            @RequestParam(name = "releaseDate", required = false) String releaseDate,
            @RequestParam(name = "retractDate", required = false) String retractDate,
            @RequestParam(name = "categoryIds", required = false) List<String> categoryIds,
            @RequestParam(name = "categoryIdsProvided", required = false) String categoryIdsProvided,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }
        VideoTrainingVideo originalVideo = copyVideo(existing);

        String effectiveSourceMode = resolveSourceMode(sourceMode, providerType);
        VideoProviderType parsedProviderType = resolveProviderTypeForSourceMode(effectiveSourceMode, providerType);
        VideoVisibilityScope parsedVisibilityScope;
        VideoPublicationStatus parsedPublicationStatus;
        try {
            parsedVisibilityScope = parseVisibilityScope(visibilityScope);
            parsedPublicationStatus = parsePublicationStatus(publicationStatus);
        } catch (IllegalArgumentException ex) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (parsedVisibilityScope == VideoVisibilityScope.GLOBAL && !canUseGlobalVisibility(siteId, userId)) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }
        String previousSourceReference = StringUtils.trimToEmpty(existing.getSourceReference());
        VideoProviderType previousProviderType = existing.getProviderType();

        String uploadedSourceReference = null;
        String stagedTempFilePath = null;
        String resolvedSourceReference;
        Long resolvedFileSizeBytes = existing.getFileSizeBytes();
        if (VideoTrainingConstants.SOURCE_MODE_EXTERNAL.equals(effectiveSourceMode)) {
            resolvedSourceReference = normalizeExternalSourceReference(sourceReference);
            if (StringUtils.isBlank(resolvedSourceReference)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidExternalSource", null, locale));
                return "redirect:/videos/" + videoId + "/edit";
            }
            resolvedFileSizeBytes = null;
            boolean wantTitle = StringUtils.isNotBlank(inheritTitleMetadata);
            boolean wantDescription = StringUtils.isNotBlank(inheritDescriptionMetadata);
            if (wantTitle || wantDescription) {
                String videoProvider = externalMetadataService.retrieveVideoProvider(resolvedSourceReference);
                try {
                    MetadataFetchResult meta = externalMetadataService.fetch(resolvedSourceReference);
                    if (wantTitle && StringUtils.isBlank(meta.getTitle())) {
                        preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.noTitleFromProvider", new Object[] { videoProvider }, locale));
                        return "redirect:/videos/" + videoId + "/edit";
                    }
                    if (wantDescription && StringUtils.isBlank(meta.getDescription())) {
                        preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.noDescriptionFromProvider", new Object[] { videoProvider }, locale));
                        return "redirect:/videos/" + videoId + "/edit";
                    }
                    if (wantTitle && StringUtils.isNotBlank(meta.getTitle())) {
                        title = meta.getTitle();
                    }
                    if (wantDescription && StringUtils.isNotBlank(meta.getDescription())) {
                        description = meta.getDescription();
                    }
                } catch (Exception e) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.metadata.fetchFailedProvider", new Object[] { videoProvider }, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }
            }
        } else if (VideoTrainingConstants.SOURCE_MODE_RESOURCES.equals(effectiveSourceMode)) {
            if (parsedVisibilityScope == VideoVisibilityScope.GLOBAL) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidGlobalResourceScope", null, locale));
                return "redirect:/videos/" + videoId + "/edit";
            }
            resolvedSourceReference = StringUtils.trimToEmpty(existingResourceReference);
            if (StringUtils.isBlank(resolvedSourceReference) || !uploadService.isExistingSiteVideoResourceReference(existing.getSiteId(), resolvedSourceReference)) {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidResourceReference", null, locale));
                return "redirect:/videos/" + videoId + "/edit";
            }
            resolvedFileSizeBytes = uploadService.resolveNativeResourceSizeBytes(resolvedSourceReference);
        } else {
            if (nativeFile != null && !nativeFile.isEmpty()) {
                if (!uploadService.isValidNativeUpload(nativeFile.getOriginalFilename(), nativeFile.getContentType(), false)) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadInvalidType", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }

                if (parsedProviderType == VideoProviderType.YOUTUBE_UPLOAD && !oauthCredentialsService.isConfigured(VideoProviderType.YOUTUBE_UPLOAD)) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidProvider", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }

                Long maxNativeUploadBytes = uploadService.getConfiguredMaxNativeUploadBytes();
                Long margin = 64L * 1024L; // 64KB margin for multipart overhead
                if (nativeFile.getSize() > (maxNativeUploadBytes + margin)) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadTooLarge", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }
                try {
                    if (uploadService.isManagedUploadProvider(parsedProviderType)) {
                        stagedTempFilePath = uploadService.stageTemporaryManagedUpload(nativeFile.getInputStream());
                        uploadedSourceReference = stagedTempFilePath;
                        resolvedSourceReference = stagedTempFilePath;
                        resolvedFileSizeBytes = nativeFile.getSize();
                        parsedPublicationStatus = VideoPublicationStatus.DRAFT;
                    } else {
                        uploadedSourceReference = uploadService.uploadNativeVideo(existing.getSiteId(), currentUserId(), parsedVisibilityScope,
                                nativeFile.getBytes(), nativeFile.getOriginalFilename(), nativeFile.getContentType(), nativeFile.getSize());
                        resolvedSourceReference = uploadedSourceReference;
                        resolvedFileSizeBytes = nativeFile.getSize();
                        parsedProviderType = VideoProviderType.NATIVE;
                    }
                } catch (OverQuotaException ex) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadOverQuota", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                } catch (Exception ex) {
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadFailed", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }
            } else if (uploadService.isManagedUploadProvider(existing.getProviderType())) {
                resolvedSourceReference = previousSourceReference;
                if (resolvedFileSizeBytes == null) {
                    resolvedFileSizeBytes = existing.getFileSizeBytes();
                }
                parsedProviderType = existing.getProviderType();
            } else if (existing.getProviderType() == VideoProviderType.NATIVE) {
                resolvedSourceReference = previousSourceReference;
                if (resolvedFileSizeBytes == null) {
                    resolvedFileSizeBytes = uploadService.resolveNativeResourceSizeBytes(previousSourceReference);
                }

                String relocatedSourceReference = uploadService.relocateManagedNativeResourceIfNeeded(
                        previousSourceReference,
                        existing.getSiteId(),
                    StringUtils.defaultIfBlank(existing.getOwnerId(), currentUserId()),
                        parsedVisibilityScope);
                if (!StringUtils.equals(relocatedSourceReference, previousSourceReference)) {
                    uploadedSourceReference = relocatedSourceReference;
                    resolvedSourceReference = relocatedSourceReference;
                    if (resolvedFileSizeBytes == null) {
                        resolvedFileSizeBytes = uploadService.resolveNativeResourceSizeBytes(relocatedSourceReference);
                    }
                }
            } else {
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.nativeUploadRequired", null, locale));
                return "redirect:/videos/" + videoId + "/edit";
            }
        }

        if (StringUtils.isBlank(title)
                || (parsedProviderType == VideoProviderType.EXTERNAL && StringUtils.isBlank(resolvedSourceReference))
                || (parsedProviderType != VideoProviderType.EXTERNAL && StringUtils.isBlank(resolvedSourceReference))) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }

        Instant parsedReleaseDate;
        Instant parsedRetractDate;
        try {
            parsedReleaseDate = parseInputDateTime(releaseDate);
            parsedRetractDate = parseInputDateTime(retractDate);
        } catch (IllegalArgumentException ex) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidDateTime", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }

        if (!isValidVisibilityWindow(parsedReleaseDate, parsedRetractDate)) {
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidVisibilityWindow", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }

        VideoPublicationStatus currentStatus = existing.getPublicationStatus();
        if (!StringUtils.equals(currentStatus != null ? currentStatus.name() : null,
                parsedPublicationStatus.name())) {
            try {
                VideoPublicationStatus[] validTransitions = videoTrainingService.getValidPublicationStatusTransitions(
                        currentStatus, parsedVisibilityScope);
                boolean transitionValid = false;
                for (VideoPublicationStatus valid : validTransitions) {
                    if (valid == parsedPublicationStatus) {
                        transitionValid = true;
                        break;
                    }
                }
                if (!transitionValid) {
                    uploadService.cleanupManagedNativeResource(uploadedSourceReference);
                    preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidStatusTransition", null, locale));
                    return "redirect:/videos/" + videoId + "/edit";
                }
            } catch (Exception ex) {
                uploadService.cleanupManagedNativeResource(uploadedSourceReference);
                preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidStatusTransition", null, locale));
                return "redirect:/videos/" + videoId + "/edit";
            }
        }

        existing.setTitle(StringUtils.trimToEmpty(title));
        existing.setInheritTitleMetadata(StringUtils.isNotBlank(inheritTitleMetadata));
        existing.setInheritDescriptionMetadata(StringUtils.isNotBlank(inheritDescriptionMetadata));
        existing.setDescription(StringUtils.trimToEmpty(description));
        existing.setProviderType(parsedProviderType);
        existing.setSourceReference(resolvedSourceReference);
        existing.setFileSizeBytes(parsedProviderType == VideoProviderType.EXTERNAL ? null : resolvedFileSizeBytes);
        existing.setVisibilityScope(parsedVisibilityScope);
        existing.setPublicationStatus(parsedPublicationStatus);
        existing.setRequiredViewPermission(VideoTrainingConstants.PERMISSION_VIEW);
        existing.setReleaseDate(parsedReleaseDate);
        existing.setRetractDate(parsedRetractDate);

        try {
            VideoTrainingVideo savedVideo = saveVideoWithOptionalCategories(existing, categoryIds, categoryIdsProvided != null);
            videoTrainingService.syncExternalProviderPrivacy(savedVideo);
            if (uploadService.isManagedUploadProvider(savedVideo.getProviderType()) && StringUtils.isNotBlank(stagedTempFilePath)) {
                processJobService.queueManagedUploadJob(savedVideo.getId(), savedVideo.getOwnerId(), stagedTempFilePath, savedVideo.getProviderType());
            }
        } catch (SecurityException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        } catch (IllegalArgumentException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        } catch (RuntimeException ex) {
            uploadService.cleanupManagedNativeResource(uploadedSourceReference);
            uploadService.cleanupTemporaryUpload(stagedTempFilePath);
            try {
                videoTrainingService.saveVideo(originalVideo);
            } catch (Exception ignored) {
            }
            preserveFormState(redirectAttributes, title, description, providerType, sourceMode, sourceReference, existingResourceReference, inheritTitleMetadata, inheritDescriptionMetadata, visibilityScope, publicationStatus, releaseDate, retractDate, categoryIds, categoryIdsProvided);
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
            return "redirect:/videos/" + videoId + "/edit";
        }

        if (previousProviderType == VideoProviderType.NATIVE
                && !StringUtils.equals(previousSourceReference, resolvedSourceReference)) {
            uploadService.cleanupManagedNativeResource(previousSourceReference);
        }

        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.updated", null, locale));
        return "redirect:/videos";
    }

    @PostMapping("/videos/{videoId}/delete")
    public String deleteVideo(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }

        try {
            videoTrainingService.deleteVideo(videoId);
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        if (existing.getProviderType() == VideoProviderType.NATIVE) {
            uploadService.cleanupManagedNativeResource(existing.getSourceReference());
        }

        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.deleted", null, locale));
        return "redirect:/videos";
    }

    @PostMapping(value = "/videos/{videoId}/delete/ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> deleteVideoAjax(@PathVariable String videoId, Locale locale) {
        Map<String, Object> response = new HashMap<>();
        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElse(null);
        if (existing == null) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.notFound", null, resolveLocale(locale)));
            return response;
        }

        try {
            videoTrainingService.deleteVideo(videoId);
            if (existing.getProviderType() == VideoProviderType.NATIVE) {
                uploadService.cleanupManagedNativeResource(existing.getSourceReference());
            }
            response.put("success", true);
        } catch (SecurityException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.accessDenied", null, resolveLocale(locale)));
        }

        return response;
    }

    @PostMapping(value = "/videos/{videoId}/status/ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updatePublicationStatusAjax(@PathVariable String videoId,
            @RequestParam(name = "status") String status,
            Locale locale) {
        Map<String, Object> response = new HashMap<>();
        VideoPublicationStatus parsed;
        try {
            parsed = parsePublicationStatus(status);
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.invalidStatusTransition", null, resolveLocale(locale)));
            return response;
        }

        try {
            VideoTrainingVideo saved = updatePublicationStatus(videoId, parsed);
            videoTrainingService.syncExternalProviderPrivacy(saved);
            response.put("success", true);
            response.put("status", saved.getPublicationStatus() != null ? saved.getPublicationStatus().name() : null);
        } catch (SecurityException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.accessDenied", null, resolveLocale(locale)));
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.invalidStatusTransition", null, resolveLocale(locale)));
        } catch (IllegalStateException ex) {
            String resolved = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), resolveLocale(locale));
            response.put("success", false);
            response.put("error", resolved);
        }

        return response;
    }

        @PostMapping("/videos/{videoId}/publish")
        public String publishVideo(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }

        VideoPublicationStatus targetStatus = VideoPublicationStatus.PUBLISHED;
        String successMessageKey = "video.training.published";
        boolean moderationEnabled = isModerationEnabled();
        boolean canApprove = canApprovePublication(existing.getSiteId(), currentUserId());
        if (moderationEnabled
            && !canApprove
                && (existing.getPublicationStatus() == VideoPublicationStatus.DRAFT
                    || existing.getPublicationStatus() == VideoPublicationStatus.WITHDRAWN)) {
            targetStatus = VideoPublicationStatus.PENDING_APPROVAL;
            successMessageKey = "video.training.pendingApprovalSubmitted";
        }

        return updatePublicationStatus(videoId, targetStatus,
            successMessageKey, redirectAttributes, locale);
        }

        @PostMapping("/videos/{videoId}/withdraw")
        public String withdrawVideo(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        return updatePublicationStatus(videoId, VideoPublicationStatus.WITHDRAWN,
            "video.training.withdrawn", redirectAttributes, locale);
        }

        @PostMapping("/videos/{videoId}/archive")
        public String archiveVideo(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        return updatePublicationStatus(videoId, VideoPublicationStatus.ARCHIVED,
            "video.training.archived", redirectAttributes, locale);
        }

        @PostMapping("/videos/{videoId}/restore-draft")
        public String restoreVideoToDraft(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        return updatePublicationStatus(videoId, VideoPublicationStatus.DRAFT,
            "video.training.restoredDraft", redirectAttributes, locale);
        }

        @PostMapping("/videos/{videoId}/submit-approval")
        public String submitVideoForApproval(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        return updatePublicationStatus(videoId, VideoPublicationStatus.PENDING_APPROVAL,
            "video.training.pendingApprovalSubmitted", redirectAttributes, locale);
        }

        @PostMapping("/videos/{videoId}/reject-approval")
        public String rejectVideoApproval(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        return updatePublicationStatus(videoId, VideoPublicationStatus.DRAFT,
            "video.training.pendingApprovalRejected", redirectAttributes, locale);
        }

    @GetMapping("/videos/{videoId}")
    public String details(@PathVariable String videoId,
            RedirectAttributes redirectAttributes,
            Locale locale,
            @RequestParam(name = "lessonPageId", required = false) String lessonPageId,
            Model model) {

        VideoTrainingVideo video = videoTrainingService.getVideoById(videoId).orElse(null);
        if (video == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }

        String userId = currentUserId();
        boolean canManageAll = videoTrainingService.canManageLibrary(video.getSiteId(), userId);
        boolean canManageOwn = videoTrainingService.hasManagePermission(video.getSiteId(), userId) && Objects.equals(video.getOwnerId(), userId);
        boolean canManage = canManageAll || canManageOwn;
        boolean canApprovePublication = canApprovePublication(video.getSiteId(), userId);
        if (!canManage && !videoTrainingService.canViewVideo(video, userId, Instant.now(), lessonPageId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        videoTrainingService.registerView(video.getSiteId(), videoId, userId, Instant.now(), lessonPageId);
        Locale effectiveLocale = resolveLocale(locale);

        populateNavigationFlags(model, video.getSiteId(), userId);
        String externalEmbedUrl = video.getProviderType() != VideoProviderType.NATIVE && video.getProviderType() != VideoProviderType.HLS_UPLOAD && video.getProviderType() != VideoProviderType.RESOURCES
            ? normalizeExternalSourceReference(video.getSourceReference())
            : "";
        model.addAttribute("video", video);
        model.addAttribute("canManage", canManage);
        model.addAttribute("canApprove", canApprovePublication);
        model.addAttribute("isNativeVideo", video.getProviderType() == VideoProviderType.NATIVE || video.getProviderType() == VideoProviderType.HLS_UPLOAD || video.getProviderType() == VideoProviderType.RESOURCES);
        model.addAttribute("isProcessingUpload", processJobService.isProcessingManagedUpload(video.getId(), video.getProviderType()));
        model.addAttribute("externalEmbedUrl", externalEmbedUrl);
        model.addAttribute("nativePlaybackUrl", uploadService.resolveNativePlaybackUrl(video));
        model.addAttribute("nativeContentType", uploadService.resolveNativeContentType(video));
        model.addAttribute("moderationEnabled", isModerationEnabled());
        model.addAttribute("isVisibleNow", videoTrainingService.canViewVideo(video, userId, Instant.now(), lessonPageId));
        model.addAttribute("releaseDateDisplay", formatInstantForDisplay(video.getReleaseDate(), effectiveLocale));
        model.addAttribute("retractDateDisplay", formatInstantForDisplay(video.getRetractDate(), effectiveLocale));
        model.addAttribute("isFavorite",
            videoTrainingService.getUserVideoPreference(video.getSiteId(), videoId, userId)
                .map(pref -> pref.isFavorite())
                .orElse(false));
        model.addAttribute("isWatchLater",
            videoTrainingService.getUserVideoPreference(video.getSiteId(), videoId, userId)
                .map(pref -> pref.isWatchLater())
                .orElse(false));
        model.addAttribute("currentPath", "/videos/" + videoId);
        model.addAttribute("isGlobalSite", isGlobalSiteId(video.getSiteId()));
        return "video-training/details";
    }

    @PostMapping("/videos/{videoId}/favorite")
    public String setFavorite(@PathVariable String videoId,
        @RequestParam(name = "favorite", defaultValue = "true") boolean favorite,
        @RequestParam(name = "returnTo", required = false) String returnTo,
        RedirectAttributes redirectAttributes,
        Locale locale) {

        String siteId = currentSiteId();
        String userId = currentUserId();
        try {
            videoTrainingService.setUserFavorite(siteId, videoId, userId, favorite);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage(favorite ? "video.training.favorites.added" : "video.training.favorites.removed", null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
        }

        return "redirect:" + resolveReturnPath(returnTo, "/videos/" + videoId);
    }

    @PostMapping("/videos/{videoId}/watch-later")
    public String setWatchLater(@PathVariable String videoId,
        @RequestParam(name = "watchLater", defaultValue = "true") boolean watchLater,
        @RequestParam(name = "returnTo", required = false) String returnTo,
        RedirectAttributes redirectAttributes,
        Locale locale) {

        String siteId = currentSiteId();
        String userId = currentUserId();
        try {
            videoTrainingService.setUserWatchLater(siteId, videoId, userId, watchLater);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage(watchLater ? "video.training.watchLater.added" : "video.training.watchLater.removed", null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
        }

        return "redirect:" + resolveReturnPath(returnTo, "/videos/" + videoId);
    }

    @PostMapping(value = "/videos/{videoId}/favorite/ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> setFavoriteAjax(@PathVariable String videoId,
            @RequestParam(name = "favorite", defaultValue = "true") boolean favorite,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        Map<String, Object> response = new HashMap<>();
        try {
            videoTrainingService.setUserFavorite(siteId, videoId, userId, favorite);
            response.put("success", true);
            response.put("favorite", favorite);
        } catch (SecurityException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.accessDenied", null, resolveLocale(locale)));
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.notFound", null, resolveLocale(locale)));
        }
        return response;
    }

    @PostMapping(value = "/videos/{videoId}/watch-later/ajax", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> setWatchLaterAjax(@PathVariable String videoId,
            @RequestParam(name = "watchLater", defaultValue = "true") boolean watchLater,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        Map<String, Object> response = new HashMap<>();
        try {
            videoTrainingService.setUserWatchLater(siteId, videoId, userId, watchLater);
            response.put("success", true);
            response.put("watchLater", watchLater);
        } catch (SecurityException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.accessDenied", null, resolveLocale(locale)));
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("error", messageSource.getMessage("video.training.notFound", null, resolveLocale(locale)));
        }
        return response;
    }

    @GetMapping(VideoTrainingConstants.FAVORITES_PATH)
    public String favorites(@RequestParam(name = "viewMode", required = false) String viewMode,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir,
            Locale locale,
            RedirectAttributes redirectAttributes,
            Model model) {
        return renderPreferredVideosList(true, VideoTrainingConstants.FAVORITES_PATH, "favorites", "video.training.favorites.title",
                "video.training.favorites.empty", viewMode, query, size, page, offset, sortBy, sortDir,
                locale, redirectAttributes, model);
    }

    @GetMapping(VideoTrainingConstants.WATCH_LATER_PATH)
    public String watchLater(@RequestParam(name = "viewMode", required = false) String viewMode,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir,
            Locale locale,
            RedirectAttributes redirectAttributes,
            Model model) {
        return renderPreferredVideosList(false, VideoTrainingConstants.WATCH_LATER_PATH, "watch-later", "video.training.watchLater.title",
            "video.training.watchLater.empty", viewMode, query, size, page, offset, sortBy, sortDir,
                locale, redirectAttributes, model);
    }

    @GetMapping("/analytics")
    public String analytics(@RequestParam(name = "categoryId", required = false) String categoryId,
            RedirectAttributes redirectAttributes, Locale locale, Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        populateNavigationFlags(model, siteId, userId);
        if (!videoTrainingService.canViewAnalytics(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }
        String normalizedCategoryId = StringUtils.trimToNull(categoryId);
        List<VideoTrainingAnalyticsSummary> rawAnalyticsList = videoTrainingService.getSiteAnalyticsSummary(siteId, normalizedCategoryId);
        List<VideoTrainingAnalyticsSummary> filteredAnalyticsList = new ArrayList<>();
        List<VideoTrainingVideo> videosForAnalytics = new ArrayList<>();
        Map<String, VideoTrainingVideo> videoById = new HashMap<>();
        if (rawAnalyticsList != null) {
            for (VideoTrainingAnalyticsSummary summary : rawAnalyticsList) {
                if (summary != null && StringUtils.isNotBlank(summary.getVideoId())) {
                    Optional<VideoTrainingVideo> videoOpt = videoTrainingService.getVideoById(summary.getVideoId());
                    if (videoOpt.isPresent()) {
                        VideoTrainingVideo video = videoOpt.get();
                        videoById.put(video.getId(), video);
                        videosForAnalytics.add(video);
                        filteredAnalyticsList.add(summary);
                    }
                }
            }
        }
        model.addAttribute("analytics", filteredAnalyticsList);
        model.addAttribute("videoById", videoById);
        model.addAttribute("selectedCategoryId", normalizedCategoryId);
        boolean isUserSite = siteService.isUserSite(siteId);
        Locale effectiveLocale = resolveLocale(locale);
        List<VideoTrainingCategory> analyticsCategoryTree = videoTrainingService.getSelectableCategoryTree(siteId, "", 1, Integer.MAX_VALUE);
        model.addAttribute("categoryTree", analyticsCategoryTree);
        model.addAttribute("analyticsCategoryRows", flattenCategoryRows(analyticsCategoryTree));
        populateVideoPresentationModel(model, videosForAnalytics, siteId, userId, effectiveLocale, isUserSite);
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/analytics";
    }

    @GetMapping("/upload-jobs")
    public String uploadJobs(RedirectAttributes redirectAttributes, Locale locale, Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();

        if (!(videoTrainingService.canManageLibrary(siteId, userId) || videoTrainingService.hasManagePermission(siteId, userId))) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        populateNavigationFlags(model, siteId, userId);
        model.addAttribute("jobs", buildUploadJobViews(siteId, userId, locale));
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/upload-jobs";
    }

    @GetMapping("/categories")
    public String categories(@RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            @RequestParam(name = "editId", required = false) String editId,
            RedirectAttributes redirectAttributes,
            Locale locale,
            Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        String normalizedQuery = StringUtils.trimToEmpty(query);
        long totalCount = videoTrainingService.countTopLevelCategoriesForSite(siteId, userId, normalizedQuery);
        int safeSize = Math.max(1, (int) Math.min(totalCount > 0 ? totalCount : 1L, Integer.MAX_VALUE));
        int safePage = 1;
        List<VideoTrainingCategory> categoryTree = videoTrainingService.getCategoryTree(siteId, normalizedQuery, safePage, safeSize);
        List<CategoryRowView> categoryRows = flattenCategoryRows(categoryTree);
        VideoTrainingCategory editingCategory = StringUtils.isBlank(editId)
                ? null
                : videoTrainingService.getCategoryById(editId).orElse(null);

        populateNavigationFlags(model, siteId, userId);
        model.addAttribute("categoryRows", categoryRows);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("editingCategory", editingCategory);
        model.addAttribute("parentCategories", buildParentCategoryOptions(categoryTree, editingCategory));
        model.addAttribute("q", normalizedQuery);
        model.addAttribute("size", safeSize);
        model.addAttribute("page", safePage);
        populatePagerModel(model, safePage, safeSize, totalCount);
        model.addAttribute("currentPath", "/categories");
        model.addAttribute("currentMenu", "categories");
        model.addAttribute("title", messageSource.getMessage("video.training.categories.title", null, locale));
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return "video-training/categories";
    }

    @PostMapping("/categories")
    public String saveCategory(@RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "parentCategoryId", required = false) String parentCategoryId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        VideoTrainingCategory previousCategory = StringUtils.isNotBlank(id)
                ? videoTrainingService.getCategoryById(id).orElse(null)
                : null;
        VideoTrainingCategory category = previousCategory != null ? previousCategory : new VideoTrainingCategory();
        category.setId(StringUtils.trimToNull(id));
        category.setSiteId(siteId);
        category.setName(StringUtils.trimToEmpty(name));
        category.setParentCategoryId(StringUtils.trimToNull(parentCategoryId));

        try {
            videoTrainingService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.categories.saved", null, locale));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        }

        return buildCategoryRedirect(query, page, size, null);
    }

    @PostMapping(value = "/categories/reorder", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> reorderCategories(@RequestBody List<VideoTrainingCategoryOrderUpdate> categoryOrder) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    messageSource.getMessage("video.training.categories.manage.forbidden", new Object[] { siteId }, resolveLocale(null)));
        }

        videoTrainingService.reorderCategories(siteId, categoryOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", categoryOrder != null ? categoryOrder.size() : 0);
        return response;
    }

    @GetMapping("/categories/{categoryId}/delete")
    public String confirmDeleteCategory(@PathVariable String categoryId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            RedirectAttributes redirectAttributes,
            Locale locale,
            Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        try {
            VideoTrainingCategoryDeleteImpact deleteImpact = videoTrainingService.getCategoryDeleteImpact(categoryId);
            populateNavigationFlags(model, siteId, userId);
            model.addAttribute("deleteImpact", deleteImpact);
            model.addAttribute("q", StringUtils.trimToEmpty(query));
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("title", messageSource.getMessage("video.training.categories.delete.confirm.title", null, locale));
            model.addAttribute("currentPath", "/categories");
            model.addAttribute("currentMenu", "categories");
            model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
            return "video-training/category-delete";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        }

        return buildCategoryRedirect(query, page, size, null);
    }

    @PostMapping("/categories/{categoryId}/delete")
    public String deleteCategory(@PathVariable String categoryId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        if (!videoTrainingService.canManageCategories(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
            return "redirect:/videos";
        }

        try {
            videoTrainingService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("video.training.categories.deleted", null, locale));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidInput", null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        }

        return buildCategoryRedirect(query, page, size, null);
    }

    private void populateNavigationFlags(Model model, String siteId, String userId) {
        boolean canManageAll = videoTrainingService.canManageLibrary(siteId, userId);
        boolean canManageOwn = videoTrainingService.hasManagePermission(siteId, userId);
        model.addAttribute("canManage", canManageAll || canManageOwn);
        model.addAttribute("canApprove", canApprovePublication(siteId, userId));
        model.addAttribute("canManageCategories", videoTrainingService.canManageCategories(siteId, userId));
        model.addAttribute("canAnalytics", videoTrainingService.canViewAnalytics(siteId, userId));
        model.addAttribute("canView", videoTrainingService.hasViewPermission(siteId, userId)
                || canManageAll
                || canManageOwn);
        model.addAttribute("isAdmin", securityService.isSuperUser());
    }

    private List<VideoTrainingCategory> buildVisibleCategoryTree(String siteId, String userId, boolean manageableList,
            boolean canManageAll, boolean canManageOwn) {
        List<VideoTrainingVideo> scopedVideos;
        if (manageableList && canManageAll) {
            scopedVideos = videoTrainingService.getSiteLibrary(siteId);
        } else if (manageableList && canManageOwn) {
            scopedVideos = videoTrainingService.getSiteLibraryForOwner(siteId, userId);
        } else {
            scopedVideos = videoTrainingService.getVisibleVideosForUser(siteId, userId, Instant.now());
        }

        if (scopedVideos.isEmpty()) {
            return Collections.emptyList();
        }

        List<VideoTrainingCategory> fullTree = videoTrainingService.getSelectableCategoryTree(siteId, "", 1, Integer.MAX_VALUE);
        if (fullTree.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Set<String>> videoIdsByCategoryId = new HashMap<>();
        for (VideoTrainingVideo video : scopedVideos) {
            if (video == null || video.getCategories() == null) {
                continue;
            }
            for (VideoTrainingCategory category : video.getCategories()) {
                if (category == null || StringUtils.isBlank(category.getId()) || StringUtils.isBlank(video.getId())) {
                    continue;
                }
                videoIdsByCategoryId.computeIfAbsent(category.getId(), ignored -> new HashSet<>()).add(video.getId());
            }
        }

        List<VideoTrainingCategory> visibleRoots = new ArrayList<>();
        for (VideoTrainingCategory root : fullTree) {
            VisibleCategoryBranch branch = filterVisibleCategoryBranch(root, videoIdsByCategoryId);
            if (branch != null && branch.category != null) {
                visibleRoots.add(branch.category);
            }
        }
        return visibleRoots;
    }

    private VisibleCategoryBranch filterVisibleCategoryBranch(VideoTrainingCategory category, Map<String, Set<String>> videoIdsByCategoryId) {
        if (category == null) {
            return null;
        }

        List<VisibleCategoryBranch> childBranches = new ArrayList<>();
        Set<String> visibleVideoIds = new HashSet<>(videoIdsByCategoryId.getOrDefault(category.getId(), Collections.emptySet()));

        if (category.getChildren() != null) {
            for (VideoTrainingCategory child : category.getChildren()) {
                VisibleCategoryBranch childBranch = filterVisibleCategoryBranch(child, videoIdsByCategoryId);
                if (childBranch != null && childBranch.category != null) {
                    childBranches.add(childBranch);
                    visibleVideoIds.addAll(childBranch.videoIds);
                }
            }
        }

        if (visibleVideoIds.isEmpty()) {
            return null;
        }

        VideoTrainingCategory copy = copyCategory(category);
        List<VideoTrainingCategory> children = new ArrayList<>();
        for (VisibleCategoryBranch childBranch : childBranches) {
            children.add(childBranch.category);
        }
        copy.setChildren(children);
        copy.setHasChildren(!children.isEmpty());
        copy.setVideoCount((long) visibleVideoIds.size());

        return new VisibleCategoryBranch(copy, visibleVideoIds);
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

    private static class VisibleCategoryBranch {
        private final VideoTrainingCategory category;
        private final Set<String> videoIds;

        private VisibleCategoryBranch(VideoTrainingCategory category, Set<String> videoIds) {
            this.category = category;
            this.videoIds = videoIds;
        }
    }

    private void populateCategoryTreeModel(Model model, String siteId) {
        model.addAttribute("categoryTree", videoTrainingService.getCategoryTree(siteId, "", 1, Integer.MAX_VALUE));
    }

    private List<CategoryRowView> flattenCategoryRows(List<VideoTrainingCategory> roots) {
        List<CategoryRowView> rows = new ArrayList<>();
        if (roots == null) {
            return rows;
        }

        for (VideoTrainingCategory root : roots) {
            appendCategoryRow(rows, root, 0);
        }
        return rows;
    }

    private void appendCategoryRow(List<CategoryRowView> rows, VideoTrainingCategory category, int level) {
        if (category == null) {
            return;
        }

        rows.add(new CategoryRowView(category, level));
        if (category.getChildren() != null) {
            for (VideoTrainingCategory child : category.getChildren()) {
                appendCategoryRow(rows, child, level + 1);
            }
        }
    }

    private List<VideoTrainingCategory> buildParentCategoryOptions(List<VideoTrainingCategory> roots, VideoTrainingCategory editingCategory) {
        if (roots == null) {
            return Collections.emptyList();
        }

        List<VideoTrainingCategory> parents = new ArrayList<>();
        for (VideoTrainingCategory root : roots) {
            if (editingCategory != null && Objects.equals(editingCategory.getId(), root.getId())) {
                continue;
            }
            parents.add(root);
        }
        return parents;
    }

    private void populateCategoryModel(Model model, String siteId, VideoTrainingVideo video) {
        List<VideoTrainingCategory> categoryTree = videoTrainingService.getSelectableCategoryTree(siteId, "", 1, Integer.MAX_VALUE);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("selectedCategoryIds", video == null || video.getCategories() == null
                ? Collections.emptyList()
                : video.getCategories().stream().map(VideoTrainingCategory::getId).toList());
    }

    private VideoTrainingVideo saveVideoWithOptionalCategories(VideoTrainingVideo video, List<String> categoryIds, boolean categoriesProvided) {
        if (categoriesProvided) {
            return videoTrainingService.saveVideoWithCategoryIds(video, categoryIds == null ? Collections.emptyList() : categoryIds);
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return videoTrainingService.saveVideoWithCategoryIds(video, categoryIds);
        }
        return videoTrainingService.saveVideo(video);
    }

    private void preserveFormState(RedirectAttributes redirectAttributes,
            String title,
            String description,
            String providerType,
            String sourceMode,
            String sourceReference,
            String existingResourceReference,
            String inheritTitleMetadata,
            String inheritDescriptionMetadata,
            String visibilityScope,
            String publicationStatus,
            String releaseDate,
            String retractDate,
            List<String> categoryIds,
            String categoryIdsProvided) {

        VideoTrainingVideo temp = new VideoTrainingVideo();
        temp.setTitle(StringUtils.defaultString(title));
        temp.setDescription(StringUtils.defaultString(description));
        temp.setInheritTitleMetadata(StringUtils.isNotBlank(inheritTitleMetadata));
        temp.setInheritDescriptionMetadata(StringUtils.isNotBlank(inheritDescriptionMetadata));

        try {
            VideoProviderType parsedProvider = resolveProviderTypeForSourceMode(resolveSourceMode(sourceMode, providerType), providerType);
            temp.setProviderType(parsedProvider);
        } catch (Exception ignored) {
        }

        String resolvedRef = StringUtils.trimToEmpty(sourceReference);
        if (StringUtils.isBlank(resolvedRef)) {
            resolvedRef = StringUtils.trimToEmpty(existingResourceReference);
        }
        temp.setSourceReference(resolvedRef);

        try {
            temp.setVisibilityScope(parseVisibilityScope(visibilityScope));
        } catch (Exception ignored) {
        }

        try {
            temp.setPublicationStatus(parsePublicationStatus(publicationStatus));
        } catch (Exception ignored) {
        }

        redirectAttributes.addFlashAttribute("video", temp);
        redirectAttributes.addFlashAttribute("sourceMode", resolveSourceMode(sourceMode, providerType));
        redirectAttributes.addFlashAttribute("releaseDateInput", StringUtils.defaultString(releaseDate));
        redirectAttributes.addFlashAttribute("retractDateInput", StringUtils.defaultString(retractDate));
        redirectAttributes.addFlashAttribute("selectedCategoryIds", categoryIds == null ? Collections.emptyList() : categoryIds);
    }

    private VideoTrainingVideo copyVideo(VideoTrainingVideo source) {
        if (source == null) {
            return null;
        }

        VideoTrainingVideo copy = new VideoTrainingVideo();
        copy.setId(source.getId());
        copy.setSiteId(source.getSiteId());
        copy.setOwnerId(source.getOwnerId());
        copy.setTitle(source.getTitle());
        copy.setInheritTitleMetadata(source.isInheritTitleMetadata());
        copy.setInheritDescriptionMetadata(source.isInheritDescriptionMetadata());
        copy.setDescription(source.getDescription());
        copy.setProviderType(source.getProviderType());
        copy.setSourceReference(source.getSourceReference());
        copy.setSourceDeleted(source.isSourceDeleted());
        copy.setFileSizeBytes(source.getFileSizeBytes());
        copy.setVisibilityScope(source.getVisibilityScope());
        copy.setPublicationStatus(source.getPublicationStatus());
        copy.setReleaseDate(source.getReleaseDate());
        copy.setRetractDate(source.getRetractDate());
        copy.setRequiredViewPermission(source.getRequiredViewPermission());
        copy.setCreatedOn(source.getCreatedOn());
        copy.setModifiedOn(source.getModifiedOn());
        copy.setCategories(source.getCategories() == null ? new HashSet<>() : new HashSet<>(source.getCategories()));
        return copy;
    }

    private String buildCategoryRedirect(String query, int page, int size, String editId) {
        StringBuilder target = new StringBuilder("redirect:/categories");
        List<String> parameters = new ArrayList<>();
        if (StringUtils.isNotBlank(query)) {
            parameters.add("q=" + urlEncode(query));
        }
        if (page > 0) {
            parameters.add("page=" + page);
        }
        if (size > 0) {
            parameters.add("size=" + size);
        }
        if (StringUtils.isNotBlank(editId)) {
            parameters.add("editId=" + urlEncode(editId));
        }
        if (!parameters.isEmpty()) {
            target.append('?').append(String.join("&", parameters));
        }
        return target.toString();
    }

    private String buildAbsoluteUrl(HttpServletRequest request, String path) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (!("http".equalsIgnoreCase(request.getScheme()) && port == 80)
                && !("https".equalsIgnoreCase(request.getScheme()) && port == 443)) {
            url.append(":").append(port);
        }
        url.append(request.getContextPath());
        if (!path.startsWith("/")) {
            url.append('/');
        }
        url.append(path);
        return url.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private List<UploadJobView> buildUploadJobViews(String siteId, String userId, Locale locale) {
        List<UploadJobView> jobViews = new ArrayList<>();
        for (VideoTrainingProcessJob job : processJobService.findBySubmitterUserIdOrderByModifiedOnDesc(userId)) {
            VideoTrainingVideo video = videoTrainingService.getVideoById(job.getVideoId()).orElse(null);
            if (video == null || !Objects.equals(video.getSiteId(), siteId)) {
                continue;
            }

            jobViews.add(new UploadJobView(
                    job,
                    video,
                    formatInstantForDisplay(job.getCreatedOn(), locale),
                    formatInstantForDisplay(job.getModifiedOn(), locale),
                    localizeJobStatus(job.getStatus(), locale)));
        }
        return jobViews;
    }

    private String localizeJobStatus(VideoTrainingProcessJobStatus status, Locale locale) {
        if (status == null) {
            return "";
        }
        String key = "video.training.job.status." + status.name();
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception ex) {
            return status.name();
        }
    }

    private String renderPreferredVideosList(boolean favoritesOnly, String listPath, String menuCurrent,
            String titleMessageKey, String emptyMessageKey,
            String viewMode, String query, int size, Integer page, Integer offset, String sortBy, String sortDir,
            Locale locale, RedirectAttributes redirectAttributes, Model model) {
        String siteId = currentSiteId();
        String userId = currentUserId();
        Locale effectiveLocale = resolveLocale(locale);

        if (!videoTrainingService.hasViewPermission(siteId, userId)
            && !videoTrainingService.canManageLibrary(siteId, userId)
            && !videoTrainingService.hasManagePermission(siteId, userId)) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, effectiveLocale));
            return "redirect:/videos";
        }

        boolean isUserSite = siteService.isUserSite(siteId);
        boolean canManageAll = videoTrainingService.canManageLibrary(siteId, userId);
        boolean canManageOwn = videoTrainingService.hasManagePermission(siteId, userId);
        boolean canManage = canManageAll || canManageOwn;
        String normalizedQuery = StringUtils.trimToEmpty(query);
        int safeSize = normalizePageSize(size);
        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortDir = normalizeSortDir(sortDir);
        String sortField = mapSortField(normalizedSortBy, isUserSite);
        boolean ascending = "asc".equals(normalizedSortDir);
        String effectiveViewMode = resolveEffectiveViewMode(siteId, canManage, viewMode);

        List<VideoTrainingVideo> preferredVideos = favoritesOnly
                ? videoTrainingService.getUserFavoriteVideos(siteId, userId, Instant.now())
                : videoTrainingService.getUserWatchLaterVideos(siteId, userId, Instant.now());
        List<VideoTrainingVideo> filteredVideos = new ArrayList<>();
        for (VideoTrainingVideo video : preferredVideos) {
            if (matchesSearchText(video, normalizedQuery)) {
                filteredVideos.add(video);
            }
        }
        filteredVideos.sort((left, right) -> comparePreferredVideos(left, right, sortField, ascending));

        long totalCount = filteredVideos.size();
        int requestedPage = page != null ? page : ((offset != null ? offset : 0) / safeSize) + 1;
        int safePage = normalizePage(requestedPage, safeSize, totalCount);
        int pageOffset = (safePage - 1) * safeSize;
        int endIndex = Math.min(filteredVideos.size(), pageOffset + safeSize);
        List<VideoTrainingVideo> videos = pageOffset >= filteredVideos.size()
                ? new ArrayList<>()
                : new ArrayList<>(filteredVideos.subList(pageOffset, endIndex));
        int totalPages = (int) Math.max(1, Math.ceil((double) totalCount / safeSize));

        populateNavigationFlags(model, siteId, userId);
        populateVideoPresentationModel(model, videos, siteId, userId, effectiveLocale, isUserSite);
        model.addAttribute("videos", videos);
        model.addAttribute("isUserSite", isUserSite);
        model.addAttribute("viewMode", effectiveViewMode);
        model.addAttribute("isCardsView", VideoTrainingConstants.VIEW_MODE_CARDS.equals(effectiveViewMode));
        model.addAttribute("isTableView", VideoTrainingConstants.VIEW_MODE_TABLE.equals(effectiveViewMode));
        model.addAttribute("q", normalizedQuery);
        model.addAttribute("size", safeSize);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("offset", pageOffset);
        model.addAttribute("sortBy", normalizedSortBy);
        model.addAttribute("sortDir", normalizedSortDir);
        model.addAttribute("siteId", siteId);
        model.addAttribute("siteRef", siteService.siteReference(siteId));
        model.addAttribute("currentPath", listPath);
        model.addAttribute("listPath", listPath);
        model.addAttribute("currentMenu", menuCurrent);
        model.addAttribute("showManageColumns", true);
        model.addAttribute("showManageButtons", false);
        model.addAttribute("showAccessModeSwitch", false);
        model.addAttribute("isManageableList", false);
        model.addAttribute("isViewableList", false);
        model.addAttribute("accessMode", menuCurrent);
        populatePagerModel(model, safePage, safeSize, totalCount);
        model.addAttribute("title", messageSource.getMessage(titleMessageKey, null, effectiveLocale));
        model.addAttribute("emptyMessage", messageSource.getMessage(emptyMessageKey, null, effectiveLocale));
        model.addAttribute("isGlobalSite", isGlobalSiteId(siteId));
        return favoritesOnly ? "video-training/favorites" : "video-training/watch-later";
    }

    private boolean matchesSearchText(VideoTrainingVideo video, String searchText) {
        if (video == null || StringUtils.isBlank(searchText)) {
            return true;
        }

        String normalized = StringUtils.lowerCase(StringUtils.trimToEmpty(searchText));
        return StringUtils.contains(StringUtils.lowerCase(StringUtils.defaultString(video.getTitle())), normalized)
                || StringUtils.contains(StringUtils.lowerCase(StringUtils.defaultString(video.getDescription())), normalized)
                || StringUtils.contains(StringUtils.lowerCase(StringUtils.defaultString(video.getSourceReference())), normalized);
    }

    private int comparePreferredVideos(VideoTrainingVideo left, VideoTrainingVideo right, String sortField, boolean ascending) {
        int primaryComparison = comparePreferredVideoField(left, right, sortField, ascending);
        if (primaryComparison != 0) {
            return primaryComparison;
        }

        int modifiedComparison = compareNullable(left.getModifiedOn(), right.getModifiedOn(), false);
        if (modifiedComparison != 0) {
            return modifiedComparison;
        }

        return compareNullable(left.getId(), right.getId(), false);
    }

    private int comparePreferredVideoField(VideoTrainingVideo left, VideoTrainingVideo right, String sortField, boolean ascending) {
        switch (sortField) {
            case "title":
                return compareNullable(left.getTitle(), right.getTitle(), ascending);
            case "siteId":
                return compareNullable(left.getSiteId(), right.getSiteId(), ascending);
            case "providerType":
                return compareNullable(enumName(left.getProviderType()), enumName(right.getProviderType()), ascending);
            case "visibilityScope":
                return compareNullable(enumName(left.getVisibilityScope()), enumName(right.getVisibilityScope()), ascending);
            case "publicationStatus":
                return compareNullable(enumName(left.getPublicationStatus()), enumName(right.getPublicationStatus()), ascending);
            case "releaseDate":
                return compareNullable(left.getReleaseDate(), right.getReleaseDate(), ascending);
            case "retractDate":
                return compareNullable(left.getRetractDate(), right.getRetractDate(), ascending);
            case "modifiedOn":
            default:
                return compareNullable(left.getModifiedOn(), right.getModifiedOn(), ascending);
        }
    }

    private int compareNullable(String left, String right, boolean ascending) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }

        int comparison = String.CASE_INSENSITIVE_ORDER.compare(left, right);
        return ascending ? comparison : -comparison;
    }

    private <T extends Comparable<? super T>> int compareNullable(T left, T right, boolean ascending) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }

        int comparison = left.compareTo(right);
        return ascending ? comparison : -comparison;
    }

    private String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    private String resolveReturnPath(String returnTo, String fallback) {
        String candidate = StringUtils.trimToEmpty(returnTo);
        if (StringUtils.isBlank(candidate)) {
            return fallback;
        }
        if (!candidate.startsWith("/") || candidate.startsWith("//")) {
            return fallback;
        }
        return candidate;
    }

    public static class CategoryRowView {
        private final VideoTrainingCategory category;
        private final int level;

        public CategoryRowView(VideoTrainingCategory category, int level) {
            this.category = category;
            this.level = level;
        }

        public VideoTrainingCategory getCategory() {
            return category;
        }

        public int getLevel() {
            return level;
        }
    }

    private void populateVideoPresentationModel(Model model,
            List<VideoTrainingVideo> videos,
            String siteId,
            String userId,
            Locale locale,
            boolean isUserSite) {

        Map<String, String> releaseDisplayById = new HashMap<>();
        Map<String, String> retractDisplayById = new HashMap<>();
        Map<String, String> thumbnailUrlById = new HashMap<>();
        Map<String, Boolean> thumbnailIsVideoById = new HashMap<>();
        Map<String, String> siteNameBySiteId = new HashMap<>();
        Map<String, List<String>> videoCategoryNamesById = new HashMap<>();

        for (VideoTrainingVideo video : videos) {
            releaseDisplayById.put(video.getId(), formatInstantForDisplay(video.getReleaseDate(), locale));
            retractDisplayById.put(video.getId(), formatInstantForDisplay(video.getRetractDate(), locale));
            List<String> categoryNames = new ArrayList<>();
            if (video.getCategories() != null) {
                video.getCategories().stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(category -> StringUtils.lowerCase(StringUtils.defaultString(category.getName()))))
                        .forEach(category -> categoryNames.add(category.getName()));
            }
            videoCategoryNamesById.put(video.getId(), categoryNames);
            String thumbUrl = buildThumbnailUrl(video);
            boolean isVideoFormat = isNativeVideoThumbnail(video);
            if (!isVideoFormat && StringUtils.isNotBlank(thumbUrl)) {
                int lastDot = thumbUrl.lastIndexOf('.');
                if (lastDot != -1 && lastDot < thumbUrl.length() - 1) {
                    String extension = thumbUrl.substring(lastDot + 1).toLowerCase();
                    if (VideoTrainingConstants.ALLOWED_NATIVE_VIDEO_EXTENSIONS.contains(extension)) {
                        isVideoFormat = true;
                    }
                }
            }
            if (video.getProviderType() == VideoProviderType.HLS_UPLOAD && thumbUrl != null && thumbUrl.contains("master.m3u8")) {
                thumbUrl = thumbUrl.replace("master.m3u8", "thumbnail.jpg");
                isVideoFormat = false;
            }
            thumbnailUrlById.put(video.getId(), thumbUrl);
            thumbnailIsVideoById.put(video.getId(), isVideoFormat);
            if (isUserSite && !siteNameBySiteId.containsKey(video.getSiteId())) {
                siteNameBySiteId.put(video.getSiteId(), getSiteName(video.getSiteId()));
            }
        }

        Map<String, Boolean> isFavoriteById = new HashMap<>();
        Map<String, Boolean> isWatchLaterById = new HashMap<>();
        List<String> videoIds = videos.stream().map(VideoTrainingVideo::getId).toList();
        videoTrainingService.getUserVideoPreferences(siteId, userId, videoIds)
                .forEach((videoId, pref) -> {
                    isFavoriteById.put(videoId, pref.isFavorite());
                    isWatchLaterById.put(videoId, pref.isWatchLater());
                });

        model.addAttribute("videos", videos);
        model.addAttribute("releaseDisplayById", releaseDisplayById);
        model.addAttribute("retractDisplayById", retractDisplayById);
        model.addAttribute("thumbnailUrlById", thumbnailUrlById);
        model.addAttribute("thumbnailIsVideoById", thumbnailIsVideoById);
        model.addAttribute("isFavoriteById", isFavoriteById);
        model.addAttribute("isWatchLaterById", isWatchLaterById);
        model.addAttribute("siteNameBySiteId", siteNameBySiteId);
        model.addAttribute("videoCategoryNamesById", videoCategoryNamesById);
    }

    private String currentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

    private String currentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    private ZoneId getUserZoneId() {
        TimeZone timeZone = userTimeService.getLocalTimeZone();
        return timeZone != null ? timeZone.toZoneId() : ZoneOffset.UTC;
    }

    private Instant parseInputDateTime(String input) {
        String trimmedInput = StringUtils.trimToNull(input);
        if (trimmedInput == null) {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.parse(trimmedInput);
        return localDateTime.atZone(getUserZoneId()).toInstant();
    }

    private String formatInstantForInput(Instant instant) {
        if (instant == null) {
            return "";
        }
        return LocalDateTime.ofInstant(instant, getUserZoneId())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    private String formatInstantForDisplay(Instant instant, Locale locale) {
        if (instant == null) {
            return "";
        }
        return userTimeService.shortLocalizedTimestamp(instant, resolveLocale(locale));
    }

    private boolean isValidVisibilityWindow(Instant releaseDate, Instant retractDate) {
        return releaseDate == null || retractDate == null || releaseDate.isBefore(retractDate);
    }

    private String resolveSourceMode(String sourceMode, String providerType) {
        String normalizedSourceMode = StringUtils.trimToEmpty(sourceMode);
        if (VideoTrainingConstants.SOURCE_MODE_EXTERNAL.equals(normalizedSourceMode)
                || VideoTrainingConstants.SOURCE_MODE_UPLOAD.equals(normalizedSourceMode)
                || VideoTrainingConstants.SOURCE_MODE_RESOURCES.equals(normalizedSourceMode)) {
            return normalizedSourceMode;
        }

        String normalizedProviderType = StringUtils.trimToEmpty(providerType);
        if ("EXTERNAL".equals(normalizedProviderType)) {
            return VideoTrainingConstants.SOURCE_MODE_EXTERNAL;
        }
        if ("RESOURCES".equals(normalizedProviderType)) {
            return VideoTrainingConstants.SOURCE_MODE_RESOURCES;
        }

        return VideoTrainingConstants.SOURCE_MODE_UPLOAD;
    }

    private VideoVisibilityScope parseVisibilityScope(String visibilityScope) {
        String value = StringUtils.trimToEmpty(visibilityScope);
        if (StringUtils.isBlank(value)) {
            return VideoVisibilityScope.COURSE;
        }
        return VideoVisibilityScope.valueOf(value);
    }

    private VideoPublicationStatus parsePublicationStatus(String publicationStatus) {
        String value = StringUtils.trimToEmpty(publicationStatus);
        if (StringUtils.isBlank(value)) {
            return VideoPublicationStatus.DRAFT;
        }
        return VideoPublicationStatus.valueOf(value);
    }

    private MetadataFetchResult resolveNativeUploadMetadata(MultipartFile nativeFile) {
        if (nativeFile == null || nativeFile.isEmpty()) {
            return new MetadataFetchResult("", "");
        }

        String originalFilename = StringUtils.defaultString(nativeFile.getOriginalFilename());
        String title = StringUtils.defaultIfBlank(StringUtils.substringBeforeLast(originalFilename, "."), originalFilename);
        return new MetadataFetchResult(title, "");
    }

    private String updatePublicationStatus(String videoId,
            VideoPublicationStatus status,
            String successMessageKey,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.notFound", null, locale));
            return "redirect:/videos";
        }

        try {
            VideoTrainingVideo savedVideo = updatePublicationStatus(videoId, status);
            videoTrainingService.syncExternalProviderPrivacy(savedVideo);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage(successMessageKey, null, locale));
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.accessDenied", null, locale));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("video.training.invalidStatusTransition", null, locale));
        } catch (IllegalStateException ex) {
            // e.g. attempting to publish when source is unavailable
            String resolved = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
            redirectAttributes.addFlashAttribute("error", resolved);
        }

        return "redirect:/videos";
    }

    private VideoTrainingVideo updatePublicationStatus(String videoId, VideoPublicationStatus status) {
        VideoTrainingVideo existing = videoTrainingService.getVideoById(videoId).orElseThrow(() -> new IllegalArgumentException("Unknown videoId"));
        String userId = currentUserId();
        boolean canManageAll = videoTrainingService.canManageLibrary(existing.getSiteId(), userId);
        boolean canManageOwn = videoTrainingService.hasManagePermission(existing.getSiteId(), userId)
                && Objects.equals(existing.getOwnerId(), userId);
        if (!canManageAll && !canManageOwn) {
            throw new SecurityException("User cannot manage video " + videoId);
        }

        if ((status == VideoPublicationStatus.PUBLISHED || status == VideoPublicationStatus.DRAFT)
            && isModerationEnabled()
                && existing.getPublicationStatus() == VideoPublicationStatus.PENDING_APPROVAL
                && !canApprovePublication(existing.getSiteId(), userId)) {
            throw new SecurityException("User cannot approve publication for video " + videoId);
        }

        return videoTrainingService.updateVideoStatus(videoId, status);
    }

    private VideoPublicationStatus[] filterPublicationStatusesForUser(String siteId, String userId,
            VideoPublicationStatus[] publicationStatuses) {
        if (publicationStatuses == null || publicationStatuses.length == 0) {
            return publicationStatuses;
        }

        if (!isModerationEnabled() || canApprovePublication(siteId, userId)) {
            return publicationStatuses;
        }

        List<VideoPublicationStatus> filtered = new ArrayList<>();
        for (VideoPublicationStatus publicationStatus : publicationStatuses) {
            if (publicationStatus != VideoPublicationStatus.PUBLISHED) {
                filtered.add(publicationStatus);
            }
        }
        return filtered.toArray(VideoPublicationStatus[]::new);
    }

    private boolean isModerationEnabled() {
        return serverConfigurationService.getBoolean(VideoTrainingConstants.MODERATION_ENABLED_PROPERTY, VideoTrainingConstants.DEFAULT_MODERATION_ENABLED);
    }

    private boolean canApprovePublication(String siteId, String userId) {
        return securityService.isSuperUser()
                || securityService.unlock(userId, VideoTrainingConstants.PERMISSION_APPROVE_PUBLISH, siteService.siteReference(siteId));
    }

    private boolean canUseGlobalVisibility(String siteId, String userId) {
        return securityService.isSuperUser()
            || securityService.unlock(userId, VideoTrainingConstants.PERMISSION_GLOBAL, siteService.siteReference(siteId));
    }

    private List<VideoVisibilityScope> getVisibilityScopesForUser(String siteId, String userId) {
        List<VideoVisibilityScope> scopes = new ArrayList<>();
        for (VideoVisibilityScope scope : VideoVisibilityScope.values()) {
            if (scope != VideoVisibilityScope.GLOBAL || canUseGlobalVisibility(siteId, userId)) {
                scopes.add(scope);
            }
        }
        return scopes;
    }

    private VideoProviderType resolveProviderTypeForSourceMode(String sourceMode, String providerType) {
        if (VideoTrainingConstants.SOURCE_MODE_UPLOAD.equals(sourceMode)) {
            VideoProviderType requestedUploadType = resolveUploadProviderType(providerType);
            if (requestedUploadType != null) {
                return requestedUploadType;
            }
        }

        if (VideoTrainingConstants.SOURCE_MODE_EXTERNAL.equals(sourceMode)) {
            return VideoProviderType.EXTERNAL;
        }
        if (VideoTrainingConstants.SOURCE_MODE_RESOURCES.equals(sourceMode)) {
            return VideoProviderType.RESOURCES;
        }
        return uploadService.isHlsUploadEnabled() ? VideoProviderType.HLS_UPLOAD : VideoProviderType.NATIVE;
    }

    private VideoProviderType resolveUploadProviderType(String providerType) {
        String normalizedProviderType = StringUtils.trimToEmpty(providerType).toUpperCase(Locale.ROOT);
        if (StringUtils.equals(normalizedProviderType, VideoProviderType.YOUTUBE_UPLOAD.name())) {
            return VideoProviderType.YOUTUBE_UPLOAD;
        }
        if (StringUtils.equals(normalizedProviderType, VideoProviderType.HLS_UPLOAD.name())) {
            return VideoProviderType.HLS_UPLOAD;
        }
        if (StringUtils.equals(normalizedProviderType, VideoProviderType.NATIVE.name())) {
            return VideoProviderType.NATIVE;
        }
        return null;
    }

    private String resolveEffectiveViewMode(String siteId, boolean canManage, String requestedViewMode) {
        String sessionKey = VideoTrainingConstants.VIEW_MODE_SESSION_PREFIX + siteId;
        if (isValidViewMode(requestedViewMode)) {
            sessionManager.getCurrentSession().setAttribute(sessionKey, requestedViewMode);
            return requestedViewMode;
        }

        Object sessionValue = sessionManager.getCurrentSession().getAttribute(sessionKey);
        if (sessionValue instanceof String storedViewMode && isValidViewMode(storedViewMode)) {
            return storedViewMode;
        }

        return canManage ? VideoTrainingConstants.VIEW_MODE_TABLE : VideoTrainingConstants.VIEW_MODE_CARDS;
    }

    private int normalizePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            return VideoTrainingConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(VideoTrainingConstants.MAX_PAGE_SIZE, requestedSize);
    }

    private int normalizePage(int requestedPage, int pageSize, long totalCount) {
        int safePage = Math.max(requestedPage, 1);
        if (totalCount <= 0) {
            return 1;
        }

        int totalPages = (int) Math.max(1, Math.ceil((double) totalCount / pageSize));
        return Math.min(safePage, totalPages);
    }

    private void populatePagerModel(Model model, int page, int pageSize, long totalCount) {
        int totalPages = (int) Math.max(1, Math.ceil((double) totalCount / pageSize));
        int safePage = Math.max(1, Math.min(page, totalPages));
        long topMsgPos = totalCount == 0 ? 0 : ((long) (safePage - 1) * pageSize) + 1;
        long btmMsgPos = totalCount == 0 ? 0 : Math.min(totalCount, (long) safePage * pageSize);

        List<Integer> pageSizes = new ArrayList<>(List.of(5, 10, 15, 20, 24, 50, 100));
        if (!pageSizes.contains(pageSize)) {
            pageSizes.add(pageSize);
            Collections.sort(pageSizes);
        }

        model.addAttribute("allMsgNumber", totalCount);
        model.addAttribute("topMsgPos", topMsgPos);
        model.addAttribute("btmMsgPos", btmMsgPos);
        model.addAttribute("goFPButton", safePage > 1);
        model.addAttribute("goPPButton", safePage > 1);
        model.addAttribute("goNPButton", safePage < totalPages);
        model.addAttribute("goLPButton", safePage < totalPages);
        model.addAttribute("pagesizes", pageSizes);
        model.addAttribute("pagesize", pageSize);
        model.addAttribute("totalPages", totalPages);
    }

    private boolean isValidViewMode(String viewMode) {
        return VideoTrainingConstants.VIEW_MODE_CARDS.equals(viewMode) || VideoTrainingConstants.VIEW_MODE_TABLE.equals(viewMode);
    }

    private String buildThumbnailUrl(VideoTrainingVideo video) {
        if (video == null) {
            return "";
        }
        try {
            if (videoTrainingService != null) {
                return StringUtils.defaultString(videoTrainingService.resolveThumbnailUrl(video), "");
            }
        } catch (Exception e) {
            // fallthrough to empty
        }
        return "";
    }

    private boolean isNativeVideoThumbnail(VideoTrainingVideo video) {
        return video != null
                && (video.getProviderType() == VideoProviderType.NATIVE || video.getProviderType() == VideoProviderType.RESOURCES)
                && StringUtils.isNotBlank(video.getSourceReference());
    }

    private String normalizeExternalSourceReference(String sourceReference) {
        String normalized = StringUtils.trimToEmpty(sourceReference);
        if (StringUtils.isBlank(normalized)) {
            return "";
        }
        normalized = normalized.replace("&amp;", "&");

        String youtubeVideoId = externalMetadataService.extractVideoId(normalized);
        if (StringUtils.isNotBlank(youtubeVideoId)) {
            return "https://www.youtube.com/embed/" + youtubeVideoId;
        }

        return "";
    }

    private String determineSourceMode(VideoTrainingVideo video, List<SiteVideoResourceOption> existingResources) {
        if (video == null) {
            return VideoTrainingConstants.SOURCE_MODE_UPLOAD;
        }

        if (video.getProviderType() == VideoProviderType.EXTERNAL) {
            return VideoTrainingConstants.SOURCE_MODE_EXTERNAL;
        }

        if (video.getProviderType() == VideoProviderType.RESOURCES) {
            return VideoTrainingConstants.SOURCE_MODE_RESOURCES;
        }

        if (uploadService.isManagedUploadProvider(video.getProviderType())) {
            return VideoTrainingConstants.SOURCE_MODE_UPLOAD;
        }

        String sourceReference = StringUtils.trimToEmpty(video.getSourceReference());
        for (SiteVideoResourceOption option : existingResources) {
            if (StringUtils.equals(option.getReference(), sourceReference)) {
                return VideoTrainingConstants.SOURCE_MODE_RESOURCES;
            }
        }

        return VideoTrainingConstants.SOURCE_MODE_UPLOAD;
    }

    private static class UploadJobView {

        private final VideoTrainingProcessJob job;
        private final VideoTrainingVideo video;
        private final String createdOnDisplay;
        private final String modifiedOnDisplay;
        private final String statusLabel;

        private UploadJobView(VideoTrainingProcessJob job,
                VideoTrainingVideo video,
                String createdOnDisplay,
                String modifiedOnDisplay,
                String statusLabel) {
            this.job = job;
            this.video = video;
            this.createdOnDisplay = createdOnDisplay;
            this.modifiedOnDisplay = modifiedOnDisplay;
            this.statusLabel = statusLabel;
        }

        public VideoTrainingProcessJob getJob() {
            return job;
        }

        public VideoTrainingVideo getVideo() {
            return video;
        }

        public String getCreatedOnDisplay() {
            return createdOnDisplay;
        }

        public String getModifiedOnDisplay() {
            return modifiedOnDisplay;
        }

        public String getStatusLabel() {
            return statusLabel;
        }

        public String getErrorMessage() {
            return job.getErrorMessage();
        }

        public String getStatus() {
            return job.getStatus() != null ? job.getStatus().name() : "";
        }

        public String getJobId() {
            return job.getId();
        }
    }

    private String getSiteName(String siteId) {
        try {
            return siteService.getSite(siteId).getTitle();
        } catch (Exception e) {
            return siteId;
        }
    }

    private List<VideoTrainingVideoView> buildVideoViews(
            List<VideoTrainingVideo> videos,
            boolean isUserSite,
            Locale locale) {
        List<VideoTrainingVideoView> views = new ArrayList<>();

        for (VideoTrainingVideo video : videos) {
            views.add(VideoTrainingVideoView.builder()
                    .id(video.getId())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .siteName(isUserSite ? getSiteName(video.getSiteId()) : null)
                    .releaseDisplay(formatInstantForDisplay(video.getReleaseDate(), locale))
                    .retractDisplay(formatInstantForDisplay(video.getRetractDate(), locale))
                    .thumbnailUrl(buildThumbnailUrl(video))
                    .thumbnailIsVideo(isNativeVideoThumbnail(video))
                    .visibilityScope(
                        Optional.ofNullable(video.getVisibilityScope())
                                .map(Enum::name)
                                .orElse("COURSE"))
                    .publicationStatus(
                        Optional.ofNullable(video.getPublicationStatus())
                                .map(Enum::name)
                                .orElse("DRAFT"))
                    .build());
        }

        return views;
    }

}
