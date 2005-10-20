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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateRestrictions {

    private static final Log LOG = LogFactory.getLog(DateRestrictions.class);
 
    private Date visible;
    private Boolean visiblePostOnSchedule;
    private Date postingAllowed;
    private Boolean postingAllowedPostOnSchedule;
    private Date readOnly;
    private Boolean readOnlyPostOnSchedule;
    private Date hidden;
    private Boolean hiddenPostOnSchedule;
    private Long id;
    private Integer version; 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public Date getHidden() {
        return hidden;
    }

    public void setHidden(Date hidden) {
        this.hidden = hidden;
    }

    public Boolean getHiddenPostOnSchedule() {
        return hiddenPostOnSchedule;
    }

    public void setHiddenPostOnSchedule(Boolean hiddenPostOnSchedule) {
        this.hiddenPostOnSchedule = hiddenPostOnSchedule;
    }

    public Date getPostingAllowed() {
        return postingAllowed;
    }

    public void setPostingAllowed(Date postingAllowed) {
        this.postingAllowed = postingAllowed;
    }

    public Boolean getPostingAllowedPostOnSchedule() {
        return postingAllowedPostOnSchedule;
    }

    public void setPostingAllowedPostOnSchedule(Boolean postingAllowedPostOnSchedule) {
        this.postingAllowedPostOnSchedule = postingAllowedPostOnSchedule;
    }

    public Date getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Date readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getReadOnlyPostOnSchedule() {
        return readOnlyPostOnSchedule;
    }

    public void setReadOnlyPostOnSchedule(Boolean readOnlyPostOnSchedule) {
        this.readOnlyPostOnSchedule = readOnlyPostOnSchedule;
    }

    public Date getVisible() {
        return visible;
    }

    public void setVisible(Date visible) {
        this.visible = visible;
    }

    public Boolean getVisiblePostOnSchedule() {
        return visiblePostOnSchedule;
    }

    public void setVisiblePostOnSchedule(Boolean visiblePostOnSchedule) {
        this.visiblePostOnSchedule = visiblePostOnSchedule;
    }

}
