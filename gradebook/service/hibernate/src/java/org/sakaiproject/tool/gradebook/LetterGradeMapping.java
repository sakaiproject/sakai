/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook;

import java.util.*;

/**
 * A LetterGradeMapping defines the set of grades available to a gradebook as
 * "A", "B", "C", "D", and "F", each of which can be mapped to a minimum
 * percentage value.
 *
 * @deprecated
 */
public class LetterGradeMapping extends GradeMapping {
	private List grades;
	private List defaultValues;
	public Collection getGrades() {
		return grades;
	}
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
        defaultValues.add(new Double(90));
        defaultValues.add(new Double(80));
        defaultValues.add(new Double(70));
        defaultValues.add(new Double(60));
        defaultValues.add(new Double(00));

        setDefaultValues();
    }

	public String getName() {
		return "Letter Grades";
	}
}


