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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.data.ifc.grading;

import java.io.Serializable;
import java.util.Date;

public interface MediaIfc
    extends Serializable{

  Long getMediaId();

  void setMediaId(Long mediaId);

  ItemGradingIfc getItemGradingData();

  void setItemGradingData(ItemGradingIfc itemGradingData);

  byte[] getMedia();

  void setMedia(byte[] media);

  Long getFileSize();

  void setFileSize(Long fileSize);

  void setMimeType(String mimeType);

  String getMimeType();

  String getDescription();

  void setDescription(String pdescription);

  String getLocation();

  void setLocation(String location);

  String getFilename();

  void setFilename(String filename);

  boolean getIsLink();

  void setIsLink(boolean isLink);

  boolean getIsHtmlInline();

  void setIsHtmlInline(boolean isHtmlInline);

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

  String getDuration();

  void setDuration(String duration);

  boolean getDurationIsOver();

  void setDurationIsOver(boolean durationIsOver);

  String getTimeAllowed();

  void setTimeAllowed(String timeAllowed);

}
