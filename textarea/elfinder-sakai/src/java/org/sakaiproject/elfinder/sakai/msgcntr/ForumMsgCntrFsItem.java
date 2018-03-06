/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
