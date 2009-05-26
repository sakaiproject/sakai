package org.sakaiproject.tool.assessment.data.ifc.grading;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;

public interface ItemGradingAttachmentIfc extends AttachmentIfc {
	Long getAttachmentType();

	void setAttachmentType(Long attachmentType);

	ItemGradingIfc getItemGrading();

	void setItemGrading(ItemGradingIfc itemGrading);

}
