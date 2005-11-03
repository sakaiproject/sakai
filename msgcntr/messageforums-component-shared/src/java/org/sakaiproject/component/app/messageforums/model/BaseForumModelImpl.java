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

package org.sakaiproject.component.app.messageforums.model;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.model.BaseForumModel;
import org.sakaiproject.api.common.type.Type;
 
public class BaseForumModelImpl extends MutableEntityModelImpl implements BaseForumModel {

    private static final Log LOG = LogFactory.getLog(BaseForumModelImpl.class);

    private String title;
    private String shortDescription;
    private String extendedDescription;
    private Type  type;
    private List attachments;
    private List topics;
    private Integer sortIndex; 
    
    // package level constructor only used for Testing
    BaseForumModelImpl() {}
    
    public BaseForumModelImpl(BaseForum baseForum) {
        // TODO: set up this model based on hibernate object passes
        
    }

    public List getAttachments() {
        return attachments;
    }

    public void setAttachments(List attachments) {
        this.attachments = attachments;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List getTopics() {
        return topics;
    }

    public void setTopics(List topics) {
        this.topics = topics;
    }

    public Type getType()
    {
      // TODO Auto-generated method stub
      return null;
    }

    public void setType(Type typeModel)
    {
      // TODO Auto-generated method stub
      
    }
    
}
