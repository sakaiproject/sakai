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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 *
 */
public class CourseGradesToSpreadsheetConverterDefault implements CourseGradesToSpreadsheetConverter {

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.gradebook.CourseGradesConverter#getSpreadsheetData(java.util.List, org.sakaiproject.tool.gradebook.CourseGrade, java.util.Map)
	 */
	public List<List<Object>> getSpreadsheetData(List<EnrollmentRecord> enrollments, CourseGrade courseGrade, Map<String, CourseGradeRecord> gradesMap, List<String> fields) {

		List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

		// Build column headers.
		List<Object> headerRow = new ArrayList<Object>();
		for (String headerField : fields){
			headerRow.add(FacesUtil.getLocalizedString(headerField));
		}
		spreadsheetData.add(headerRow);

		// Build student grade rows.
		for (Object enrollment : enrollments) {
			User student = ((EnrollmentRecord)enrollment).getUser();
			String studentUid = student.getUserUid();
			Map studentMap = (Map)gradesMap.get(studentUid);
			List<Object> row = new ArrayList<Object>();
			if (studentMap != null) {
				CourseGradeRecord gradeRecord = (CourseGradeRecord)studentMap.get(courseGrade.getId()); 
				if (gradeRecord != null) {

					for (String field : fields){

						switch (StandardFields.valueOf(field)) {
						case usereid:
							row.add(student.getDisplayId());
							break;

						case sortname:
							row.add(student.getSortName());
							break;

						case finalscore:
							row.add(gradeRecord.getPointsEarned());
							break;

						case calculatedgrade:
							row.add(getCalculatedLetterGrade(courseGrade, gradeRecord));
							break;

						case gradeoverride:
							row.add(gradeRecord.getEnteredGrade());
							break;

						case coursegrade:
							row.add(gradeRecord.getDisplayGrade());
							break;

						case lastmodifieddate:
							if(gradeRecord.getDateRecorded() !=null) {
								row.add("\""+ gradeRecord.getDateRecorded().toString() +"\"");
							}
							else {
								row.add("");
							}
							break;
						}
					}
				}
			}
			spreadsheetData.add(row);
		}
		return spreadsheetData;
	}
	
	/**
	 * 
	 * @return letter grade representation of grade or null if no course grade yet
	 */
    public String getCalculatedLetterGrade(CourseGrade courseGrade, CourseGradeRecord gradeRecord) {
    	
    	GradeMapping gradeMapping = courseGrade.getGradebook().getSelectedGradeMapping();
    	Double grade = gradeRecord.getAutoCalculatedGrade();
    	String letterGrade = null;
    	if (grade != null)
    		letterGrade = gradeMapping.getGrade(gradeRecord.getNonNullAutoCalculatedGrade());
    	return letterGrade;
    }

	public enum StandardFields
	{
		usereid, sortname, finalscore, calculatedgrade, gradeoverride, coursegrade, lastmodifieddate;

	}
}
