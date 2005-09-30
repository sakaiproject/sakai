/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class CourseGradeDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(CourseGradeDetailsBean.class);

	// View maintenance fields - serializable.
	private List scoreRows;
//	private Map changedGrades;
//	private boolean workInProgress;

	// Controller fields - transient.
	private CourseGrade courseGrade;
    private GradeRecordSet gradeRecordSet;
    private GradeMapping gradeMapping;
    private double totalPoints;

	public class ScoreRow implements Serializable {
        private EnrollmentRecord enrollment;
        private CourseGradeRecord courseGradeRecord;

		public ScoreRow() {
		}
		public ScoreRow(EnrollmentRecord enrollment) {
            this.enrollment = enrollment;
			courseGradeRecord = (CourseGradeRecord)gradeRecordSet.getGradeRecord(enrollment.getUser().getUserUid());
			if (courseGradeRecord == null) {
				// Since we're using the domain object to transport UI input, we need
				// to create it, even if we don't end up storing it in the database.
				courseGradeRecord = new CourseGradeRecord(courseGrade, enrollment.getUser().getUserUid(), null);
				gradeRecordSet.addGradeRecord(courseGradeRecord);
			}
		}

        public String getCalculatedLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullAutoCalculatedGrade());
        }

        public Double getCalculatedPercentGrade() {
        	return courseGradeRecord.getNonNullAutoCalculatedGrade();
        }

        public CourseGradeRecord getCourseGradeRecord() {
            return courseGradeRecord;
        }
        public void setCourseGradeRecord(CourseGradeRecord courseGradeRecord) {
            this.courseGradeRecord = courseGradeRecord;
        }
        public EnrollmentRecord getEnrollment() {
            return enrollment;
        }
	}

	protected void init() {
		super.init();

		// We save the "changedGrades" map across the request thread to deal with
		// the following scenario:
		//
		//   1) The instructor enters several grades, one of which is invalid,
		//      and saves.
		//   2) The component model is updated with the request values. Validation
		//      fails for the invalid grade. Value change listeners are called for
		//      the valid grades. The save action is not reached.
		//   3) The instructor is reshown the form with its inputs, with an error
		//      message. After fixing the error, the instructor saves.
		//   4) JSF only calls value change listeners for the fixed grades, not for
		//      the previously entered (but unsaved) valid grades.

		// Clear view state.
		scoreRows = new ArrayList();
		courseGrade = getGradeManager().getCourseGradeWithStats(getGradebookId());

        gradeMapping = getGradebook().getSelectedGradeMapping();
        totalPoints = getGradeManager().getTotalPoints(getGradebookId());

		// Set up score rows.
		Map enrollmentMap = getOrderedEnrollmentMap();
		List gradeRecords = getGradeManager().getPointsEarnedSortedGradeRecords(courseGrade, enrollmentMap.keySet());
		List workingEnrollments = new ArrayList(enrollmentMap.values());

		if (!isEnrollmentSort()) {
			// Need to sort and page based on a scores column.
			String sortColumn = getSortColumn();
			Comparator comparator = null;
			if (sortColumn.equals(CourseGrade.SORT_BY_CALCULATED_GRADE) ||
                sortColumn.equals(CourseGrade.SORT_BY_POINTS_EARNED)) {
                comparator = CourseGradeRecord.calcComparator;
            } else if (sortColumn.equals(CourseGrade.SORT_BY_OVERRIDE_GRADE)) {
            	comparator = CourseGradeRecord.getOverrideComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            if (comparator != null) {
	            Collections.sort(gradeRecords, comparator);
	        }

			List scoreSortedEnrollments = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
				scoreSortedEnrollments.add(enrollmentMap.get(cgr.getStudentId()));
			}

			// Put enrollments with no scores at the beginning of the final list.
			workingEnrollments.removeAll(scoreSortedEnrollments);

			// Add all sorted enrollments with scores into the final list
			workingEnrollments.addAll(scoreSortedEnrollments);

			workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
		}
		gradeRecordSet = new GradeRecordSet(courseGrade);
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
			gradeRecordSet.addGradeRecord(gradeRecord);
		}
		for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
            scoreRows.add(new ScoreRow(enrollment));
		}

//		workInProgress = true;
	}

    public CourseGrade getCourseGrade() {
        return courseGrade;
    }
    public GradeRecordSet getGradeRecordSet() {
        return gradeRecordSet;
    }
    public double getTotalPoints() {
        return totalPoints;
    }
	/**
	 * Action listener to update grades.
	 * NOTE: No transient fields are available yet.
	 */
	public void processUpdateGrades(ActionEvent event) {
		try {
			saveGrades();
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
	}

	private void saveGrades() throws StaleObjectModificationException {
		getGradeManager().updateCourseGradeRecords(gradeRecordSet);

		// Clear the work-in-progress indicator so that the user can
		// start fresh.
//		workInProgress = false;

		// Let the user know.
		FacesUtil.addMessage(getLocalizedString("course_grade_details_grades_saved"));
	}

	private ScoreRow getScoreRow(UIComponent component) {
		UIData gradingTable = (UIData)component.findComponent("gradingTable");
		return (ScoreRow)gradingTable.getRowData();
	}

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

    // Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isCourseGradeDetailsTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setCourseGradeDetailsTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getCourseGradeDetailsTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setCourseGradeDetailsTableSortColumn(sortColumn);
    }
}



