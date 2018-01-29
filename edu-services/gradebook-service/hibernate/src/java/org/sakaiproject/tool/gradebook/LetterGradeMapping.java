/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A LetterGradeMapping defines the set of grades available to a gradebook as
 * "A", "B", "C", "D", and "F", each of which can be mapped to a minimum
 * percentage value.
 *
 */
public class LetterGradeMapping extends GradeMapping {
	
	private static final long serialVersionUID = 1L;
	
	private List grades;
	private List defaultValues;
	@Override
	public Collection getGrades() {
		return grades;
	}
	@Override
	public List getDefaultValues() {
        return defaultValues;
    }

    public LetterGradeMapping() {
        setGradeMap(new LinkedHashMap());

        grades = new ArrayList();
        grades.add("A");
        grades.add("B");
        grades.add("C");
        grades.add("D");
        grades.add("F");

        defaultValues = new ArrayList();
        defaultValues.add(Double.valueOf(90));
        defaultValues.add(Double.valueOf(80));
        defaultValues.add(Double.valueOf(70));
        defaultValues.add(Double.valueOf(60));
        defaultValues.add(Double.valueOf(00));

        setDefaultValues();
    }

	@Override
	public String getName() {
		return "Letter Grades";
	}
}


