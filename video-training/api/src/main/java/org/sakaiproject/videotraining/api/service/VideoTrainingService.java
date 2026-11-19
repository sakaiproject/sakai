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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

public interface VideoTrainingService {

    VideoTrainingVideo updateVideoVisibility(String videoId, VideoVisibilityScope newScope);

    VideoTrainingVideo updateVideoStatus(String videoId, VideoPublicationStatus newStatus);

    VideoTrainingVideo updateVideoSchedule(String videoId, Instant releaseDate, Instant retractDate);

    VideoTrainingVideo saveVideo(VideoTrainingVideo video);

    VideoTrainingVideo saveVideoWithCategoryIds(VideoTrainingVideo video, List<String> categoryIds);

    Optional<VideoTrainingVideo> getVideoById(String videoId);

    void deleteVideo(String videoId);

    List<VideoTrainingVideo> getSiteLibrary(String siteId);

    List<VideoTrainingVideo> getSiteLibraryPage(String siteId, String searchText, int page, int size);

    List<VideoTrainingVideo> getSiteLibraryPage(String siteId, String searchText, String categoryId, int page, int size);

    List<VideoTrainingVideo> getSiteLibraryPageForOwner(String siteId, String ownerId, String searchText, int page, int size);

    List<VideoTrainingVideo> getSiteLibraryPageForOwner(String siteId, String ownerId, String searchText, String categoryId, int page, int size);

    List<VideoTrainingVideo> getSiteLibraryForOwner(String siteId, String ownerId);

    List<VideoTrainingVideo> getSiteLibraryCursor(String siteId, String searchText, Instant cursorModifiedOn, String cursorVideoId, int size);

    List<VideoTrainingVideo> getSiteLibrarySorted(String siteId, String searchText, int offset, int size, String sortField, boolean ascending);

    List<VideoTrainingVideo> getSiteLibrarySorted(String siteId, String searchText, String categoryId, int offset, int size, String sortField, boolean ascending);

    List<VideoTrainingVideo> getSiteLibrarySortedForOwner(String siteId, String ownerId, String searchText, int offset, int size, String sortField, boolean ascending);

    List<VideoTrainingVideo> getSiteLibrarySortedForOwner(String siteId, String ownerId, String searchText, String categoryId, int offset, int size, String sortField, boolean ascending);

    long countSiteLibrary(String siteId, String searchText);

    long countSiteLibrary(String siteId, String searchText, String categoryId);

    long countSiteLibraryForOwner(String siteId, String ownerId, String searchText);

    long countSiteLibraryForOwner(String siteId, String ownerId, String searchText, String categoryId);

    long countSiteViewableVideosForUser(String siteId, String userId, String searchText);

    long countGlobalVideos(String searchText);

    List<VideoTrainingVideo> getVisibleVideosForUser(String siteId, String userId, Instant now);

    List<VideoTrainingVideo> getVisibleVideosForUserPage(String siteId, String userId, Instant now, String searchText, int page, int size);

    List<VideoTrainingVideo> getVisibleVideosForUserPage(String siteId, String userId, Instant now, String searchText, String categoryId, int page, int size);

    List<VideoTrainingVideo> getVisibleVideosForUserSorted(String siteId, String userId, Instant now, String searchText, int offset, int size, String sortField, boolean ascending);

    List<VideoTrainingVideo> getVisibleVideosForUserSorted(String siteId, String userId, Instant now, String searchText, String categoryId, int offset, int size, String sortField, boolean ascending);

    List<VideoTrainingVideo> getVisibleVideosForUserCursor(String siteId, String userId, Instant now, String searchText, Instant cursorModifiedOn, String cursorVideoId, int size);

    long countVisibleVideosForUser(String siteId, String userId, Instant now, String searchText);

    long countVisibleVideosForUser(String siteId, String userId, Instant now, String searchText, String categoryId);

    long countGlobalVideosForUser(String userId, String searchText);

    long countSiteVideosForUser(String siteId, String userId, String searchText);

    long countCategoriesForSite(String siteId, String userId);

    long countTopLevelCategoriesForSite(String siteId, String userId, String searchText);

    List<VideoTrainingVideo> getVisibleGlobalVideosPage(String searchText, int page, int size);

    List<VideoTrainingVideo> getGlobalVideosForUser(String userId, String searchText, int page, int size);

    List<VideoTrainingVideo> getSiteVideosForUserPage(String siteId, String userId, String searchText, int page, int size);

