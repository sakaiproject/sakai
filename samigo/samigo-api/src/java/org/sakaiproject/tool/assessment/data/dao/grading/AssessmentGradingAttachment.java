/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.data.dao.grading;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;

/**
 * 
 * Represents an attachment to an assessment grading object
 * 
 */
public class AssessmentGradingAttachment extends GradingAttachmentData implements
		Serializable {
	private static final long serialVersionUID = 5236325714234687092L;
	private AssessmentGradingData assessmentGrading;

	public AssessmentGradingAttachment() {
	}

	public AssessmentGradingAttachment(Long attachmentId,
			AssessmentGradingData assessmentGrading, String resourceId, String filename,
			String mimeType, Long fileSize, String description,
			String location, Boolean isLink, Integer status, String createdBy,
			Date createdDate, String lastModifiedBy, Date lastModifiedDate) {
		super(attachmentId, resourceId,
				AssessmentAttachmentIfc.ASSESSMENTGRADING_ATTACHMENT, filename,
				mimeType, fileSize, description, location, isLink, status,
				createdBy, createdDate, lastModifiedBy, lastModifiedDate);
		this.assessmentGrading = assessmentGrading;
	}
	
	public AssessmentGradingAttachment(GradingAttachmentData attach, AssessmentGradingData assessmentGrading) {
		super(attach.getAttachmentId(), attach.getResourceId(),
				AssessmentAttachmentIfc.ASSESSMENTGRADING_ATTACHMENT, attach.getFilename(),
				attach.getMimeType(), attach.getFileSize(), attach.getDescription(), attach.getLocation(), attach.getIsLink(), attach.getStatus(),
				attach.getCreatedBy(), attach.getCreatedDate(), attach.getLastModifiedBy(), attach.getLastModifiedDate());
		this.assessmentGrading = assessmentGrading;
	}

	public AssessmentGradingData getAssessmentGrading() {
		return assessmentGrading;
	}

	public void setAssessmentGrading(AssessmentGradingData assessmentGrading) {
		this.assessmentGrading = assessmentGrading;
	}

	public Long getAttachmentType() {
		return AttachmentIfc.ASSESSMENTGRADING_ATTACHMENT;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssessmentGradingAttachment other = (AssessmentGradingAttachment) obj;
		if (assessmentGrading == null) {
			if (other.assessmentGrading != null)
				return false;
		} else if (!assessmentGrading.equals(other.assessmentGrading))
			return false;
		return true;
	}
}
