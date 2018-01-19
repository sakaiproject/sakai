/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/LabelImpl.java $
 * $Id: LabelImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Label;

@Slf4j
public class LabelImpl extends MutableEntityImpl implements Label {

    private String key;
    private String value;
    
    
    // foreign keys for hibernate
    private DiscussionForum discussionForum;
    private DiscussionTopic discussionTopic;
       
    // indecies for hibernate
    private int dtindex;
    private int dfindex;

    public DiscussionForum getDiscussionForum() {
        return discussionForum;
    }

    public void setDiscussionForum(DiscussionForum discussionForum) {
        this.discussionForum = discussionForum;
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
     
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DiscussionTopic getDiscussionTopic() {
        return discussionTopic;
    }

    public void setDiscussionTopic(DiscussionTopic discussionTopic) {
        this.discussionTopic = discussionTopic;
    }

    public int getDfindex() {
        try {
            return getDiscussionForum().getLabels().indexOf(this);
        } catch (Exception e) {
            return dfindex;
        }
    }

    public void setDfindex(int dfindex) {
        this.dfindex = dfindex;
    }

    public int getDtindex() {
        try {
            return getDiscussionTopic().getLabels().indexOf(this);
        } catch (Exception e) {
            return dtindex;
        }
    }

    public void setDtindex(int dtindex) {
        this.dtindex = dtindex;
    }
    
}
