/**
 * $URL: $
 * $Id: $
 *
 * Copyright (c) 2024 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lessonbuildertool.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingVideo;
import org.sakaiproject.videotraining.api.service.VideoTrainingService;

import lombok.extern.slf4j.Slf4j;

/**
 * Interface to Video Training Module Videos for Lessons
 * 
 * Allows embedding videos from the Video Training Module into Lessons pages.
 * Supports visibility filtering and permission checking.
 */
@Slf4j
public class VideoTrainingEntity implements LessonEntity {

    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    protected static ResourceLoader rb = new ResourceLoader("lessons");

    private SimplePageBean simplePageBean;
    private LessonEntity nextEntity = null;

    protected static VideoTrainingService videoTrainingService = null;

    protected static MemoryService memoryService = null;
    protected static ToolManager toolManager = null;
    protected static SiteService siteService = null;

    // Entity state
    protected String id;  // Video UUID
    protected int type;
    protected VideoTrainingVideo video = null;
    protected String siteId = null;

    /**
     * Zero-arg constructor for entity lookup patterns
     */
    public VideoTrainingEntity() {
    }

    /**
     * Constructor for creating entity instance with ID
     */
    protected VideoTrainingEntity(String videoId, String siteId) {
        this.id = videoId;
        this.type = LessonEntity.TYPE_VIDEO_TRAINING;
        this.siteId = siteId;
    }

