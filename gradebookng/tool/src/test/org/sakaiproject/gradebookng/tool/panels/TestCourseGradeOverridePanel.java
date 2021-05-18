/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeOverridePanel;

public class TestCourseGradeOverridePanel {

	private Map<String, Double> schema;

	@Before
	public void init() {
		schema = new HashMap<String, Double>();
		schema.put("F", 0.0);
		schema.put("D", 60.0);
		schema.put("C", 70.0);
		schema.put("B", 80.0);
		schema.put("A", 90.0);
	}

	@Test
	public void testCalculateGradeFromNumber() {

		String resultScale;
		String gradeNumber = "85";
		String gradeScale = "B";

		resultScale = CourseGradeOverridePanel.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeScale);

		gradeNumber = "30";
		gradeScale = "F";

		resultScale = CourseGradeOverridePanel.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeScale);

		gradeNumber = "60";
		gradeScale = "D";

		resultScale = CourseGradeOverridePanel.getGradeFromNumber(gradeNumber, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeScale);
	}

	@Test
	public void testCalculateNumberFromGrade() {

		String resultScale;
		String gradeNumber = "80";
		String gradeScale = "B";

		resultScale = CourseGradeOverridePanel.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeNumber);

		gradeNumber = "0";
		gradeScale = "F";

		resultScale = CourseGradeOverridePanel.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeNumber);

		gradeNumber = "60";
		gradeScale = "D";

		resultScale = CourseGradeOverridePanel.getNumberFromGrade(gradeScale, schema, Locale.getDefault());

		Assert.assertEquals(resultScale, gradeNumber);
	}
}