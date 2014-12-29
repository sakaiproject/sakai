/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.author;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import java.io.Serializable;
import java.util.Date;

public class AttachmentBean implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 4912478649252175081L;
private Long attachmentId;
  private String resourceId;
  private String filename;
  private String mimeType;
  private Long fileSize; // in kilobyte
  private String description;
  private String location;
  private Boolean isLink;
  private Integer status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Long attachmentType;

  public AttachmentBean()  {
  }


  public AttachmentBean(AttachmentIfc a){
    this.attachmentId = a.getAttachmentId();
    this.resourceId = a.getResourceId();
    this.filename = a.getFilename();
    this.mimeType = a.getMimeType();
    this.fileSize = a.getFileSize();
    this.description = a.getDescription();
    this.location = a.getLocation();
    this.isLink = a.getIsLink();
    this.status = a.getStatus();
    this.createdBy = a.getCreatedBy();
    this.createdDate = a.getCreatedDate();
    this.lastModifiedBy = a.getLastModifiedBy();
    this.lastModifiedDate = a.getLastModifiedDate();
  }

  public Long getAttachmentId()
  {
    return attachmentId;
  }

  public void setAttachmentId(Long attachmentId)
  {
    this.attachmentId = attachmentId;
  }

  public String getResourceId()
  {
    return resourceId;
  }

  public void setResourceId(String resourceId)
  {
    this.resourceId = resourceId;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getMimeType()
  {
    return mimeType;
  }


  public Long getFileSize()
  {
    return fileSize;
  }

  public void setFileSize(Long fileSize)
  {
    this.fileSize = fileSize;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String pdescription)
  {
    description = pdescription;
  }

  public String getLocation()
  {
    return location;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

 public Boolean getIsLink()
 {
   return isLink;
 }

 public void setIsLink(Boolean isLink)
 {
   this.isLink = isLink;
 }

  public Integer getStatus() {
    return this.status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedDate() {
    return this.createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public Long getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(Long attachmentType) {
    this.attachmentType = attachmentType;
  }

}
