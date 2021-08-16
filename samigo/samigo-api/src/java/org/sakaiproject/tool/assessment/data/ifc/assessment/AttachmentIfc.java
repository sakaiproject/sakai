/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.io.Serializable;
import java.util.Date;

public interface AttachmentIfc
    extends Serializable{

  // This three parameters are for SAM_ATTACHMENT_T table	
  public static final Long ASSESSMENT_ATTACHMENT = 1L;
  public static final Long SECTION_ATTACHMENT = 2L;
  public static final Long ITEM_ATTACHMENT = 3L;
  //This parameter is for SAM_GRADINGATTACHMENT_T table
  public static final Long ITEMGRADING_ATTACHMENT = 1L;
  public static final Long ASSESSMENTGRADING_ATTACHMENT = 4L;
  
  public static final Long ITEM_TEXT_ATTACHMENT = 5L;//for EMI sub-item attachments
  public static final Integer ACTIVE_STATUS = 1;
  public static final Integer INACTIVE_STATUS = 0;

  Long getAttachmentId();

  void setAttachmentId(Long attachmentId);

  void setResourceId(String resourceId);

  String getResourceId();

  String getFilename();

  void setFilename(String filename);

  void setMimeType(String mimeType);

  String getMimeType();

  Long getFileSize();

  void setFileSize(Long fileSize);

  String getDescription();

  void setDescription(String pdescription);

  String getLocation();

  void setLocation(String location);

  Boolean getIsLink();

  void setIsLink(Boolean isLink);

  Integer getStatus();

  void setStatus(Integer status);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  Date getCreatedDate();

  void setCreatedDate(Date createdDate);

  String getLastModifiedBy();

  void setLastModifiedBy(String lastModifiedBy);

  Date getLastModifiedDate();

  void setLastModifiedDate(Date lastModifiedDate);

}
