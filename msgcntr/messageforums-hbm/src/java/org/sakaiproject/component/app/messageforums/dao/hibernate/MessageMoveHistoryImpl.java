/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.MessageMoveHistory;

public class MessageMoveHistoryImpl extends MutableEntityImpl implements MessageMoveHistory {
    private Long fromTopicId;
    private Long messageId;
    private Boolean reminder;
    private Long toTopicId;

    public Long getFromTopicId() {
        return fromTopicId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Boolean getReminder() {
        return reminder;
    }

    public Long getToTopicId() {
        return toTopicId;
    }

    public void setFromTopicId(Long topicId) {
        this.fromTopicId = topicId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public void setReminder(Boolean remind) {
        this.reminder = remind;
    }

    public void setToTopicId(Long topicId) {
        this.toTopicId = topicId;

    }
}