    // ============ Service Setters (Spring Injection) ============

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }

    public void setNextEntity(LessonEntity e) {
        this.nextEntity = e;
    }

    public LessonEntity getNextEntity() {
        return nextEntity;
    }

    public void setMemoryService(MemoryService m) {
        memoryService = m;
    }

    public void setToolManager(ToolManager tm) {
        if (toolManager == null) toolManager = tm;
    }

    public void setSiteService(SiteService sm) {
        if (siteService == null) siteService = sm;
    }

    public void setVideoTrainingService(VideoTrainingService vts) {
        if (videoTrainingService == null) videoTrainingService = vts;
    }


    public void init() {
        log.info("VideoTrainingEntity.init()");
    }

    public void destroy() {
        log.info("VideoTrainingEntity.destroy()");
    }

    // ============ LessonEntity Interface Implementation ============

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getToolId() {
        return "sakai.video.training";
    }

    @Override
    public String getReference() {
        return "/" + LessonEntity.VIDEO_TRAINING + "/" + id;
    }

    @Override
    public int getLevel() {
        return 0;  // Not hierarchical like forums
    }

    @Override
    public int getTypeOfGrade() {
        return 1;  // No grading support in P1
    }

    @Override
    public int getSubmissionType() {
        return 0;  // No submissions in P1
    }

    @Override
    public boolean showAdditionalLink() {
        return false;
    }

    @Override
    public boolean isUsable() {
        return true;  // Always usable if it exists
    }

    // ============ Entity Discovery Methods ============

    @Override
    public List<LessonEntity> getEntitiesInSite() {
        return getEntitiesInSite(null);
    }

    @Override
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {
        List<LessonEntity> ret = new ArrayList<>();

        if (videoTrainingService == null || bean == null) {
            return ret;
        }

        try {
            String currentSiteId = bean.getCurrentSiteId();
            String userId = bean.getCurrentUserId();
            Long totalCount = videoTrainingService.countSiteVideosForUser(currentSiteId, userId, null);
            List<VideoTrainingVideo> videos = videoTrainingService.getSiteVideosForUserPage(currentSiteId, userId, "", 0, totalCount.intValue());
            for (VideoTrainingVideo video : videos) {
                String videoId = video.getId();
                if (StringUtils.isBlank(videoId)) {
                    continue;
                }

                VideoTrainingEntity entity = new VideoTrainingEntity(videoId, currentSiteId);
                entity.video = video;
                entity.setSimplePageBean(bean);
                ret.add(entity);
            }
        } catch (Exception e) {
            log.warn("Error retrieving VTM videos for site: {}", e.getMessage());
        }

        return ret;
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {
        if (!ref.startsWith("/" + LessonEntity.VIDEO_TRAINING + "/")) {
            return getEntity(ref);
        }

        try {
            String idString = ref.substring(("/" + LessonEntity.VIDEO_TRAINING + "/").length());
            VideoTrainingEntity entity = new VideoTrainingEntity(idString, o != null ? o.getCurrentSiteId() : null);
            entity.setSimplePageBean(o);
            VideoTrainingVideo video = videoTrainingService.getVideoById(idString).orElse(null);
            entity.video = video;

            return entity;
        } catch (Exception e) {
            log.warn("Error parsing video reference: {}", ref);
            if (nextEntity != null) {
                return nextEntity.getEntity(ref);
            }
            return null;
        }
    }

    public LessonEntity getEntity(String ref) {
        if (!ref.startsWith("/" + LessonEntity.VIDEO_TRAINING + "/")) {
            if (nextEntity != null) {
                return nextEntity.getEntity(ref);
            }
            return null;
        }

        try {
            String idString = ref.substring(("/" + LessonEntity.VIDEO_TRAINING + "/").length());
            return new VideoTrainingEntity(idString, null);
        } catch (Exception e) {
            log.warn("Error parsing video reference: {}", ref);
            if (nextEntity != null) {
                return nextEntity.getEntity(ref);
            }
            return null;
        }
    }

    // ============ Video Loading ============

    protected void loadVideo() {
        if (video != null || id == null) {
            return;
        }

        try {
            video = videoTrainingService.getVideoById(id).orElse(null);
        } catch (Exception e) {
            log.warn("Error loading video {}: {}", id, e.getMessage());
            video = null;
        }
    }

    // ============ Video Properties ============

    public String getTitle() {
        loadVideo();

        if (video == null) {
            return null;
        }

        return ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(video.getTitle());
    }

    public String getDescription() {
        loadVideo();
        return video == null ? null : video.getDescription();
    }

    public String getUrl() {
        // Generate URL for embedded video player
        loadVideo();
        if (video == null) {
            return null;
        }

        String baseUrl = ServerConfigurationService.getServerUrl();
        
        if (simplePageBean != null) {
            String toolId = simplePageBean.getCurrentTool(getToolId());
            String siteId = simplePageBean.getCurrentSiteId();
            if (toolId != null && siteId != null) {
                // Return tool inline URL using getToolUrl to match how AssignmentEntity handles it
                String lessonPageParam = "";
                if (simplePageBean.getCurrentPageId() > 0) {
                    lessonPageParam = "&lessonPageId=" + simplePageBean.getCurrentPageId();
                }
                return ServerConfigurationService.getToolUrl() + "/" + toolId + "/videos/" + id + "?panel=Main&embedded=true" + lessonPageParam;
            }
        }

        String sourceReference = video == null ? null : video.getSourceReference();
        if (StringUtils.isNotBlank(sourceReference)) {
            return sourceReference;
        }

        String lessonPageParam = "";
        if (simplePageBean != null && simplePageBean.getCurrentPageId() > 0) {
            lessonPageParam = "?lessonPageId=" + simplePageBean.getCurrentPageId();
        }

        return baseUrl + "/direct/video-training/" + id + "/view" + lessonPageParam;
    }

    public String getPortalUrl() {
        if (simplePageBean != null) {
            String siteId = simplePageBean.getCurrentSiteId();
            if (siteId != null) {
                return ServerConfigurationService.getServerUrl() + "/portal/site/" + siteId;
            }
        }
        return getUrl();
    }

    public String getThumbnailUrl() {
        loadVideo();
        if (video == null) {
            return "";
        }

        try {
            if (videoTrainingService != null) {
                return StringUtils.defaultString(videoTrainingService.resolveThumbnailUrl(video), "");
            }
        } catch (Exception e) {
            log.warn("Error resolving thumbnail via VideoTrainingService: {}", e.getMessage());
        }

        return "";
    }

    public boolean isThumbnailVideo() {
        loadVideo();
        return video != null
                && video.getProviderType() == VideoProviderType.NATIVE
                && StringUtils.isNotBlank(video.getSourceReference());
    }

    public String getEditNote() {
        loadVideo();
        if (video == null) {
            return rb.getString("vtm.video.deleted");
        }
        return null;
    }

    @Override
    public Date getDueDate() {
        return null;  // Videos don't have due dates
    }

    @Override
    public LessonSubmission getSubmission(String user) {
        return null;  // No submissions for P1
    }

    @Override
    public int getSubmissionCount(String user) {
        return 0;  // No submissions for P1
    }

    @Override
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
        return null;  // Creation happens via VTM tool, not directly from Lessons
    }

    @Override
    public String editItemUrl(SimplePageBean bean) {
        return null;  // Cannot edit videos from Lessons picker
    }

    @Override
    public String editItemSettingsUrl(SimplePageBean bean) {
        return null;  // No settings to edit from Lessons
    }

    @Override
    public boolean objectExists() {
        loadVideo();
        return video != null;
    }

    @Override
    public boolean notPublished(String ref) {
        return false;  // Not using draft/publish for VTM in lessons integration
    }

    @Override
    public boolean notPublished() {
        return !objectExists();
    }

    @Override
    public Collection<String> getGroups(boolean nocache) {
        return null;  // Not group-aware in P1
    }

    @Override
    public void setGroups(Collection<String> groups) {
        // Not group-aware in P1
    }

    @Override
    public String getObjectId() {
        return LessonEntity.VIDEO_TRAINING + "/" + id;
    }

    @Override
    public String findObject(String objectid, Map<String, String> objectMap, String siteid) {
        if (objectid != null && objectid.startsWith(LessonEntity.VIDEO_TRAINING + "/")) {
            String videoId = objectid.substring((LessonEntity.VIDEO_TRAINING + "/").length());
            return "/" + LessonEntity.VIDEO_TRAINING + "/" + videoId;
        }

        if (nextEntity != null) {
            return nextEntity.findObject(objectid, objectMap, siteid);
        }
        return null;
    }

    @Override
    public String getSiteId() {
        if (siteId != null) {
            return siteId;
        }
        
        loadVideo();
        return video == null ? null : video.getSiteId();
    }

    @Override
    public void preShowItem(SimplePageItem simplePageItem) {
        // Update item properties from video if needed
        loadVideo();
        if (video != null) {
            String itemName = simplePageItem.getName();
            String itemDescription = simplePageItem.getDescription();
            String videoTitle = video == null ? null : video.getTitle();
            String videoDescription = video == null ? null : video.getDescription();

            // Sync title if not set in item
            if (StringUtils.isEmpty(itemName) && StringUtils.isNotEmpty(videoTitle)) {
                simplePageItem.setName(videoTitle);
            }

            // Sync description if not set in item
            if (StringUtils.isEmpty(itemDescription) && StringUtils.isNotEmpty(videoDescription)) {
                simplePageItem.setDescription(videoDescription);
            }
        }
    }
}
