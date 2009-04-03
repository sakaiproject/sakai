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

package org.sakaiproject.tool.gradebook.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetConverter;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 *
 */
public class CourseGradesToSpreadsheetConverterDefault implements CourseGradesToSpreadsheetConverter {

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.gradebook.CourseGradesConverter#getSpreadsheetData(java.util.List, org.sakaiproject.tool.gradebook.CourseGrade, java.util.Map)
	 */
	public List<List<Object>> getSpreadsheetData(
			List<EnrollmentRecord> enrollments, CourseGrade courseGrade,
			Map<String, CourseGradeRecord> gradesMap) {
    	List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

    	// Build column headers.
        List<Object> headerRow = new ArrayList<Object>();
        headerRow.add(FacesUtil.getLocalizedString("export_student_id"));
        headerRow.add(FacesUtil.getLocalizedString("export_student_name"));
        headerRow.add(FacesUtil.getLocalizedString("course_grade_details_course_grade_column_name"));
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

}
