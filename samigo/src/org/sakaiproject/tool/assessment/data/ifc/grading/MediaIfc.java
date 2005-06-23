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

}
