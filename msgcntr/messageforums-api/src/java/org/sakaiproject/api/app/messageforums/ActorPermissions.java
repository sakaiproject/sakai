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

import java.util.List;

public interface ActorPermissions {

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public List getAccessors();

    public void setAccessors(List accessors);

    public List getContributors();

    public void setContributors(java.util.List contributors);

    public java.util.List getModerators();

    public void setModerators(java.util.List moderators);
    
    public void addAccesssor(MessageForumsUser user);
    
    public void removeAccessor(MessageForumsUser user);
    
    public void addModerator(MessageForumsUser user);
    
    public void removeModerator(MessageForumsUser user);
    
    public void addContributor(MessageForumsUser user);
    
    public void removeContributor(MessageForumsUser user);

}