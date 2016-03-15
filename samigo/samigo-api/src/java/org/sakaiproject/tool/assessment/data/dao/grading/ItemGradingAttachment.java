/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.assessment.data.dao.grading;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;

/**
 * 
 * Represents an attachment to an item grading object
 * 
 */
public class ItemGradingAttachment extends GradingAttachmentData implements
		Serializable {
	private static final long serialVersionUID = 5236325714234687092L;
	private ItemGradingData itemGrading;

	public ItemGradingAttachment() {
	}

	public ItemGradingAttachment(Long attachmentId,
			ItemGradingData itemGrading, String resourceId, String filename,
			String mimeType, Long fileSize, String description,
			String location, Boolean isLink, Integer status, String createdBy,
			Date createdDate, String lastModifiedBy, Date lastModifiedDate) {
		super(attachmentId, resourceId,
				AssessmentAttachmentIfc.ITEMGRADING_ATTACHMENT, filename,
				mimeType, fileSize, description, location, isLink, status,
				createdBy, createdDate, lastModifiedBy, lastModifiedDate);
		this.itemGrading = itemGrading;
	}
	
	public ItemGradingAttachment(GradingAttachmentData attach, ItemGradingData itemGrading) {
		super(attach.getAttachmentId(), attach.getResourceId(),
				AssessmentAttachmentIfc.ITEMGRADING_ATTACHMENT, attach.getFilename(),
				attach.getMimeType(), attach.getFileSize(), attach.getDescription(), attach.getLocation(), attach.getIsLink(), attach.getStatus(),
				attach.getCreatedBy(), attach.getCreatedDate(), attach.getLastModifiedBy(), attach.getLastModifiedDate());
		this.itemGrading = itemGrading;
	}

	public ItemGradingData getItemGrading() {
		return itemGrading;
	}

	public void setItemGrading(ItemGradingData itemGrading) {
		this.itemGrading = itemGrading;
	}

	public Long getAttachmentType() {
		return AttachmentIfc.ITEMGRADING_ATTACHMENT;
	}

	public void setAttachmentType(Long attachmentType) {
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemGradingAttachment other = (ItemGradingAttachment) obj;
		if (itemGrading == null) {
			if (other.itemGrading != null)
				return false;
		} else if (!itemGrading.equals(other.itemGrading))
			return false;
		return true;
	}
}
