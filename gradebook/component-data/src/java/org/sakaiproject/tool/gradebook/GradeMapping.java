/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/component-data/src/java/org/sakaiproject/tool/gradebook/GradeMapping.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A GradeMapping provides a means to convert between an arbitrary set of grades
 * (letter grades, pass / not pass, 4,0 scale) and numeric percentages.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public abstract class GradeMapping implements Comparable {
	protected Log log = LogFactory.getLog(GradeMapping.class);
	protected Long id;
	protected int version;

	protected Gradebook gradebook;
	protected Map gradeMap;

	protected List grades;
	protected List defaultValues;

	public abstract String getName();

	/**
	 * Sets the percentage values for this GradeMapping to their default values.
	 */
	public void setDefaultValues() {
		gradeMap = new HashMap();
		List defaultValues = getDefaultValues();
		List grades = getGrades();
		for (int i = 0; i < grades.size(); i++) {
			String grade = (String) grades.get(i);
			gradeMap.put(grade, defaultValues.get(i));
		}
	}

	/**
	 * Determines whether this grade mapping is the gradebook's default
	 *
	 * @return Whether this is the default mapping for the gradebook
	 */
	public boolean isDefault() {
		return this.getClass().equals(getGradebook().getDefaultGradeMapping());
	}

	/**
	 *
	 * @return A List of the available grade values
	 */
	public List getGrades() {
		return grades;
	}

	/**
	 *
	 * @return A List of the default grade values
	 */
	public List getDefaultValues() {
        return defaultValues;
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
        if(!grades.contains(grade)) {
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
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/component-data/src/java/org/sakaiproject/tool/gradebook/GradeMapping.java,v 1.4 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
