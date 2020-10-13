package org.sakaiproject.component.app.messageforums.elfinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForumToolFsVolumeFactory implements ToolFsVolumeFactory {

    @Setter private DiscussionForumManager discussionForumManager;
    @Setter private MessageForumsForumManager messageForumsForumManager;
    @Setter private UIPermissionsManager uiPermissionsManager;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private SakaiFsService sakaiFsService;
    @Setter private ServerConfigurationService serverConfigurationService;

    public void init() {
        sakaiFsService.registerToolVolume(this);
    }

    @Override
    public String getPrefix() {
        return "forums";
    }

    @Override
    public ToolFsVolume getVolume(String siteId) {
        return new ForumToolFsVolume(sakaiFsService, siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.forums";
    }

    public class ForumToolFsVolume extends ReadOnlyFsVolume implements ToolFsVolume {

        private static final String FORUM_URL_PREFIX = "/direct/forum/";
        private static final String TOPIC_URL_PREFIX = "/direct/forum_topic/";

        private SakaiFsService sakaiFsService;
        private String siteId;

        public ForumToolFsVolume(SakaiFsService sakaiFsService, String siteId) {
            this.sakaiFsService = sakaiFsService;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return siteId;
        }

        @Override
        public ToolFsVolumeFactory getToolVolumeFactory() {
            return ForumToolFsVolumeFactory.this;
        }

        @Override
        public boolean isWriteable(SakaiFsItem item) {
            return false;
        }

        @Override
        public boolean exists(SakaiFsItem newFile) {
            return false;
        }

        @Override
        public SakaiFsItem fromPath(String path) {
            log.debug("path = {}", path);
            if (StringUtils.isNotBlank(path)) {
                String[] parts = path.split("/");
                if (getPrefix().equals(parts[1])) {
                    switch (parts.length) {
                        case 4:
                            // /forums/site/SITEID
                            log.debug("Matched root from path [{}]", path);
                            break;
                        case 6:
                            // /forums/site/SITEID/forum/5
                            log.debug("Matched forum from path [{}]", path);
                            BaseForum forum = messageForumsForumManager.getForumById(true, Long.valueOf(parts[5]));
                            return new SakaiFsItem(forum.getId().toString(), forum.getTitle(), this, FsType.FORUMS_FORUM);
                        case 8:
                            // /forums/site/SITEID/forum/5/topic/10
                            log.debug("Matched topic from path [{}]", path);
                            Topic topic = messageForumsForumManager.getTopicById(true, Long.valueOf(parts[7]));
                            // In Forums permissions work individually on topics therefore adding checks for topics in el-finder
                            String userId = userDirectoryService.getCurrentUser().getId();
                            if (uiPermissionsManager.isRead((DiscussionTopic) topic, (DiscussionForum) topic.getBaseForum(), userId, this.siteId)) {
                                return new SakaiFsItem(topic.getId().toString(), topic.getTitle(), this, FsType.FORUMS_TOPIC);
                            } else {
                                log.debug("User {} doesn't have access for path [{}]", userId, path);
                            }
                            break;
                        default:
                            log.debug("No match found for path: {}", path);
                            break;
                    }
                }
            }
            return this.getRoot();
        }

        @Override
        public String getDimensions(SakaiFsItem fsi) {
            return null;
        }

        @Override
        public long getLastModified(SakaiFsItem fsi) {
            return 0;
        }

        @Override
        public String getMimeType(SakaiFsItem fsi) {
            return this.isFolder(fsi) ? "directory" : "sakai/forums";
        }

        @Override
        public String getName() {
            // TODO i18m
            return "Discussions";
        }

        @Override
        public String getName(SakaiFsItem fsi) {
            if (this.getRoot().equals(fsi)) {
                return getName();
            } else if (FsType.FORUMS_FORUM.equals(fsi.getType()) || FsType.FORUMS_TOPIC.equals(fsi.getType())) {
                return fsi.getTitle();
            } else {
                throw new IllegalArgumentException("Could not get title for: " + fsi.toString());
            }
        }

        @Override
        public SakaiFsItem getParent(SakaiFsItem fsi) {
            if (this.getRoot().equals(fsi)) {
                return sakaiFsService.getSiteVolume(siteId).getRoot();
            } else if (FsType.FORUMS_FORUM.equals(fsi.getType())) {
                return this.getRoot();
            } else if (FsType.FORUMS_TOPIC.equals(fsi.getType())) {
                Topic topic = messageForumsForumManager.getTopicById(true, Long.valueOf(fsi.getId()));
                BaseForum forum = topic.getBaseForum();
                return new SakaiFsItem(forum.getId().toString(), forum.getTitle(), this, FsType.FORUMS_FORUM);
            }
            return null;
        }

        @Override
        public String getPath(SakaiFsItem fsi) throws IOException {
            if (this.getRoot().equals(fsi)) {
                return "/" + getPrefix() + "/site/" + siteId;
            } else if (FsType.FORUMS_FORUM.equals(fsi.getType())) {
                return "/" + getPrefix() + "/site/" + siteId + "/forum/" + fsi.getId();
            } else if (FsType.FORUMS_TOPIC.equals(fsi.getType())) {
                Topic topic = messageForumsForumManager.getTopicById(true, Long.valueOf(fsi.getId()));
                return "/" + getPrefix() + "/site/" + siteId + "/forum/" + topic.getBaseForum().getId() + "/topic/" + fsi.getId();
            } else {
                throw new IllegalArgumentException("Wrong type: " + fsi.toString());
            }
        }

        @Override
        public SakaiFsItem getRoot() {
            return new SakaiFsItem("", "", this, FsType.FORUMS_FORUM);
        }

        @Override
        public long getSize(SakaiFsItem fsi) throws IOException {
            return 0;
        }

        @Override
        public String getThumbnailFileName(SakaiFsItem fsi) {
            return null;
        }

        @Override
        public boolean hasChildFolder(SakaiFsItem fsi) {
            return FsType.FORUMS_FORUM.equals(fsi.getType());
        }

        @Override
        public boolean isFolder(SakaiFsItem fsi) {
            return !FsType.FORUMS_TOPIC.equals(fsi.getType());
        }

        @Override
        public boolean isRoot(SakaiFsItem fsi) {
            return false;
        }

        @Override
        public SakaiFsItem[] listChildren(SakaiFsItem fsi) throws PermissionException {
            List<SakaiFsItem> items = new ArrayList<>();
            if (this.getRoot().equals(fsi)) {
                List<BaseForum> forums = discussionForumManager.getDiscussionForumsByContextId(this.siteId);
                for (BaseForum forum : forums) {
                    SakaiFsItem item = new SakaiFsItem(forum.getId().toString(), forum.getTitle(), this, FsType.FORUMS_FORUM);
                    items.add(item);
                }
            } else if (FsType.FORUMS_FORUM.equals(fsi.getType())) {
                String userId = userDirectoryService.getCurrentUser().getId();
                BaseForum forum = messageForumsForumManager.getForumByIdWithTopics(Long.valueOf(fsi.getId()));

                for (Topic topic : (List<Topic>) forum.getTopics()) {
                    // In Forums permissions work individually on topics therefore adding checks for topics in el-finder
                    if (uiPermissionsManager.isRead((DiscussionTopic) topic, (DiscussionForum) forum, userId, this.siteId)) {
                        SakaiFsItem item = new SakaiFsItem(topic.getId().toString(), topic.getTitle(), this, FsType.FORUMS_TOPIC);
                        items.add(item);
                    }
                }
            }
            return items.toArray(new SakaiFsItem[0]);
        }

        @Override
        public InputStream openInputStream(SakaiFsItem fsi) throws IOException {
            return null;
        }

        @Override
        public String getURL(SakaiFsItem fsi) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if (FsType.FORUMS_FORUM.equals(fsi.getType())) {
                return serverUrlPrefix + FORUM_URL_PREFIX + fsi.getId();
            } else if (FsType.FORUMS_TOPIC.equals(fsi.getType())) {
                return serverUrlPrefix + TOPIC_URL_PREFIX + fsi.getId();
            }
            return null;
        }
    }
}
