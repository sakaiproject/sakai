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
  /**
	 * 
	 */
	private static final long serialVersionUID = 3080542880089498490L;
private String mediaId;
  private String mediaUrl;
  private String filename;
  private Long itemGradingId;

  
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

  public void setItemGradingId(Long itemGradingId)
  {
    this.itemGradingId = itemGradingId;
  }

  public Long getItemGradingId()
  {
    return itemGradingId;
  }
}
