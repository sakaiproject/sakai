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
	@Override
	public List<List<Object>> getSpreadsheetData(final List<EnrollmentRecord> enrollments, final CourseGrade courseGrade, final Map<String, CourseGradeRecord> gradesMap, final List<String> fields) {

		final List<List<Object>> spreadsheetData = new ArrayList<>();

		// Build column headers.
		final List<Object> headerRow = new ArrayList<>();
		for (final String headerField : fields){
			headerRow.add(FacesUtil.getLocalizedString(headerField));
		}
		spreadsheetData.add(headerRow);

		// Build student grade rows.
		for (final Object enrollment : enrollments) {
			final User student = ((EnrollmentRecord)enrollment).getUser();
			final String studentUid = student.getUserUid();
			final Map studentMap = (Map)gradesMap.get(studentUid);
			final List<Object> row = new ArrayList<>();
			if (studentMap != null) {
				final CourseGradeRecord gradeRecord = (CourseGradeRecord)studentMap.get(courseGrade.getId());
				if (gradeRecord != null) {

					for (final String field : fields){

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
    public String getCalculatedLetterGrade(final CourseGrade courseGrade, final CourseGradeRecord gradeRecord) {

		final GradeMapping gradeMapping = courseGrade.getGradebook().getSelectedGradeMapping();
		final Double grade = gradeRecord.getAutoCalculatedGrade();
		String letterGrade = null;
		if (grade != null) {
			letterGrade = gradeMapping.getMappedGrade(gradeRecord.getNonNullAutoCalculatedGrade());
		}
		return letterGrade;
    }

	public enum StandardFields
	{
		usereid, sortname, finalscore, calculatedgrade, gradeoverride, coursegrade, lastmodifieddate;

	}
}
