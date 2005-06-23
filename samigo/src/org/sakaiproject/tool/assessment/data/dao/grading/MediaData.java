/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.data.dao.grading;

import org.sakaiproject.tool.assessment.data.ifc.grading.MediaIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import java.io.Serializable;
import java.util.Date;
import java.util.ResourceBundle;

public class MediaData
    implements Serializable, MediaIfc
{
  private static final ResourceBundle mediaProperties =
      ResourceBundle.getBundle("org.sakaiproject.tool.assessment.data.ifc.grading.media");
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

  public MediaData()
  {
  }

  public MediaData(ItemGradingIfc itemGradingData, byte[] media, Long fileSize,
                   String mimeType, String description, String location,
                   String filename, boolean isLink, boolean isHtmlInline,
                   Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
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
  }

  public MediaData(byte[] media, String mimeType)
  {
    setMimeType(mimeType);
    setMedia(media);
    setFileSize(new Long(media.length));
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

  // determine value of SAVETODB
  public static boolean saveToDB(){
    String saveToDB = mediaProperties.getString("SAVETODB");
    if (("true").equals(saveToDB))
      return true;
    else
      return false;
  }
}
