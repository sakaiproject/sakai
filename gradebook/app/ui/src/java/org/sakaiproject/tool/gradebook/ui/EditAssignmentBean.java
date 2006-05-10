/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class EditAssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(EditAssignmentBean.class);

	private Long assignmentId;
    private Assignment assignment;

	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("init assignment=" + assignment);

		if (assignment == null) {
			assignment = (Assignment)getGradebookManager().getGradableObject(assignmentId);
			if (assignment == null) {
				// The assignment might have been removed since this link was set up.
				if (logger.isWarnEnabled()) logger.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());

				// TODO Deliver an appropriate message.
			}
		}
	}

	public String updateAssignment() {
		try {
			getGradebookManager().updateAssignment(assignment);
			String messageKey = getGradebookManager().isEnteredAssignmentScores(assignmentId) ?
				"edit_assignment_save_scored" :
				"edit_assignment_save";
            FacesUtil.addRedirectSafeMessage(getLocalizedString(messageKey, new String[] {assignment.getName()}));
		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
            return "failure";
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_locking_failure"));
            return "failure";
		}
		return navigateToAssignmentDetails();
	}

	public String cancel() {
		return navigateToAssignmentDetails();
	}

	private String navigateToAssignmentDetails() {
		// Go back to the Assignment Details page for this assignment.
		AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
		assignmentDetailsBean.setAssignmentId(assignmentId);
		return "assignmentDetails";
	}

	/**
	 * View maintenance methods.
	 */
	public Long getAssignmentId() {
		if (logger.isDebugEnabled()) logger.debug("getAssignmentId " + assignmentId);
		return assignmentId;
	}
	public void setAssignmentId(Long assignmentId) {
		if (logger.isDebugEnabled()) logger.debug("setAssignmentId " + assignmentId);
		if (assignmentId != null) {
			this.assignmentId = assignmentId;
		}
	}

    public Assignment getAssignment() {
        if (logger.isDebugEnabled()) logger.debug("getAssignment " + assignment);
        return assignment;
    }
}



