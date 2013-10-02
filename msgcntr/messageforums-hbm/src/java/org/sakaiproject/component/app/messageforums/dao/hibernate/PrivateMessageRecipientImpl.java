/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PrivateMessageRecipientImpl.java $
 * $Id: PrivateMessageRecipientImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;


public class PrivateMessageRecipientImpl implements PrivateMessageRecipient{
        
  private String userId;
  private String typeUuid;
  private String contextId;
  private Boolean read;
  private Boolean bcc;
  private Boolean replied;
  
  /**
   * default constructor
   */
  public PrivateMessageRecipientImpl(){}
  
  /**
   * constructor
   * @param userId
   * @param typeUuid
   * @param contextId
   * @param read
   */
  public PrivateMessageRecipientImpl(String userId, String typeUuid, String contextId, Boolean read, Boolean bcc){
    this.userId = userId;
    this.typeUuid = typeUuid;
    this.contextId = contextId;
    this.read = read;
    this.bcc = bcc;
    this.replied = false;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.PrivateMessageRecipients#getTypeUuid()
   */
  public String getTypeUuid()
  {
    return typeUuid;
  }
  /**
   * @see org.sakaiproject.api.app.messageforums.PrivateMessageRecipients#setTypeUuid(java.lang.String)
   */
  public void setTypeUuid(String typeUuid)
  {
    this.typeUuid = typeUuid;
  }
  /**
   * @see org.sakaiproject.api.app.messageforums.PrivateMessageRecipient#getContextId()
   */
  public String getContextId()
  {
    return contextId;
  }
  /**
   * @see org.sakaiproject.api.app.messageforums.PrivateMessageRecipient#setContextId(java.lang.String)
   */
  public void setContextId(String contextId)
  {
    this.contextId = contextId;
  }      
  /**
   * @see org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipients#getUserId()
   */
  public String getUserId()
  {
    return userId;
  }
  /**
   * @see org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipients#setUserId(java.lang.String)
   */
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  /**
   * @see org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipients#getRead()
   */
  public Boolean getRead()
  {
    return read;
  }
  /**
   * @see org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipients#setRead(java.lang.Boolean)
   */
  public void setRead(Boolean read)
  {
    this.read = read;
  }
  
  public Boolean getReplied()
  {
    return replied;
  }
  
  public void setReplied(Boolean replied)
  {
    this.replied = replied;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == this){
      return true;
    }
        
    if (!(obj instanceof PrivateMessageRecipientImpl))
      return false;
    
    PrivateMessageRecipientImpl objCast = (PrivateMessageRecipientImpl) obj;
    
    return (objCast.userId != null && objCast.userId.equals(this.userId)) &&
           (objCast.typeUuid != null && objCast.typeUuid.equals(this.typeUuid)) &&
           (objCast.contextId != null && objCast.contextId.equals(this.contextId)) &&
           (objCast.read != null && objCast.read.equals(this.read));
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    int result = 17;
    result = 41 * result + ((userId == null) ? 0 : userId.hashCode());
    result = 41 * result + ((typeUuid == null) ? 0 : typeUuid.hashCode());
    result = 41 * result + ((contextId == null) ? 0 : contextId.hashCode());
    result = 41 * result + ((read == null) ? 0 : read.hashCode());
    return result;    
  }

  public Boolean getBcc() {
	  return bcc;
  }

  public void setBcc(Boolean bcc) {
	  this.bcc = bcc;
  }

}
