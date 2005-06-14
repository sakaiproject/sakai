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
