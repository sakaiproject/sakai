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
import java.util.TreeMap;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.DoubleComparator;

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

	public GradeMapping(final GradingScale gradingScale) {
		setGradingScale(gradingScale);
		this.gradeMap = new HashMap<>(gradingScale.getDefaultBottomPercents());
	}

	public String getName() {
		return getGradingScale().getName();
	}

	/**
	 * Sets the percentage values for this GradeMapping to their default values.
	 */
	public void setDefaultValues() {
		this.gradeMap = new HashMap<>(getDefaultBottomPercents());
	}

	/**
	 * Backwards-compatible wrapper to get to grading scale.
	 */
	public Map<String, Double> getDefaultBottomPercents() {
		final GradingScale scale = getGradingScale();
		if (scale != null) {
			return scale.getDefaultBottomPercents();
		} else {
			final Map<String, Double> defaultBottomPercents = new HashMap<String, Double>();
			final Iterator<String> gradesIter = getGrades().iterator();
			final Iterator<Double> defaultValuesIter = getDefaultValues().iterator();
			while (gradesIter.hasNext()) {
				final String grade = gradesIter.next();
				final Double value = defaultValuesIter.next();
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
	@Deprecated
	public List<Double> getDefaultValues() {
		throw new UnsupportedOperationException("getDefaultValues called for GradeMapping " + getName() + " in Gradebook " + getGradebook());
    }

	/**
	 * Gets the percentage mapped to a particular grade.
	 */
	public Double getValue(final String grade) {
		return this.gradeMap.get(grade);
	}

	/**
	 * Get the mapped grade based on the persistent grade mappings
	 *
	 */
	public String getMappedGrade(final Double value) {
		return getMappedGrade(getGradeMap(), value);
	}

	/**
	 * Get the mapped grade based on the passed in grade mappings.
	 *
	 * NOTE: The gradeMap MUST be sorted!
	 */
	public static String getMappedGrade(final Map<String, Double> gradeMap, final Double value) {
		if(value == null) {
            return null;
        }

		for (final Map.Entry<String, Double> entry : gradeMap.entrySet()) {
			final String grade = entry.getKey();
			final Double mapVal = entry.getValue();

			// If the value in the map is less than the value passed, then the
			// map value is the letter grade for this value
			if (mapVal != null && mapVal.compareTo(value) <= 0) {
				return grade;
			}
		}
		// As long as 'F' is zero, this should never happen.
		return null;
	}

	/**
	 * Handles the sorting of the grade mapping.
	 *
	 * @param gradeMap
	 * @return
	 */
	public static Map<String, Double> sortGradeMapping(final Map<String, Double> gradeMap) {

		// we only ever order by bottom percents now
		final DoubleComparator doubleComparator = new DoubleComparator(gradeMap);
		final Map<String, Double> rval = new TreeMap<>(doubleComparator);
		rval.putAll(gradeMap);

		return rval;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}


	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public Map<String, Double> getGradeMap() {
		return this.gradeMap;
	}

	public void setGradeMap(final Map<String, Double> gradeMap) {
		this.gradeMap = gradeMap;
	}

	public Gradebook getGradebook() {
		return this.gradebook;
	}

	public void setGradebook(final Gradebook gradebook) {
		this.gradebook = gradebook;
	}

 	@Override
	public int compareTo(final Object o) {
		final GradeMapping other = (GradeMapping) o;
		return new CompareToBuilder().append(getName(), other.getName()).toComparison();
    }

	@Override
	public boolean equals(final Object o) {
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final GradeMapping other = (GradeMapping) o;
		return new EqualsBuilder().append(getName(), other.getName()).isEquals();
	}

	@Override
	public int hashCode() {
		if (getGradingScale() == null || getName() == null) {
			return 0;
		}
		return new HashCodeBuilder().append(getName()).toHashCode();

	}

    @Override
	public String toString() {
        return new ToStringBuilder(this).
            append(getName()).
            append(this.id).toString();
    }

    /**
     * Enable any-case input of grades (typically lowercase input
     * for uppercase grades). Look for a case-insensitive match
     * to the input text and if it's found, return the official
     * version.
     *
     * @return The normalized version of the grade, or null if not found.
     */
    public String standardizeInputGrade(final String inputGrade) {
    	String standardizedGrade = null;
    	for (final String grade: getGrades()) {
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
		return this.gradingScale;
	}

	public void setGradingScale(final GradingScale gradingScale) {
		this.gradingScale = gradingScale;
	}
}