    List<VideoTrainingVideo> getSiteViewableVideosForUserPage(String siteId, String userId, String searchText, int page, int size);

    List<VideoTrainingVideo> getAdminAllGlobalVideosPage(String searchText, int page, int size);

    long adminCountAllGlobal(String searchText);

    List<VideoTrainingVideo> getGlobalVideosCursor(String searchText, Instant cursorModifiedOn, String cursorVideoId, int size);

    List<VideoTrainingVideo> getGlobalVideosSorted(String searchText, int offset, int size, String sortField, boolean ascending);

    void registerView(String siteId, String videoId, String userId, Instant when);

    void registerView(String siteId, String videoId, String userId, Instant when, String lessonPageId);

    List<VideoTrainingAnalyticsEvent> getEventsForVideo(String videoId);

    List<VideoTrainingAnalyticsSummary> getSiteAnalyticsSummary(String siteId);

    List<VideoTrainingAnalyticsSummary> getSiteAnalyticsSummary(String siteId, String categoryId);

    boolean canManageLibrary(String siteId, String userId);

    boolean hasManagePermission(String siteId, String userId);

    boolean hasViewPermission(String siteId, String userId);

    boolean canViewVideo(VideoTrainingVideo video, String userId, Instant now);

    boolean canViewVideo(VideoTrainingVideo video, String userId, Instant now, String lessonPageId);

    boolean canViewAnalytics(String siteId, String userId);

    boolean canManageCategories(String siteId, String userId);

    boolean canManageVideo(String videoId, String userId);

    Long getSiteStorageQuotaBytes(String siteId);

    long getSiteStorageUsageBytes(String siteId);

    void registerAudit(String siteId, String userId, String action, String videoId, String details);

    List<VideoTrainingCategory> getCategories(String siteId, int offset, int limit);

    List<VideoTrainingCategory> getSelectableCategoryTree(String siteId, String searchText, int page, int size);

    List<VideoTrainingCategory> getCategoryTree(String siteId, String searchText, int page, int size);

    Optional<VideoTrainingCategory> getCategoryById(String categoryId);

    List<VideoTrainingVideo> getVideosByCategoryId(String siteId, String categoryId);

    VideoTrainingCategoryDeleteImpact getCategoryDeleteImpact(String categoryId);

    VideoTrainingCategory saveCategory(VideoTrainingCategory category);

    void reorderCategories(String siteId, List<VideoTrainingCategoryOrderUpdate> categoryOrder);

    void deleteCategory(String categoryId);

    List<String> getVideoCategoryIds(String videoId);

    void setVideoCategoryIds(String videoId, List<String> categoryIds);

    List<VideoTrainingCourseGroup> getCourseGroupsForSites(List<String> siteIds, String userId, Instant now, int limitPerSite);

    VideoTrainingLessonLink saveLessonLink(VideoTrainingLessonLink lessonLink);

    void deleteLessonLink(String lessonLinkId);

    List<VideoTrainingLessonLink> getLessonLinksForVideo(String videoId);

    VideoTrainingVideo promoteLessonResource(String siteId, String lessonPageId, String lessonItemId, String resourceReference, String title, String description, Long fileSizeBytes);

    Optional<VideoTrainingUserVideoPreference> getUserVideoPreference(String siteId, String videoId, String userId);

    Map<String, VideoTrainingUserVideoPreference> getUserVideoPreferences(String siteId, String userId, List<String> videoIds);

    void setUserFavorite(String siteId, String videoId, String userId, boolean favorite);

    void setUserWatchLater(String siteId, String videoId, String userId, boolean watchLater);

    List<VideoTrainingVideo> getUserFavoriteVideos(String siteId, String userId, Instant now);

    List<VideoTrainingVideo> getUserWatchLaterVideos(String siteId, String userId, Instant now);

    VideoPublicationStatus[] getValidPublicationStatusTransitions(VideoPublicationStatus currentStatus, org.sakaiproject.videotraining.api.model.VideoVisibilityScope visibilityScope);

    String resolveThumbnailUrl(VideoTrainingVideo video);

    boolean ensureSourceState(VideoTrainingVideo video);

    void syncExternalProviderPrivacy(VideoTrainingVideo video);

    VideoTrainingVideo persistVideoChanges(VideoTrainingVideo video);

    List<VideoTrainingVideo> findAllVideos(String searchText, int offset, int size);

    long countAllVideos(String searchText);
}
