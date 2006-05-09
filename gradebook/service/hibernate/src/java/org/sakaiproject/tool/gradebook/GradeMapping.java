/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
	protected Map gradeMap;

	/*
	protected List grades;
	protected List defaultValues;
	*/

	private GradingScale gradingScale;

	/*
		<subclass name="org.sakaiproject.tool.gradebook.PassNotPassMapping" discriminator-value="1" />
		<subclass name="org.sakaiproject.tool.gradebook.LetterGradeMapping" discriminator-value="2" />
		<subclass name="org.sakaiproject.tool.gradebook.LetterGradePlusMinusMapping" discriminator-value="3" />
	*/

	public GradeMapping() {
	}

	public GradeMapping(GradingScale gradingScale) {
		setGradingScale(gradingScale);
		gradeMap = new HashMap(gradingScale.getDefaultBottomPercents());
/*
		Iterator gradesIter = getGradingScale().getGrades().iterator();
		Iterator defaultValuesIter = getGradingScale().getDefaultBottomPercents().iterator();
		while (gradesIter.hasNext()) {
			String grade = (String)gradesIter.next();
			Double value = (Double)defaultValuesIter.next();
			gradeMap.put(grade, value);
		}
*/
	}


	public String getName() {
		return getGradingScale().getName();
	}

	/**
	 * Sets the percentage values for this GradeMapping to their default values.
	 */
	public void setDefaultValues() {
		GradingScale gradingScale = getGradingScale();
		if (gradingScale != null) {
			gradeMap = new HashMap(gradingScale.getDefaultBottomPercents());
		} else {
			// Backward compatibility with pre-grading-scale mappings.
			gradeMap = new HashMap();
			Iterator defaultValuesIter = getDefaultValues().iterator();
			Iterator gradesIter = getGrades().iterator();
			while (gradesIter.hasNext()) {
				String grade = (String)gradesIter.next();
				Double value = (Double)defaultValuesIter.next();
				gradeMap.put(grade, value);
			}
		}
	}

	/**
	 *
	 * @return An (ordered) collection of the available grade values
	 */
	public Collection getGrades() {
		return getGradingScale().getGrades();
	}

    // TODO Move this display-control logic to the UI layer where it belongs.
    // (What's really important is whether the score value is 0 or null.)
/*
    public String getLowestGrade() {
    	Object[] grades = getGrades().toArray();
        return (String)grades[grades.length - 1];
    }
*/
	/**
	 *
	 * @return A List of the default grade values. Only used for backward
	 * compatibility to pre-grading-scale mappings.
	 */
	public List getDefaultValues() {
		throw new UnsupportedOperationException("getDefaultValues called for GradeMapping " + getName() + " in Gradebook " + getGradebook());
    }

	/**
	 * Gets the percentage mapped to a particular grade.
	 */
	public Double getValue(String grade) {
		return (Double) gradeMap.get(grade);
	}

    /**
     * Maps a grade to a percentage value.
     *
     * @param grade The grade being mapped
     * @param percentage The percentage value to map to the grade
     */
    public void putValue(String grade, Double percentage) {
        if(!getGrades().contains(grade)) {
            throw new IllegalArgumentException("The grade " + grade + " is not appropriate for '" + getName() + "' grade mappings.");
        }
        gradeMap.put(grade, percentage);
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
            if(log.isInfoEnabled()) log.info("Can not get a mapped grade for a null value");
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
	public Map getGradeMap() {
		return gradeMap;
	}

	/**
	 * @param gradeMap
	 *            The gradeMap to set.
	 */
	public void setGradeMap(Map gradeMap) {
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

	public GradingScale getGradingScale() {
		return gradingScale;
	}

	public void setGradingScale(GradingScale gradingScale) {
		this.gradingScale = gradingScale;
	}
}
