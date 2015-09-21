package org.sakaiproject.elfinder.sakai.msgcntr;

import cn.bluejoe.elfinder.service.FsVolume;
import org.sakaiproject.api.app.messageforums.Topic;

/**
 * Created by buckett on 13/08/15.
 */
public class TopicMsgCntrFsItem extends MsgCntrFsItem {

    private Topic topic;

    public TopicMsgCntrFsItem(Topic topic, String id, FsVolume volume) {
        super(id, volume);
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }
}
