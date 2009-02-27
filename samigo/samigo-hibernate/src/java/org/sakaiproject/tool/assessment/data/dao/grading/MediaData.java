/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.grading;

import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import java.io.Serializable;
import java.util.Date;


public class MediaData
    implements Serializable, MediaIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -437737678404198607L;
private Long mediaId;
  private ItemGradingIfc itemGradingData;
  private byte[] media;
  private Long fileSize; // in kilobyte
  private String mimeType;
  private String description;
  private String location;
  private String filename;
  private boolean isLink;
  private boolean isHtmlInline;
  private Integer status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private String duration;

  public MediaData()
  {
  }

  public MediaData(ItemGradingIfc itemGradingData, byte[] media, Long fileSize,
                   String mimeType, String description, String location,
                   String filename, boolean isLink, boolean isHtmlInline,
                   Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate, String duration){
    this.itemGradingData = itemGradingData;
    this.media = media;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.description = description;
    this.location = location;
    this.filename = filename;
    this.isLink = isLink;
    this.isHtmlInline = isHtmlInline;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
    this.duration = duration;
  }

  public MediaData(Long mediaId, ItemGradingIfc itemGradingData, Long fileSize,
                   String mimeType, String description, String location,
                   String filename, boolean isLink, boolean isHtmlInline,
                   Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate, String duration){
    this.mediaId = mediaId;
    this.itemGradingData = itemGradingData;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.description = description;
    this.location = location;
    this.filename = filename;
    this.isLink = isLink;
    this.isHtmlInline = isHtmlInline;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
    this.duration = duration;
  }


  public MediaData(byte[] media, String mimeType)
  {
    setMimeType(mimeType);
    setMedia(media);
    setFileSize( Long.valueOf(media.length));
  }

  public Long getMediaId()
  {
    return mediaId;
  }

  public void setMediaId(Long mediaId)
  {
    this.mediaId = mediaId;
  }

  public ItemGradingIfc getItemGradingData() {
    return itemGradingData;
  }

  public void setItemGradingData(ItemGradingIfc itemGradingData) {
    this.itemGradingData = itemGradingData;
  }

  public byte[] getMedia()
  {
    return media;
  }

  public void setMedia(byte[] media)
  {
    this.media = media;
  }

  public Long getFileSize()
  {
    return fileSize;
  }

  public void setFileSize(Long fileSize)
  {
    this.fileSize = fileSize;
  }

  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  public String getMimeType()
  {
    return mimeType;
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

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

 public boolean getIsLink()
 {
   return isLink;
 }

 public void setIsLink(boolean isLink)
 {
   this.isLink = isLink;
 }

  public boolean getIsHtmlInline()
  {
    return isHtmlInline;
  }

  public void setIsHtmlInline(boolean isHtmlInline)
  {
    this.isHtmlInline = isHtmlInline;
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

  public String getDuration() {
    return duration;
  }
  public void setDuration(String duration) {
    this.duration = duration;
  }

  // convenient method
  private boolean durationIsOver;
  public boolean getDurationIsOver() {
    return durationIsOver;
  }
  public void setDurationIsOver(boolean durationIsOver) {
    this.durationIsOver = durationIsOver;
  }

  private String timeAllowed;
  public String getTimeAllowed() {
	  return timeAllowed;
  }
  
  public void setTimeAllowed(String timeAllowed) {
	  this.timeAllowed = timeAllowed;  
  }
}
