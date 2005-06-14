package org.sakaiproject.tool.assessment.ui.bean.shared;

import java.io.Serializable;

/**
 * <p> </p>
 * <p>Description: Media Bean with some properties</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */

public class MediaBean
  implements Serializable
{
  private String mediaId;
  private String mediaUrl;
  private String filename;

  public MediaBean()
  {
  }

  public void setMediaId(String mediaId)
  {
    this.mediaId = mediaId;
  }

  public String getMediaId()
  {
    return mediaId;
  }

  public void setMediaUrl(String mediaUrl)
  {
    this.mediaUrl = mediaUrl;
  }

  public String getMediaUrl()
  {
    return mediaUrl;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public String getFilename()
  {
    return filename;
  }

}
