/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-api/src/java/org/sakaiproject/tool/assessment/data/ifc/grading/MediaIfc.java $
 * $Id: MediaIfc.java 11438 2006-06-30 20:06:03Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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

  public static Long ASSESSMENT_ATTACHMENT = new Long(1);
  public static Long SECTION_ATTACHMENT = new Long(2);
  public static Long ITEM_ATTACHMENT = new Long(3);
  public static Long ITEMGRADING_ATTACHMENT = new Long(4);
  public static Integer ACTIVE_STATUS = new Integer(1);
  public static Integer INACTIVE_STATUS = new Integer(0);

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
