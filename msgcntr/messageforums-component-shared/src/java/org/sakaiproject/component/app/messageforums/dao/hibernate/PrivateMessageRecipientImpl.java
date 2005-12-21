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

import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;


public class PrivateMessageRecipientImpl implements PrivateMessageRecipient{
        
  private String userId;
  private String typeUuid;
  private String contextId;
  private Boolean read;
  
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
  public PrivateMessageRecipientImpl(String userId, String typeUuid, String contextId, Boolean read){
    this.userId = userId;
    this.typeUuid = typeUuid;
    this.contextId = contextId;
    this.read = read;
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

}
