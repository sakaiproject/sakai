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

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

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

	@Override
	protected void init() {
        if (this.assignmentId != null) {
            this.assignment = getGradebookManager().getAssignment(this.assignmentId);
            if (this.assignment == null) {
                // The assignment might have been removed since this link was set up.
                if (log.isWarnEnabled()) {
					log.warn("No assignmentId=" + this.assignmentId + " in gradebookUid " + getGradebookUid());
				}

                // TODO Deliver an appropriate message.
            }
        }
    }

    public String removeAssignment() {
        if(this.removeConfirmed) {
            try {
                getGradebookManager().removeAssignment(this.assignmentId);
            } catch (final StaleObjectModificationException e) {
                FacesUtil.addErrorMessage(getLocalizedString("remove_assignment_locking_failure"));
                return null;
            }
            final String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
			getGradebookBean().postEvent("gradebook.deleteItem",
					"/gradebook/" + getGradebookId() + "/" + this.assignment.getName() + "/" + authzLevel);
            FacesUtil.addRedirectSafeMessage(getLocalizedString("remove_assignment_success", new String[] {this.assignment.getName()}));
            return "overview";
        } else {
            FacesUtil.addErrorMessage(getLocalizedString("remove_assignment_confirmation_required"));
            return null;
        }
    }

    public String cancel() {
        // Go back to the GradebookAssignment Details page for this assignment.
        final AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
        assignmentDetailsBean.setAssignmentId(this.assignmentId);
        return "assignmentDetails";
    }

    public GradebookAssignment getAssignment() {
        return this.assignment;
    }
    public void setAssignment(final GradebookAssignment assignment) {
        this.assignment = assignment;
    }

    /**
	 * @return Returns the assignmentId.
	 */
	public Long getAssignmentId() {
		return this.assignmentId;
	}
	/**
	 * @param assignmentId The assignmentId to set.
	 */
	public void setAssignmentId(final Long assignmentId) {
		this.assignmentId = assignmentId;
	}
	/**
	 * @return Returns the removeConfirmed.
	 */
	public boolean isRemoveConfirmed() {
		return this.removeConfirmed;
	}
	/**
	 * @param removeConfirmed The removeConfirmed to set.
	 */
	public void setRemoveConfirmed(final boolean removeConfirmed) {
		this.removeConfirmed = removeConfirmed;
	}
}
