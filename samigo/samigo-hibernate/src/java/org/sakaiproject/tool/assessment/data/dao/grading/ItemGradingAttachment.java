package org.sakaiproject.tool.assessment.data.dao.grading;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;


public class ItemGradingAttachment extends GradingAttachmentData 
	implements Serializable, ItemGradingAttachmentIfc
{
	private static final long serialVersionUID = 5236325714234687092L;
	private ItemGradingIfc itemGrading;

    public ItemGradingAttachment()  { }

	public ItemGradingAttachment(Long attachmentId, ItemGradingIfc itemGrading, String resourceId,
			String filename, String mimeType,
			Long fileSize, String description, String location,
			Boolean isLink, Integer status, String createdBy, Date createdDate,
			String lastModifiedBy, Date lastModifiedDate){
		super(attachmentId, resourceId, AssessmentAttachmentIfc.ITEMGRADING_ATTACHMENT,
				filename, mimeType,
				fileSize, description, location, isLink, status,
				createdBy, createdDate, lastModifiedBy, lastModifiedDate);
		this.itemGrading = itemGrading;
	}

	public ItemGradingIfc getItemGrading() {
		return itemGrading;
	}

	public void setItemGrading(ItemGradingIfc itemGrading) {
		this.itemGrading = itemGrading;
	}

	public Long getAttachmentType() {
		return AttachmentIfc.ITEMGRADING_ATTACHMENT;
	}

	public void setAttachmentType(Long attachmentType) {
	}
}
