/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/ui/EditAssignmentBean.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
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
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class EditAssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(EditAssignmentBean.class);

	// View maintenance fields - serializable.
	private Long assignmentId;
	private String title;
	private Double points;
	private Date dueDate;

	// Controller fields - transient.
	private transient Assignment assignment;

	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("init assignment=" + assignment);
		if (logger.isDebugEnabled()) logger.debug("    assignmentId=" + assignmentId + ", title=" + title + ", points=" + points + ", dueDate=" + dueDate);

		// Clear view state.
		title = null;
		points = null;
		dueDate = null;

		if (assignmentId != null) {
			assignment = (Assignment)getGradableObjectManager().getGradableObjectWithStats(assignmentId);
			if (assignment != null) {
				title = assignment.getName();
				points = assignment.getPointsForDisplay();
				dueDate = assignment.getDateForDisplay();
			} else {
				// The assignment might have been removed since this link was set up.
				if (logger.isWarnEnabled()) logger.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());

				// TODO Deliver an appropriate message.
			}
		}
	}

	public String updateAssignment() {
		try {
			getGradableObjectManager().updateAssignment(assignmentId, title, points, dueDate);
			String messageKey = getGradeManager().isEnteredAssignmentScores(assignmentId) ?
				"edit_assignment_save_scored" :
				"edit_assignment_save";
            FacesUtil.addRedirectSafeMessage(getLocalizedString(messageKey, new String[] {title}));
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

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public Double getPoints() {
		return points;
	}
	public void setPoints(Double points) {
		this.points = points;
	}

	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/ui/EditAssignmentBean.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
