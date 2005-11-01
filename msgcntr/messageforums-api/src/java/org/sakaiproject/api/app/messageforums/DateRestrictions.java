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

package org.sakaiproject.api.app.messageforums;

import java.util.Date;

public interface DateRestrictions {

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public Date getHidden();

    public void setHidden(Date hidden);

    public Boolean getHiddenPostOnSchedule();

    public void setHiddenPostOnSchedule(Boolean hiddenPostOnSchedule);

    public Date getPostingAllowed();

    public void setPostingAllowed(Date postingAllowed);

    public Boolean getPostingAllowedPostOnSchedule();

    public void setPostingAllowedPostOnSchedule(Boolean postingAllowedPostOnSchedule);

    public Date getReadOnly();

    public void setReadOnly(Date readOnly);

    public Boolean getReadOnlyPostOnSchedule();

    public void setReadOnlyPostOnSchedule(Boolean readOnlyPostOnSchedule);

    public Date getVisible();

    public void setVisible(Date visible);

    public Boolean getVisiblePostOnSchedule();

    public void setVisiblePostOnSchedule(Boolean visiblePostOnSchedule);

}