package org.sakaiproject.elfinder.sakai.msgcntr;

import cn.bluejoe.elfinder.service.FsVolume;
import org.sakaiproject.api.app.messageforums.BaseForum;

/**
 * Created by buckett on 13/08/15.
 */
public class ForumMsgCntrFsItem extends MsgCntrFsItem {

    private BaseForum forum;

    public ForumMsgCntrFsItem(BaseForum forum, String id, FsVolume volume) {
        super(id, volume);
        this.forum = forum;
    }

    public BaseForum getForum() {
        return forum;
    }
}
