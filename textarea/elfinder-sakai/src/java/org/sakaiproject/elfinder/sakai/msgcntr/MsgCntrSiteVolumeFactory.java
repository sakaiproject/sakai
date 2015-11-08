package org.sakaiproject.elfinder.sakai.msgcntr;

import cn.bluejoe.elfinder.service.FsItem;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.site.SiteFsItem;
import org.sakaiproject.elfinder.sakai.site.SiteFsVolume;

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

    public void setDiscussionForumManager(DiscussionForumManager discussionForumManager) {
        this.discussionForumManager = discussionForumManager;
    }

    public void setMessageForumsForumManager(MessageForumsForumManager messageForumsForumManager) {
        this.messageForumsForumManager = messageForumsForumManager;
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
                    if("forum".equals(parts[1])) {
                        topicId = parts[2];
                        BaseForum topic1 = messageForumsForumManager.getForumByUuid(topicId);
                        return new ForumMsgCntrFsItem(topic1, "", this);
                    }

                    if("topic".equals(parts[1])) {
                        topicId = parts[2];
                        Topic topic = messageForumsForumManager.getTopicByUuid(topicId);
                        return new TopicMsgCntrFsItem(topic, "", this);
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

                while(discussionForum1.hasNext()) {
                    Topic topic1 = (Topic)discussionForum1.next();
                    TopicMsgCntrFsItem childFsi = new TopicMsgCntrFsItem(topic1, "", this);
                    items.add(childFsi);
                }
            }

            return (FsItem[])items.toArray(new FsItem[0]);
        }

        public InputStream openInputStream(FsItem fsi) throws IOException {
            return null;
        }

        public String getURL(FsItem f) {
            return null;
        }

        public boolean isWriteable(FsItem fsi) {
            return false;
        }
    }
}
