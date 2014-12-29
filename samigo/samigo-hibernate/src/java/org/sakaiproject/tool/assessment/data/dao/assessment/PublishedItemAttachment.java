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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import java.io.Serializable;
import java.util.Date;


public class PublishedItemAttachment
    extends PublishedAttachmentData
    implements Serializable, ItemAttachmentIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -5089858299609212702L;
private ItemDataIfc item;

  public PublishedItemAttachment()  { }

  public PublishedItemAttachment(Long attachmentId, ItemDataIfc item, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, ItemAttachmentIfc.ITEM_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    this.item = item;
  }

  public PublishedItemAttachment(Long attachmentId, String resourceId,
                   String filename, String mimeType,
                   Long fileSize, String description, String location,
                   Boolean isLink, Integer status, String createdBy, Date createdDate,
                   String lastModifiedBy, Date lastModifiedDate){
    super(attachmentId, resourceId, ItemAttachmentIfc.ITEM_ATTACHMENT,
          filename, mimeType,
          fileSize, description, location, isLink, status,
          createdBy, createdDate, lastModifiedBy, lastModifiedDate);
  }

  public ItemDataIfc getItem() {
    return item;
  }

  public void setItem(ItemDataIfc item) {
    this.item = item;
  }

  public Long getAttachmentType() {
    return ItemAttachmentIfc.ITEM_ATTACHMENT;
  }

  public void setAttachmentType(Long attachmentType) {
  }


}
