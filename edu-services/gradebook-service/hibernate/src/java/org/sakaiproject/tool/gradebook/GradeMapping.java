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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A GradeMapping provides a means to convert between an arbitrary set of grades
 * (letter grades, pass / not pass, 4,0 scale) and numeric percentages.
 *
 */
public class GradeMapping implements Serializable, Comparable<Object> {
	
	private static final long serialVersionUID = 1L;
	
	protected Long id;
	protected int version;

	protected Gradebook gradebook;
	protected Map<String, Double> gradeMap;
	private GradingScale gradingScale;

	public GradeMapping() {
	}

	public GradeMapping(GradingScale gradingScale) {
		setGradingScale(gradingScale);
		gradeMap = new HashMap<String, Double>(gradingScale.getDefaultBottomPercents());
	}

	public String getName() {
		return getGradingScale().getName();
	}

	/**
	 * Sets the percentage values for this GradeMapping to their default values.
	 */
	public void setDefaultValues() {
		gradeMap = new HashMap<String, Double>(getDefaultBottomPercents());
	}
	
	/**
	 * Backwards-compatible wrapper to get to grading scale. 
	 */
	public Map<String, Double> getDefaultBottomPercents() {
		GradingScale scale = getGradingScale();
		if (scale != null) {
			return scale.getDefaultBottomPercents();
		} else {
			Map<String, Double> defaultBottomPercents = new HashMap<String, Double>();
			Iterator<String> gradesIter = getGrades().iterator();
			Iterator<Double> defaultValuesIter = getDefaultValues().iterator();
			while (gradesIter.hasNext()) {
				String grade = gradesIter.next();
				Double value = defaultValuesIter.next();
				defaultBottomPercents.put(grade, value);
			}
			return defaultBottomPercents;
		}
	}

	/**
	 *
	 * @return An (ordered) collection of the available grade values
	 */
	public Collection<String> getGrades() {
		return getGradingScale().getGrades();
	}

	/**
	 *
	 * @return A List of the default grade values. Only used for backward
	 * compatibility to pre-grading-scale mappings.
	 * 
	 * @deprecated
	 */
	public List<Double> getDefaultValues() {
		throw new UnsupportedOperationException("getDefaultValues called for GradeMapping " + getName() + " in Gradebook " + getGradebook());
    }

	/**
	 * Gets the percentage mapped to a particular grade.
	 */
	public Double getValue(String grade) {
		return gradeMap.get(grade);
	}

	/**
	 * This algorithm is slow, since it checks each grade option, starting from
	 * the "top" (in this case an 'A'). We can make it faster by starting in the
	 * middle (as in a bubble sort), but since there are so few grade options, I
	 * think I'll leave it for now.
	 *
	 * @see org.sakaiproject.tool.gradebook.GradeMapping#getGrade(Double)
	 */
	public String getGrade(Double value) {
		if(value == null) {
            return null;
        }
		
        for (String grade: getGrades()) {
			Double mapVal = gradeMap.get(grade);
			// If the value in the map is less than the value passed, then the
			// map value is the letter grade for this value
			if (mapVal != null && mapVal.compareTo(value) <= 0) {
				return grade;
			}
		}
		// As long as 'F' is zero, this should never happen.
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

	public Map<String, Double> getGradeMap() {
		return gradeMap;
	}

	public void setGradeMap(Map<String, Double> gradeMap) {
		this.gradeMap = gradeMap;
	}

	public Gradebook getGradebook() {
		return gradebook;
	}
	
	public void setGradebook(Gradebook gradebook) {
		this.gradebook = gradebook;
	}

 	@Override
	public int compareTo(Object o) {
        return getName().compareTo(((GradeMapping)o).getName());
    }

    @Override
	public String toString() {
        return new ToStringBuilder(this).
            append(getName()).
            append(id).toString();
    }

    /**
     * Enable any-case input of grades (typically lowercase input
     * for uppercase grades). Look for a case-insensitive match
     * to the input text and if it's found, return the official
     * version.
     *
     * @return The normalized version of the grade, or null if not found.
     */
    public String standardizeInputGrade(String inputGrade) {
    	String standardizedGrade = null;
    	for (String grade: getGrades()) {
    		if (grade.equalsIgnoreCase(inputGrade)) {
    			standardizedGrade = grade;
    			break;
    		}
    	}
    	return standardizedGrade;
    }

	/**
	 * @return the GradingScale used to define this mapping, or null if
	 * this is an old Gradebook which uses hard-coded scales
	 */
    public GradingScale getGradingScale() {
		return gradingScale;
	}

	public void setGradingScale(GradingScale gradingScale) {
		this.gradingScale = gradingScale;
	}
}
