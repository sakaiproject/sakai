/*******************************************************************************
 * Copyright (c) 2006, 2007 The Regents of the University of California
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Provides data for the student view of the gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class InstructorViewBean extends ViewByStudentBean implements Serializable {
	private static Log logger = LogFactory.getLog(InstructorViewBean.class);

	private EnrollmentRecord previousStudent;
	private EnrollmentRecord nextStudent;

	// parameters passed to the page
	private String returnToPage;
	private String assignmentId; // for returning to specific gradebook item

	/**
	 * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
	 */
	public void init() {
		previousStudent = null;
		nextStudent = null;
		
		setIsInstructorView(true);

		if (getStudentUid() != null) {
			super.init();
            
			// set up the "next" and "previous" student navigation
			// TODO preserve filter/sort status from previous page
			List enrollees = getAvailableEnrollments();

			if (enrollees != null && enrollees.size() > 1) {
				for (int i=0; i < enrollees.size(); i++) {
					EnrollmentRecord enrollee = (EnrollmentRecord)enrollees.get(i);
					if (enrollee.getUser().getUserUid().equals(getStudentUid())) {
						if (i-1 > 0) {
							previousStudent = (EnrollmentRecord) enrollees.get(i-1);
						}
						if (i+1 < enrollees.size()) {
							nextStudent = (EnrollmentRecord) enrollees.get(i+1);
						}

						break;
					}
				}
			}
		}
	}
	
	

	// Navigation
	public EnrollmentRecord getPreviousStudent() {
		return previousStudent;
	}
	public EnrollmentRecord getNextStudent() {
		return nextStudent;
	}
	
	public boolean isFirst() {
		return previousStudent == null;
	}
	public boolean isLast() {
		return nextStudent == null;
	}

	/**
	 * @return text for "Return to" button
	 */
	public String getReturnToPageName() {
		String pageTitle;
		if (returnToPage.equals("assignmentDetails"))
			pageTitle = getLocalizedString("assignment_details_page_title");
		else
			pageTitle = getLocalizedString("roster_page_title");
		
		return getLocalizedString("inst_view_return_to", new String[] {pageTitle});
	}

	/**
	 * Action listener to view a different student
	 */
	public void processStudentUidChange(ActionEvent event) {
		Map params = FacesUtil.getEventParameterMap(event);
		if (logger.isDebugEnabled()) 
			logger.debug("processStudentUidChange params=" + params + ", current studentUid=" + getStudentUid());
		String idParam = (String)params.get("studentUid");
		if (idParam != null) {
			setStudentUid(idParam);
		}
	}
	
	/**
	 * Action listener to update scores.
	 */
	public void processUpdateScores() {
		try {
			saveScores();
		} catch (StaleObjectModificationException e) {
            FacesUtil.addErrorMessage(getLocalizedString("assignment_details_locking_failure"));
		}
	}
	
	/**
	 * Save the input scores for the user
	 * @throws StaleObjectModificationException
	 */
	public void saveScores() throws StaleObjectModificationException {
        if (logger.isInfoEnabled()) logger.info("saveScores for " + getUserDisplayName());

        // first, determine which scores were updated
        List updatedGradeRecords = new ArrayList();
        if (getGradebookItems() != null) {
        	Iterator itemIter = getGradebookItems().iterator();
        	while (itemIter.hasNext()) {
        		Object item = itemIter.next();
        		if (item instanceof AssignmentGradeRow) {
        			AssignmentGradeRow gradeRow = (AssignmentGradeRow) item;
        			AssignmentGradeRecord gradeRecord = gradeRow.getGradeRecord();
        			
        			if (gradeRecord == null && gradeRow.getScore() != null) {
        				// this is a new grade
        				gradeRecord = new AssignmentGradeRecord(gradeRow.getAssociatedAssignment(), getStudentUid(), null);
        			}
        			if (gradeRecord != null) {
        				Double originalScore = null;
        				if (getGradeEntryByPoints()) {
        					originalScore = gradeRecord.getPointsEarned();
        				} else if (getGradeEntryByPercent()) {
        					originalScore = gradeRecord.getGradeAsPercentage();
        				}
        				if (originalScore != null) {
        					// truncate to two decimals for more accurate comparison
        					originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
        				}
        				Double newScore = gradeRow.getScore();
        				if ( (originalScore != null && !originalScore.equals(newScore)) ||
        						(originalScore == null && newScore != null) ) {
        					gradeRecord.setPointsEarned(newScore);
        					updatedGradeRecords.add(gradeRecord);
        				}
        			} 
        		}
        	}
        }
		
        Set excessiveScores = getGradebookManager().updateStudentGradeRecords(updatedGradeRecords, getGradebook().getGrade_type());

        if(updatedGradeRecords.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.updateItemScores","/gradebook/"+getGradebookId()+"/"+updatedGradeRecords.size()+"/"+getAuthzLevel());
        }
        
        String messageKey = (excessiveScores.size() > 0) ?
                "inst_view_scores_saved_excessive" :
                "inst_view_scores_saved";

        // Let the user know.
        FacesUtil.addMessage(getLocalizedString(messageKey));
    }
	
	/**
	 * View maintenance methods.
	 */
	
	public String getReturnToPage() {
		if (returnToPage == null)
			returnToPage = "roster";
		return returnToPage;
	}
	public void setReturnToPage(String returnToPage) {
		this.returnToPage = returnToPage;
	}
	public String getAssignmentId() {
		return assignmentId;
	}
	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}
}



