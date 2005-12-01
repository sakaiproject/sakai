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

public interface Area extends MutableEntity
{
 
  public void setVersion(Integer version);

  public String getContextId();

  public void setContextId(String contextId);

  public Boolean getHidden();

  public void setHidden(Boolean hidden);

  public String getName();

  public void setName(String name); 
  
  public Boolean getEnabled();

  public void setEnabled(Boolean enabled);
  
  public ControlPermissions getControlPermissions();

  public void setControlPermissions(ControlPermissions controlPermissions);

  public MessagePermissions getMessagePermissions();

  public void setMessagePermissions(MessagePermissions messagePermissions);

  
  public List getOpenForums();
  public void setOpenForums(List openForums);
  public List getPrivateForums();
  public void setPrivateForums(List discussionForums);
  public List getDiscussionForums();
  public void setDiscussionForums(List discussionForums);

  
  /**
   * Get type of Area
   * @return
   */
  public String getTypeUuid();
  
  /** 
   * Set type of area
   */   
  public void setTypeUuid(String typeUuid);
  
  public void addPrivateForum(BaseForum forum);
  public void removePrivateForum(BaseForum forum);
  public void addDiscussionForum(BaseForum forum);
  public void removeDiscussionForum(BaseForum forum);
  public void addOpenForum(BaseForum forum);
  public void removeOpenForum(BaseForum forum); 
  
}
