/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Backing Bean for removing assignments from a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class RemoveAssignmentBean extends GradebookDependentBean implements Serializable {
    private static final Log logger = LogFactory.getLog(RemoveAssignmentBean.class);

    // View maintenance fields - serializable.
    private Long assignmentId;
    private boolean removeConfirmed;
    private Assignment assignment;

    protected void init() {
        if (assignmentId != null) {
            assignment = (Assignment)getGradeManager().getGradableObjectWithStats(assignmentId);
            if (assignment == null) {
                // The assignment might have been removed since this link was set up.
                if (logger.isWarnEnabled()) logger.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());

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
            FacesUtil.addRedirectSafeMessage(getLocalizedString("remove_assignment_success", new String[] {assignment.getName()}));
            return "overview";
        } else {
            FacesUtil.addErrorMessage(getLocalizedString("remove_assignment_confirmation_required"));
            return null;
        }
    }

    public String cancel() {
        // Go back to the Assignment Details page for this assignment.
        AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
        assignmentDetailsBean.setAssignmentId(assignmentId);
        return "assignmentDetails";
    }

    public Assignment getAssignment() {
        return assignment;
    }
    public void setAssignment(Assignment assignment) {
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
