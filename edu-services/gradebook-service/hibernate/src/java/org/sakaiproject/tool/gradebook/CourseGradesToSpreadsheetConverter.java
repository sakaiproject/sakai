/**
 * Copyright (c) 2003-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook;

import java.util.List;
import java.util.Map;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

/**
 * Interface to let institutions intercept course grade spreadsheet download requests.
 */
public interface CourseGradesToSpreadsheetConverter {
	/**
	 * This method is called by the Course Grade UI after gathering filtered enrollment
	 * records and course grade records, and before actually formatting and downloading
	 * the XLS or CSV file. Customized implementations could, for example, call out to
	 * the course management service and change or add columns to the generic data table
	 * which is sent on to be formatted.
	 * 
	 * @param enrollments 
	 * @param courseGrade
	 * @param gradesMap a map of student UIDs to grade records
	 * @return a spreadsheet-like list of rows, each of which is a list of column values;
	 * the first row should contain header strings
	 */
	public List<List<Object>> getSpreadsheetData(List<EnrollmentRecord> enrollments, CourseGrade courseGrade, Map<String, CourseGradeRecord> gradesMap, List<String> fields);
}
