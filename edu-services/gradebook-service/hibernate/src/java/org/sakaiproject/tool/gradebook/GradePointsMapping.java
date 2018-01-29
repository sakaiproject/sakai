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
 * This is a modified LetterGradePlusMinusMapping.
 * Details at: from https://github.com/sakaiproject/sakai/issues/3432
 */
public class GradePointsMapping extends GradeMapping {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> grades;
	private List<Double> defaultValues;
	
	@Override
	public Collection<String> getGrades() {
		return grades;
	}
	
	@Override
	public List<Double> getDefaultValues() {
        return defaultValues;
    }

    public GradePointsMapping() {
        super.setGradeMap(new LinkedHashMap<String, Double>());

        // If these values change, they also need to be updated in GradebookNG SettingsGradingSchemaPanel 
        grades = new ArrayList<String>();
        grades.add("A (4.0)");
        grades.add("A- (3.67)");
        grades.add("B+ (3.33)");
        grades.add("B (3.0)");
        grades.add("B- (2.67)");
        grades.add("C+ (2.33)");
        grades.add("C (2.0)");
        grades.add("C- (1.67)");
        grades.add("D (1.0)");
        grades.add("F (0)");

        defaultValues = new ArrayList<Double>();
        defaultValues.add(Double.valueOf(100));
        defaultValues.add(Double.valueOf(90));
        defaultValues.add(Double.valueOf(87));
        defaultValues.add(Double.valueOf(83));
        defaultValues.add(Double.valueOf(80));
        defaultValues.add(Double.valueOf(77));
        defaultValues.add(Double.valueOf(73));
        defaultValues.add(Double.valueOf(70));
        defaultValues.add(Double.valueOf(67));
        defaultValues.add(Double.valueOf(00));
    }

    /**
     * @see org.sakaiproject.tool.gradebook.GradeMapping#getName()
     */
    @Override
	public String getName() {
        return "Grade Points";
    }

}
