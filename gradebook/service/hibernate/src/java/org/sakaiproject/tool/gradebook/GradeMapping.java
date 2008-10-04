/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation, the MIT Corporation
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

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A GradeMapping provides a means to convert between an arbitrary set of grades
 * (letter grades, pass / not pass, 4,0 scale) and numeric percentages.
 *
 */
public class GradeMapping implements Serializable, Comparable {
	protected Log log = LogFactory.getLog(GradeMapping.class);
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
		GradingScale gradingScale = getGradingScale();
		if (gradingScale != null) {
			return gradingScale.getDefaultBottomPercents();
		} else {
			Map<String, Double> defaultBottomPercents = new HashMap<String, Double>();
			Iterator defaultValuesIter = getDefaultValues().iterator();
			Iterator gradesIter = getGrades().iterator();
			while (gradesIter.hasNext()) {
				String grade = (String)gradesIter.next();
				Double value = (Double)defaultValuesIter.next();
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
	 */
	public List<Double> getDefaultValues() {
		throw new UnsupportedOperationException("getDefaultValues called for GradeMapping " + getName() + " in Gradebook " + getGradebook());
    }

	/**
	 * Gets the percentage mapped to a particular grade.
	 */
	public Double getValue(String grade) {
		return (Double) gradeMap.get(grade);
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
        for (Iterator iter = getGrades().iterator(); iter.hasNext();) {
			String grade = (String) iter.next();
			Double mapVal = (Double) gradeMap.get(grade);
			// If the value in the map is less than the value passed, then the
			// map value is the letter grade for this value
			if (mapVal != null && mapVal.compareTo(value) <= 0) {
				return grade;
			}
		}
		// As long as 'F' is zero, this should never happen.
		return null;
	}

	////// Bean accessors and mutators //////

	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return Returns the version.
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version
	 *            The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return Returns the gradeMap.
	 */
	public Map<String, Double> getGradeMap() {
		return gradeMap;
	}

	/**
	 * @param gradeMap
	 *            The gradeMap to set.
	 */
	public void setGradeMap(Map<String, Double> gradeMap) {
		this.gradeMap = gradeMap;
	}

	/**
	 * @return Returns the gradebook.
	 */
	public Gradebook getGradebook() {
		return gradebook;
	}
	/**
	 * @param gradebook
	 *            The gradebook to set.
	 */
	public void setGradebook(Gradebook gradebook) {
		this.gradebook = gradebook;
	}

 	public int compareTo(Object o) {
        return getName().compareTo(((GradeMapping)o).getName());
    }

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
    	for (Iterator iter = getGrades().iterator(); iter.hasNext(); ) {
    		String grade = (String)iter.next();
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
