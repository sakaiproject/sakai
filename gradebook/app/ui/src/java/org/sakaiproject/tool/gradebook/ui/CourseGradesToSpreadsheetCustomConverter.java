/**********************************************************************************
 *
 * $Id: CourseGradesToSpreadsheetConverterDefault.java 105079 2012-02-24 23:08:11Z savithap@umich.edu $
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetConverter;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 *
 */
@Slf4j
public class CourseGradesToSpreadsheetCustomConverter implements CourseGradesToSpreadsheetConverter {

	/* 
	 * org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetCustomConverter#getSpreadsheetData(java.util.List, org.sakaiproject.tool.gradebook.CourseGrade, java.util.Map)
	 * Allows to define export format by following sakai property:
	 * gradebook.institutional.export.enabled={true|false}
	 * gradebook.institutional.export.headers={true|false}
	 * gradebook.institutional.export.fields={CSV list of columns, presented in the order desired - userid, usereid, useremail, username, sortname, coursegrades, siteid, sitetitle}
	 * @returns csv with custom fields 
	 */
	@SuppressWarnings("deprecation")
	public List<List<Object>> getSpreadsheetData(
			List<EnrollmentRecord> enrollments, CourseGrade courseGrade,
			Map<String, CourseGradeRecord> gradesMap, List<String> fields) {
		List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();
		
		// Build column headers.
		Boolean hasHeader = ServerConfigurationService.getBoolean("gradebook.institutional.export.headers",false);
		if (hasHeader){
			List<Object> headerRow = new ArrayList<Object>();
			for (String headerField : fields){
				if(GradeFields.contains(headerField)){
					headerRow.add(FacesUtil.getLocalizedString(headerField));
				}
			}
			spreadsheetData.add(headerRow);
		}

		// Build student grade rows.
		for (Object enrollment : enrollments) {
			User student = ((EnrollmentRecord)enrollment).getUser();
			String studentUid = student.getUserUid();
			Map studentMap = (Map)gradesMap.get(studentUid);
			List<Object> row = new ArrayList<Object>();
			String grade = null;
			if (studentMap != null) {
				CourseGradeRecord gradeRecord = (CourseGradeRecord)studentMap.get(courseGrade.getId()); 
				if (gradeRecord != null) {
					grade = gradeRecord.getDisplayGrade();
				}
			}

			for (String field : fields){

				switch (GradeFields.fromString(field)) {
				case userid:
					row.add(student.getUserUid());
					break;

				case usereid:
					row.add(student.getDisplayId());
					break;

				case sortname:
					row.add(student.getSortName());
					break;

				case coursegrade:
					row.add(grade);
					break;

				case useremail:
					try {
						row.add(UserDirectoryService.getUser(student.getUserUid()).getEmail());
					} catch (UserNotDefinedException e) {
						row.add("undefined user");
						log.error(e.getMessage(), e);
					}
					break;

				case username:
					row.add(student.getDisplayName());
					break;

				case siteid:
					row.add(courseGrade.getGradebook().getName());
					break;

				case sitetitle:
					row.add(((EnrollmentRecord)enrollment).getLearningContext().getTitle());
					break;
				}
			}
			spreadsheetData.add(row);
		}
		return spreadsheetData;
	}

	public enum GradeFields
	{
		userid, usereid, useremail, username, sortname, coursegrade, siteid, sitetitle, novalue;

		public static boolean contains(String field) {
			try {
				GradeFields.valueOf(field);
				return true;
			} catch (Exception e) {
				log.info("Gradebook: Could not find the export field : " + field);
				return false;
			}
		}

		public static GradeFields fromString(String field) { 
			try {
				return GradeFields.valueOf(field);
			} 
			catch (Exception ex){
				log.info("Gradebook: Could not find the export field : " + field);
				return GradeFields.valueOf("novalue");
			} 
		}
	}
}
