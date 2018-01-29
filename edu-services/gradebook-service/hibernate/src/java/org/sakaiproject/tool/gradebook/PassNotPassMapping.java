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
 * A PassNotPassMapping defines the set of grades available to a
 * gradebook as "P", "NP", each of which can be mapped to a minimum percentage
 * value.
 *
 */
public class PassNotPassMapping extends GradeMapping {
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

    public PassNotPassMapping() {
        setGradeMap(new LinkedHashMap());

        grades = new ArrayList();
        grades.add("P");
        grades.add("NP");

        defaultValues = new ArrayList();
        defaultValues.add(Double.valueOf(75));
        defaultValues.add(Double.valueOf(0));
    }

	/**
	 * @see org.sakaiproject.tool.gradebook.GradeMapping#getName()
	 */
	@Override
	public String getName() {
        return "Pass / Not Pass";
	}

}



