/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.elfinder.sakai.msgcntr;

import cn.bluejoe.elfinder.service.FsItem;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.site.SiteFsItem;
import org.sakaiproject.elfinder.sakai.site.SiteFsVolume;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by buckett on 10/08/15.
 */
public class MsgCntrSiteVolumeFactory implements SiteVolumeFactory {

    private DiscussionForumManager discussionForumManager;
    private MessageForumsForumManager messageForumsForumManager;
    private UIPermissionsManager uiPermissionsManager;
    private UserDirectoryService userDirectoryService;
    private ServerConfigurationService serverConfigurationService;
    private static final String MFORUM_FORUM_PREFIX = "/direct/forum/";
    private static final String MFORUM_TOPIC_PREFIX = "/direct/forum_topic/";

    public void setDiscussionForumManager(DiscussionForumManager discussionForumManager) {
        this.discussionForumManager = discussionForumManager;
    }

    public void setMessageForumsForumManager(MessageForumsForumManager messageForumsForumManager) {
        this.messageForumsForumManager = messageForumsForumManager;
    }

    public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
        this.uiPermissionsManager = uiPermissionsManager;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public String getPrefix() {
        return "msgcntr";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new MsgCntrSiteVolume(sakaiFsService, siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.forums";
    }

    public class MsgCntrSiteVolume extends ReadOnlyFsVolume implements SiteVolume {
        private SakaiFsService service;
        private String siteId;

        public MsgCntrSiteVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        public String getSiteId() {
            return this.siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return MsgCntrSiteVolumeFactory.this;
        }

        public boolean exists(FsItem newFile) {
            return false;
        }

        public FsItem fromPath(String relativePath) {
            if(relativePath != null && !relativePath.isEmpty()) {
                String[] parts = relativePath.split("/");
                if(parts.length > 2) {
                    String topicId;
                    String userId = userDirectoryService.getCurrentUser().getId();
                    if("forum".equals(parts[1])) {
                        topicId = parts[2];
                        BaseForum topic1 = messageForumsForumManager.getForumByUuid(topicId);
                        return new ForumMsgCntrFsItem(topic1, "", this);
                    }

                    if("topic".equals(parts[1])) {
                        topicId = parts[2];
                        Topic topic = messageForumsForumManager.getTopicByUuid(topicId);
                        //In Forums permissions work individually on topics therefore adding checks for topics in el-finder
                        if(uiPermissionsManager.isRead(Long.valueOf(topic.getId()), false, false,userId, this.siteId)){
                            return new TopicMsgCntrFsItem(topic, "", this);
                        }
                    }
                }

                return this.getRoot();
            } else {
                return this.getRoot();
            }
        }

        public String getPath(FsItem fsi) throws IOException {
            if(this.getRoot().equals(fsi)) {
                return "";
            } else if(fsi instanceof ForumMsgCntrFsItem) {
                ForumMsgCntrFsItem topicMsgCntrFsItem1 = (ForumMsgCntrFsItem)fsi;
                return "/forum/" + topicMsgCntrFsItem1.getForum().getUuid();
            } else if(fsi instanceof TopicMsgCntrFsItem) {
                TopicMsgCntrFsItem topicMsgCntrFsItem = (TopicMsgCntrFsItem)fsi;
                return "/topic/" + topicMsgCntrFsItem.getTopic().getUuid();
            } else {
                throw new IllegalArgumentException("Wrong type: " + fsi);
            }
        }

        public String getDimensions(FsItem fsi) {
            return null;
        }

        public long getLastModified(FsItem fsi) {
            return 0L;
        }

        public String getMimeType(FsItem fsi) {
            return this.isFolder(fsi)?"directory":"sakai/forums";
        }

        public String getName() {
            return null;
        }

        public String getName(FsItem fsi) {
            if(this.getRoot().equals(fsi)) {
                return "Forums";
            } else if(fsi instanceof ForumMsgCntrFsItem) {
                return ((ForumMsgCntrFsItem)fsi).getForum().getTitle();
            } else if(fsi instanceof TopicMsgCntrFsItem) {
                return ((TopicMsgCntrFsItem)fsi).getTopic().getTitle();
            } else {
                throw new IllegalArgumentException("Could not get title for: " + fsi.toString());
            }
        }

        public FsItem getParent(FsItem fsi) {
            if(this.getRoot().equals(fsi)) {
                return service.getSiteVolume(siteId).getRoot();
            } else if(fsi instanceof ForumMsgCntrFsItem) {
                return this.getRoot();
            } else if(fsi instanceof TopicMsgCntrFsItem) {
                Topic topic = ((TopicMsgCntrFsItem)fsi).getTopic();
                Topic topicAndParent = messageForumsForumManager.getTopicById(true, topic.getId());
                return new ForumMsgCntrFsItem(topicAndParent.getBaseForum(), "", this);
            } else {
                return null;
            }
        }

        public FsItem getRoot() {
            return new MsgCntrFsItem("", this);
        }

        public long getSize(FsItem fsi) throws IOException {
            return 0L;
        }

        public String getThumbnailFileName(FsItem fsi) {
            return null;
        }

        public boolean hasChildFolder(FsItem fsi) {
            return fsi instanceof MsgCntrFsItem;
        }

        public boolean isFolder(FsItem fsi) {
            return !(fsi instanceof TopicMsgCntrFsItem);
        }

        public boolean isRoot(FsItem fsi) {
            return false;
        }

        public FsItem[] listChildren(FsItem fsi) {
            ArrayList items = new ArrayList();
            if(this.getRoot().equals(fsi)) {
                List forum = discussionForumManager.getDiscussionForumsByContextId(this.siteId);
                Iterator forumAndTopics = forum.iterator();

                while(forumAndTopics.hasNext()) {
                    DiscussionForum discussionForum = (DiscussionForum)forumAndTopics.next();
                    discussionForum.getTitle();
                    ForumMsgCntrFsItem topic = new ForumMsgCntrFsItem(discussionForum, "", this);
                    items.add(topic);
                }
            } else if(fsi instanceof ForumMsgCntrFsItem) {
                BaseForum forum1 = ((ForumMsgCntrFsItem)fsi).getForum();
                BaseForum forumAndTopics1 = messageForumsForumManager.getForumByIdWithTopics(forum1.getId());
                Iterator discussionForum1 = forumAndTopics1.getTopics().iterator();
                String userId = userDirectoryService.getCurrentUser().getId();

                while(discussionForum1.hasNext()) {
                    Topic topic1 = (Topic)discussionForum1.next();
                    //In Forums permissions work individually on topics therefore adding checks for topics in el-finder
                    if(uiPermissionsManager.isRead(Long.valueOf(topic1.getId()), false, false, userId, this.siteId)) {
                        TopicMsgCntrFsItem childFsi = new TopicMsgCntrFsItem(topic1, "", this);
                        items.add(childFsi);
                    }
                }
            }

            return (FsItem[])items.toArray(new FsItem[0]);
        }

        public InputStream openInputStream(FsItem fsi) throws IOException {
            return null;
        }

        public String getURL(FsItem fsItem) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if(fsItem instanceof ForumMsgCntrFsItem){
                BaseForum forum1 = ((ForumMsgCntrFsItem)fsItem).getForum();
               return serverUrlPrefix + MFORUM_FORUM_PREFIX + String.valueOf(forum1.getId());
            }
            else if(fsItem instanceof TopicMsgCntrFsItem){
                Topic topic = ((TopicMsgCntrFsItem)fsItem).getTopic();
                return serverUrlPrefix + MFORUM_TOPIC_PREFIX + String.valueOf(topic.getId());
            }
            else{
                return null;
            }
        }

        public boolean isWriteable(FsItem fsi) {
            return false;
        }
    }
}
