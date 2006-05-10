/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Label;

public class LabelImpl extends MutableEntityImpl implements Label {

    private static final Log LOG = LogFactory.getLog(LabelImpl.class);
    
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
