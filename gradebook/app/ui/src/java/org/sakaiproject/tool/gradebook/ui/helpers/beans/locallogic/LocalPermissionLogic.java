package org.sakaiproject.tool.gradebook.ui.helpers.beans.locallogic;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.*;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;

import uk.org.ponder.rsf.viewstate.ViewParameters;

public class LocalPermissionLogic {
	
	private GradebookService gradebookService;
	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}
	
	public Boolean checkCurrentUserHasViewPermission(ViewParameters incoming) {
		
		if (GradebookItemProducer.VIEW_ID.equals(incoming.viewID)) {
			String contextId = ((GradebookItemViewParams) incoming).contextId;
			return gradebookService.currentUserHasEditPerm(contextId);
			
		} else if (GradeGradebookItemProducer.VIEW_ID.equals(incoming.viewID)) {
		    String gradebookUid = ((GradeGradebookItemViewParams) incoming).contextId;
			String userId = ((GradeGradebookItemViewParams) incoming).userId;
			Long gradableObjectId = ((GradeGradebookItemViewParams) incoming).assignmentId;
			return gradebookService.isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, userId);
			
		} else if (FinishedHelperProducer.VIEW_ID.equals(incoming.viewID)) {
			return Boolean.TRUE;
			
		}
		
		return Boolean.FALSE;
	}
}