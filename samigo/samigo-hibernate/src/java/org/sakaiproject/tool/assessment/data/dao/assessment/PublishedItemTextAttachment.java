/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-hibernate/src/java/org/sakaiproject/tool/assessment/data/dao/grading/MediaData.java $
 * $Id: MediaData.java 11438 2006-06-30 20:06:03Z daisyf@stanford.edu $
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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import java.io.Serializable;
import java.util.Date;


public class PublishedItemTextAttachment
    extends PublishedAttachmentData
    implements Serializable, ItemTextAttachmentIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -5089858299609212702L;
private ItemTextIfc itemText;

  public PublishedItemTextAttachment()  { }

  public PublishedItemTextAttachment(Long attachmentId, ItemTextIfc itemText, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, ItemTextAttachmentIfc.ITEM_TEXT_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    this.itemText = itemText;
  }

  public PublishedItemTextAttachment(Long attachmentId, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, ItemTextAttachmentIfc.ITEM_TEXT_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
  }

	public ItemTextIfc getItemText() {
		return itemText;
	}

	public void setItemText(ItemTextIfc itemText) {
		this.itemText = itemText;
	}

  public Long getAttachmentType() {
    return ItemTextAttachmentIfc.ITEM_TEXT_ATTACHMENT;
  }

  public void setAttachmentType(Long attachmentType) {
  }


}
