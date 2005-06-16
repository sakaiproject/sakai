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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.facades.Enrollment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class CourseGradeDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(CourseGradeDetailsBean.class);

	// View maintenance fields - serializable.
	private List scoreRows;
	private Map changedGrades;
	private boolean workInProgress;

	// Controller fields - transient.
	private transient CourseGrade courseGrade;
	private transient Map scores;
    private transient GradeMapping gradeMapping;
    private transient double totalPoints;

	public class ScoreRow implements Serializable {
		private String studentUid;
		private String sortName;	// Transient except for validation errors
		private String displayUid;	// Transient except for validation errors
		private Double score;	// Transient except for validation errors
        private String calculatedGrade;  // Transient except for validation errors
		private String enteredGrade;

		public ScoreRow() {
		}
		public ScoreRow(Enrollment enrollment) {
			studentUid = enrollment.getUser().getUserUid();
			sortName = enrollment.getUser().getSortName();
			displayUid = enrollment.getUser().getDisplayUid();
			CourseGradeRecord gradeRecord = (CourseGradeRecord)scores.get(studentUid);
            if(gradeRecord == null) {
                List availableGrades = getGradebook().getSelectedGradeMapping().getGrades();
                calculatedGrade = (String)availableGrades.get(availableGrades.size()-1); // Get the last/lowest grade
            } else {
                score = (Double)gradeRecord.getPointsEarned();
                enteredGrade = gradeRecord.getEnteredGrade();
                calculatedGrade = gradeRecord.getDisplayGrade();
            }

            if (enteredGrade == null) {
				enteredGrade = "";
			}
		}

        public String getCalculatedLetterGrade() {
            if(score == null) {
                return gradeMapping.getGrade(new Double(0));
            }
            Double val = new Double(score.doubleValue() / totalPoints * 100);
            return gradeMapping.getGrade(val);
        }

        public Double getCalculatedPercentGrade() {
            if(score == null) {
                return new Double(0);
            }
            return new Double(score.doubleValue() / totalPoints * 100);
        }

		public String getStudentUid() {
			return studentUid;
		}
		public void setStudentUid(String studentUid) {
			this.studentUid = studentUid;
		}
		public String getEnteredGrade() {
			return enteredGrade;
		}
		public void setEnteredGrade(String grade) {
			this.enteredGrade = grade;
		}

		public String getSortName() {
			return sortName;
		}
		public void setSortName(String sortName) {
			this.sortName = sortName;
		}
		public String getDisplayUid() {
			return displayUid;
		}
		public void setDisplayUid(String displayUid) {
			this.displayUid = displayUid;
		}
		public Double getScore() {
			return score;
		}
		public void setScore(Double score) {
			this.score = score;
		}
		public String getCalculatedGrade() {
			return calculatedGrade;
		}
		public void setCalculatedGrade(String calculatedGrade) {
			this.calculatedGrade = calculatedGrade;
		}
	}

	protected void init() {
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
		if (!workInProgress) {
			changedGrades = new HashMap();
		}

		// Clear view state.
		scoreRows = new ArrayList();

		courseGrade = getGradableObjectManager().getCourseGradeWithStats(getGradebookId());

        gradeMapping = getGradebook().getSelectedGradeMapping();
        totalPoints = getGradableObjectManager().getTotalPoints(getGradebookId());

        // Set up searching and paging
        String defaultSearchString = getLocalizedString("search_default_student_search_string");
        if(StringUtils.trimToNull(searchString) == null) {
            searchString = defaultSearchString;
        }

		// Set up score rows.
		Map enrollmentMap = getOrderedEnrollmentMap();

		List gradeRecords;
		if (isFilteredSearch() || isEnrollmentSort()) {
			gradeRecords = getGradeManager().getPointsEarnedSortedGradeRecords(courseGrade, enrollmentMap.keySet());
		} else {
			gradeRecords = getGradeManager().getPointsEarnedSortedGradeRecords(courseGrade);
		}

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

		scores = new HashMap();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
			scores.put(gradeRecord.getStudentId(), gradeRecord);
		}
		for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
			Enrollment enrollment = (Enrollment)iter.next();
            scoreRows.add(new ScoreRow(enrollment));
		}

		workInProgress = true;
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
		if (logger.isInfoEnabled()) logger.info("saveGrades for gradebook=" + getGradebookUid() + ", user=" + getUserUid() + ", changing " + changedGrades.size() + " grades");

		// Only save changed grades.
		getGradeManager().updateCourseGradeRecords(getGradebookId(), changedGrades);

		// Clear the work-in-progress indicator so that the user can
		// start fresh.
		workInProgress = false;

		// Let the user know.
		FacesUtil.addMessage(getLocalizedString("course_grade_details_grades_saved"));
	}

	private ScoreRow getScoreRow(UIComponent component) {
		UIData gradingTable = (UIData)component.findComponent("gradingTable");
		return (ScoreRow)gradingTable.getRowData();
	}

	/**
	 * Used to keep track of which course grades were actually changed.  We only
	 * send the changed grades to the gradeManager service.
	 * TODO The application currently assumes a difference between nulls
	 * and blanks.
	 */
	public void courseGradeChanged(ValueChangeEvent event) {
		if (logger.isDebugEnabled()) logger.debug("courseGradeChanged: old value=" + event.getOldValue()	+ ", new value=" + event.getNewValue());
		ScoreRow scoreRow = getScoreRow(event.getComponent());
		String studentUid = scoreRow.getStudentUid();
		String newGrade = StringUtils.trimToNull((String)event.getNewValue());
		changedGrades.put(studentUid, newGrade);
	}

	/**
	 * View maintenance methods.
	 */
	public String getTitle() {
		return courseGrade.getName();
	}

	public Double getPoints() {
		return courseGrade.getPointsForDisplay();
	}

    public Double getMean() {
        Double formattedMean = courseGrade.getFormattedMean();
        if(formattedMean == null) {
            return null;
        } else {
            return new Double(courseGrade.getFormattedMean().doubleValue() / 100);
        }
    }

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

    public boolean isEmptyEnrollments() {
        return emptyEnrollments;
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
