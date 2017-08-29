/**
 * Copyright (c) 2003-2008 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.gradebook.ui.helpers.beans.locallogic;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.*;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;

import uk.org.ponder.rsf.builtin.UVBProducer;
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
			
		} else if (UVBProducer.VIEW_ID.equals(incoming.viewID)) {
		    return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}
}