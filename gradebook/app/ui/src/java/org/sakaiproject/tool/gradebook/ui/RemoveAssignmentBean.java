/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Backing Bean for removing assignments from a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class RemoveAssignmentBean extends GradebookDependentBean implements Serializable {
    // View maintenance fields - serializable.
    private Long assignmentId;
    private boolean removeConfirmed;
    private GradebookAssignment assignment;

    protected void init() {
        if (assignmentId != null) {
            assignment = getGradebookManager().getAssignment(assignmentId);
            if (assignment == null) {
                // The assignment might have been removed since this link was set up.
                if (log.isWarnEnabled()) log.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());

                // TODO Deliver an appropriate message.
            }
        }
    }

    public String removeAssignment() {
        if(removeConfirmed) {
            try {
                getGradebookManager().removeAssignment(assignmentId);
            } catch (StaleObjectModificationException e) {
                FacesUtil.addErrorMessage(getLocalizedString("remove_assignment_locking_failure"));
                return null;
            }
            String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
            getGradebookBean().getEventTrackingService().postEvent("gradebook.deleteItem","/gradebook/"+getGradebookId()+"/"+assignment.getName()+"/"+authzLevel);
            FacesUtil.addRedirectSafeMessage(getLocalizedString("remove_assignment_success", new String[] {assignment.getName()}));
            return "overview";
        } else {
            FacesUtil.addErrorMessage(getLocalizedString("remove_assignment_confirmation_required"));
            return null;
        }
    }

    public String cancel() {
        // Go back to the GradebookAssignment Details page for this assignment.
        AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
        assignmentDetailsBean.setAssignmentId(assignmentId);
        return "assignmentDetails";
    }

    public GradebookAssignment getAssignment() {
        return assignment;
    }
    public void setAssignment(GradebookAssignment assignment) {
        this.assignment = assignment;
    }

    /**
	 * @return Returns the assignmentId.
	 */
	public Long getAssignmentId() {
		return assignmentId;
	}
	/**
	 * @param assignmentId The assignmentId to set.
	 */
	public void setAssignmentId(Long assignmentId) {
		this.assignmentId = assignmentId;
	}
	/**
	 * @return Returns the removeConfirmed.
	 */
	public boolean isRemoveConfirmed() {
		return removeConfirmed;
	}
	/**
	 * @param removeConfirmed The removeConfirmed to set.
	 */
	public void setRemoveConfirmed(boolean removeConfirmed) {
		this.removeConfirmed = removeConfirmed;
	}
}
