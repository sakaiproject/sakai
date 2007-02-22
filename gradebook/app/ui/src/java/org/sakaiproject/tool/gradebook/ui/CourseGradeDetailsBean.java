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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class CourseGradeDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(CourseGradeDetailsBean.class);

	// View maintenance fields - serializable.
	private List scoreRows;

	// Controller fields - transient.
	private CourseGrade courseGrade;
    private List updatedGradeRecords;
    private GradeMapping gradeMapping;
    private double totalPoints;

	public class ScoreRow implements Serializable {
        private EnrollmentRecord enrollment;
        private CourseGradeRecord courseGradeRecord;
        private List eventRows;

		public ScoreRow() {
		}
		public ScoreRow(EnrollmentRecord enrollment, CourseGradeRecord courseGradeRecord, List gradingEvents) {
            this.enrollment = enrollment;
			this.courseGradeRecord = courseGradeRecord;

            eventRows = new ArrayList();
            for (Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	GradingEvent gradingEvent = (GradingEvent)iter.next();
            	eventRows.add(new GradingEventRow(gradingEvent));
            }
		}

        public String getCalculatedLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullAutoCalculatedGrade());
        }

        public Double getCalculatedPercentGrade() {
        	return new Double(courseGradeRecord.getNonNullAutoCalculatedGrade().doubleValue() / 100.);
        }

        public CourseGradeRecord getCourseGradeRecord() {
            return courseGradeRecord;
        }
        public void setCourseGradeRecord(CourseGradeRecord courseGradeRecord) {
            this.courseGradeRecord = courseGradeRecord;
        }

        public String getEnteredGrade() {
            return courseGradeRecord.getEnteredGrade();
        }
        public void setEnteredGrade(String enteredGrade) {
        	String originalEnteredGrade = courseGradeRecord.getEnteredGrade();
        	if (!StringUtils.equals(enteredGrade, originalEnteredGrade)) {
        		courseGradeRecord.setEnteredGrade(enteredGrade);
        		updatedGradeRecords.add(courseGradeRecord);
        	}
        }

        public EnrollmentRecord getEnrollment() {
            return enrollment;
        }

        public List getEventRows() {
        	return eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("course_grade_details_log_title", new String[] {enrollment.getUser().getDisplayName()});
        }
	}

	protected void init() {
		super.init();

		// Clear view state.
		scoreRows = new ArrayList();
		courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		updatedGradeRecords = new ArrayList();

        gradeMapping = getGradebook().getSelectedGradeMapping();
        totalPoints = getGradebookManager().getTotalPoints(getGradebookId());

		// Set up score rows.
		Map enrollmentMap = getOrderedEnrollmentMap();
		List studentUids = new ArrayList(enrollmentMap.keySet());
		List gradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecordsWithStats(courseGrade, studentUids);
		
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

			List scoreSortedStudentUids = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
				scoreSortedStudentUids.add(cgr.getStudentId());
			}

			// Put enrollments with no scores at the beginning of the final list.
			studentUids.removeAll(scoreSortedStudentUids);

			// Add all sorted enrollments with scores into the final list
			studentUids.addAll(scoreSortedStudentUids);

			studentUids = finalizeSortingAndPaging(studentUids);
		}

		// Get all of the grading events for these enrollments on this assignment
		GradingEvents allEvents = getGradebookManager().getGradingEvents(courseGrade, studentUids);

		Map gradeRecordMap = new HashMap();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
			if (studentUids.contains(gradeRecord.getStudentId())) {
				gradeRecordMap.put(gradeRecord.getStudentId(), gradeRecord);
			}
		}

        // If the table is not being sorted by enrollment information, then
        // we had to gather grade records for all students to set up the
        // current page. In that case, eliminate the undisplayed grade records
        // to reduce data contention.
        if (!isEnrollmentSort()) {
        	gradeRecords = new ArrayList(gradeRecordMap.values());
        }
			
		for (Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			EnrollmentRecord enrollment = (EnrollmentRecord)enrollmentMap.get(studentUid);
			CourseGradeRecord gradeRecord = (CourseGradeRecord)gradeRecordMap.get(studentUid);
            if(gradeRecord == null) {
                gradeRecord = new CourseGradeRecord(courseGrade, studentUid);
                gradeRecords.add(gradeRecord);
            }
			
			scoreRows.add(new ScoreRow(enrollment, gradeRecord, allEvents.getEvents(studentUid)));
		}
	}

    public CourseGrade getCourseGrade() {
        return courseGrade;
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
		getGradebookManager().updateCourseGradeRecords(courseGrade, updatedGradeRecords);

		// Let the user know.
		FacesUtil.addMessage(getLocalizedString("course_grade_details_grades_saved"));
	}
	
	// Download spreadsheet of course grades. It's very likely that insitutions will
	// want to customize this somewhere along the way.
	
    public void exportCsv(ActionEvent event){
        if(logger.isInfoEnabled()) logger.info("exporting course grade as CSV for gradebook " + getGradebookUid());
        FacesUtil.downloadSpreadsheetData(getSpreadsheetData(), 
        		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
        		new SpreadsheetDataFileWriterCsv());
    }

    public void exportExcel(ActionEvent event){
        if(logger.isInfoEnabled()) logger.info("exporting course grade as Excel for gradebook " + getGradebookUid());
        FacesUtil.downloadSpreadsheetData(getSpreadsheetData(), 
        		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
        		new SpreadsheetDataFileWriterXls());
    }
    
    private List<List<Object>> getSpreadsheetData() {
    	// Get the full list of filtered enrollments and scores (not just the current page's worth).
    	List filteredEnrollments = getWorkingEnrollments();
    	Collections.sort(filteredEnrollments, ENROLLMENT_NAME_COMPARATOR);
    	Set<String> studentUids = new HashSet<String>();
    	for (Iterator iter = filteredEnrollments.iterator(); iter.hasNext(); ) {
    		EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
    		studentUids.add(enrollment.getUser().getUserUid());
    	}

		CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
		Map filteredGradesMap = new HashMap();
		getGradebookManager().addToGradeRecordMap(filteredGradesMap, courseGradeRecords);
     	return getSpreadsheetData(filteredEnrollments, courseGrade, filteredGradesMap);
    }
    
    private List<List<Object>> getSpreadsheetData(List enrollments, CourseGrade courseGrade, Map gradesMap) {
    	List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

    	// Build column headers.
        List<Object> headerRow = new ArrayList<Object>();
        headerRow.add(getLocalizedString("export_student_id"));
        headerRow.add(getLocalizedString("export_student_name"));
        headerRow.add(getLocalizedString("course_grade_details_course_grade_column_name"));
        spreadsheetData.add(headerRow);
        
        // Build student grade rows.
        for (Object enrollment : enrollments) {
        	User student = ((EnrollmentRecord)enrollment).getUser();
        	String studentUid = student.getUserUid();
        	Map studentMap = (Map)gradesMap.get(studentUid);
        	List<Object> row = new ArrayList<Object>();
        	row.add(student.getDisplayId());
        	row.add(student.getSortName());
        	String grade = null;
        	if (studentMap != null) {
        		CourseGradeRecord gradeRecord = (CourseGradeRecord)studentMap.get(courseGrade.getId()); 
    			if (gradeRecord != null) {
    				grade = gradeRecord.getDisplayGrade();
    			}
        	}
        	row.add(grade);
        	spreadsheetData.add(row);
        }
    	
    	return spreadsheetData;
    }

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("course_grade_details_log_type");
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
