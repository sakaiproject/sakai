/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.gradebooksample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.LearningContext;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetConverter;

/**
 * Sample customization of course grade spreadsheet downloads. This changes
 * the default behavior in the following ways:
 * 
 * <ul>
 * <li> The student name column is dropped.
 * <li> Another column is added to hold "Comments".
 * <li> The Course Management service is used to look up each student's
 * grading scheme.
 * <li> If the grading scheme is "Pass/Fail" or "Satisfactory/Unsatisfactory",
 * then the Gradebook's letter grade is converted to "P", "NP", "S", or "U"
 * based on a string translation table. The converted value is put into the 
 * "Grade" column and the original letter grade is put into the "Comments" column.
 * </ul>
 * 
 * Once this has been deployed as a Sakai component, it can be plugged into
 * the Gradebook application by adding the following line to "sakai.properties":
 * <p>
 * gradebook.coursegrades.converter=org.sakaiproject.gradebooksample.CourseGradesToSpreadsheetConverterSample
 */
public class CourseGradesToSpreadsheetConverterSample implements CourseGradesToSpreadsheetConverter {
	private static final Log log = LogFactory.getLog(CourseGradesToSpreadsheetConverterSample.class);
	
	private CourseManagementService courseManagementService;
	private String letterGradingSchemeName;
	private String passGradingSchemeName;
	private String satisfactoryGradingSchemeName;
	private Map<String, String> letterToPassMap;
	private Map<String, String> letterToSatisfactoryMap;

	/**
	 * @see org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetConverter#getSpreadsheetData(java.util.List,
	 *      org.sakaiproject.tool.gradebook.CourseGrade, java.util.Map)
	 */
	public List<List<Object>> getSpreadsheetData(List<EnrollmentRecord> enrollments, CourseGrade courseGrade, Map<String, CourseGradeRecord> gradesMap) {
		List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

    	// Build column headers.
        List<Object> headerRow = new ArrayList<Object>();
        headerRow.add("STUDENT ID NUMBER");
        headerRow.add("GRADE");
        headerRow.add("COMMENTS");
        spreadsheetData.add(headerRow);

        // Build student grade rows.
        for (EnrollmentRecord enrollment : enrollments) {
        	User student = enrollment.getUser();
        	String studentUid = student.getUserUid();
        	Map studentMap = (Map)gradesMap.get(studentUid);
        	List<Object> row = new ArrayList<Object>();
        	row.add(student.getDisplayId());
        	String grade = null;
        	if (studentMap != null) {
        		CourseGradeRecord gradeRecord = (CourseGradeRecord)studentMap.get(courseGrade.getId());
    			if (gradeRecord != null) {
    				grade = gradeRecord.getDisplayGrade();
    	        	if (log.isDebugEnabled()) log.debug("student UID=" + studentUid + ", displayID=" + student.getDisplayId() + ", displayGrade=" + grade);
    				addGradeColumns(row, enrollment, grade);
    			}
        	}
        	spreadsheetData.add(row);
        }

    	return spreadsheetData;
	}
	
	private void addGradeColumns(List<Object> row, EnrollmentRecord sectionAwarenessEnrollment, String originalGrade) {
		String gradeColumn = originalGrade;
		String commentColumn = null; 
		LearningContext lc = sectionAwarenessEnrollment.getLearningContext();
		if (log.isDebugEnabled()) log.debug("lc=" + lc);
		if (lc instanceof CourseSection) {
			CourseSection courseSection = (CourseSection)lc;
			String courseManagementEid = courseSection.getEid();
			if (log.isDebugEnabled()) log.debug("courseManagementEid=" + courseManagementEid);
			if (courseManagementEid != null) {
				// Translate from Section Awareness view to Course Management data.
				Section cmSection = courseManagementService.getSection(courseManagementEid);
				if (cmSection != null) {
					EnrollmentSet enrollmentSet = cmSection.getEnrollmentSet();
					if (log.isDebugEnabled()) log.debug("enrollmentSet=" + enrollmentSet);
					if (enrollmentSet != null) {
						Enrollment cmEnrollment = courseManagementService.findEnrollment(sectionAwarenessEnrollment.getUser().getDisplayId(), enrollmentSet.getEid());
						if (cmEnrollment != null) {
							String gradingScheme = cmEnrollment.getGradingScheme();
							if (log.isDebugEnabled()) log.debug("gradingScheme=" + gradingScheme);
							if ((gradingScheme != null) && (!gradingScheme.equals(letterGradingSchemeName))) {
								if (gradingScheme.equals(passGradingSchemeName)) {
									gradeColumn = letterToPassMap.get(originalGrade);
									commentColumn = originalGrade;
								} else if (gradingScheme.equals(satisfactoryGradingSchemeName)) {
									gradeColumn = letterToSatisfactoryMap.get(originalGrade);
									commentColumn = originalGrade;							
								}
							}
						}						
					}
				}
			}
		}
		if (log.isDebugEnabled()) log.debug("gradeColumn=" + gradeColumn + ", commentColumn=" + commentColumn);
		row.add(gradeColumn);
		row.add(commentColumn);
	}

	public CourseManagementService getCourseManagementService() {
		return courseManagementService;
	}
	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	public String getLetterGradingSchemeName() {
		return letterGradingSchemeName;
	}

	public void setLetterGradingSchemeName(String letterGradingSchemeName) {
		this.letterGradingSchemeName = letterGradingSchemeName;
	}

	public Map<String, String> getLetterToPassMap() {
		return letterToPassMap;
	}

	public void setLetterToPassMap(Map<String, String> letterToPassMap) {
		this.letterToPassMap = letterToPassMap;
	}

	public Map<String, String> getLetterToSatisfactoryMap() {
		return letterToSatisfactoryMap;
	}

	public void setLetterToSatisfactoryMap(
			Map<String, String> letterToSatisfactoryMap) {
		this.letterToSatisfactoryMap = letterToSatisfactoryMap;
	}

	public String getPassGradingSchemeName() {
		return passGradingSchemeName;
	}

	public void setPassGradingSchemeName(String passGradingSchemeName) {
		this.passGradingSchemeName = passGradingSchemeName;
	}

	public String getSatisfactoryGradingSchemeName() {
		return satisfactoryGradingSchemeName;
	}

	public void setSatisfactoryGradingSchemeName(
			String satisfactoryGradingSchemeName) {
		this.satisfactoryGradingSchemeName = satisfactoryGradingSchemeName;
	}

}
