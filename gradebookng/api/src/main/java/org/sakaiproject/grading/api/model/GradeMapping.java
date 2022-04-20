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

package org.sakaiproject.grading.api.model;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;

import org.sakaiproject.grading.api.DoubleComparator;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Check this against SAK-46484. I cut this code before that patch.

/**
 * A GradeMapping provides a means to convert between an arbitrary set of grades
 * (letter grades, pass / not pass, 4,0 scale) and numeric percentages.
 *
 */
@Entity
@Table(name = "GB_GRADE_MAP_T", indexes = @Index(name = "GB_GRADE_MAP_GB_IDX", columnList = "GRADEBOOK_ID"))
@DiscriminatorColumn(name = "OBJECT_TYPE_ID")
@DiscriminatorValue("0")
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter @Setter
public class GradeMapping implements PersistableEntity<Long>, Serializable, Comparable<Object> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @ToString.Include
    protected Long id;

    @Column(name = "VERSION")
    protected Integer version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRADEBOOK_ID", nullable = false)
    protected Gradebook gradebook;

    @ElementCollection
    @CollectionTable(name = "GB_GRADE_TO_PERCENT_MAPPING_T", joinColumns = @JoinColumn(name = "GRADE_MAP_ID"))
    @MapKeyColumn(name = "LETTER_GRADE")
    @Column(name = "PERCENT")
    protected Map<String, Double> gradeMap;

    @ManyToOne
    @JoinColumn(name = "GB_GRADING_SCALE_T")
    private GradingScale gradingScale;

    public GradeMapping() { }

    public GradeMapping(GradingScale gradingScale) {

        this.gradingScale = gradingScale;
        this.gradeMap = new HashMap<>(gradingScale.getDefaultBottomPercents());
    }

    @ToString.Include
    @EqualsAndHashCode.Include
    public String getName() {
        return (gradingScale != null) ? gradingScale.getName() : null;
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
        return (getGradingScale() != null) ? getGradingScale().getGrades() : Collections.emptyList();
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
        throw new UnsupportedOperationException("getDefaultValues called for GradeMapping " + getName());
    }

    /**
     * Gets the percentage mapped to a particular grade.
     */
    public Double getValue(String grade) {
        return this.gradeMap.get(grade);
    }

    /**
     * Get the mapped grade based on the persistent grade mappings
     *
     */
    public String getMappedGrade(Double value) {
        return getMappedGrade(getGradeMap(), value);
    }

    /**
     * Get the mapped grade based on the passed in grade mappings.
     *
     * NOTE: The gradeMap MUST be sorted!
     */
    public static String getMappedGrade(Map<String, Double> gradeMap, Double value) {

        if (value == null) {
            return null;
        }

        for (Map.Entry<String, Double> entry : sortGradeMapping(gradeMap).entrySet()) {
            String grade = entry.getKey();
            Double mapVal = entry.getValue();

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
    public static Map<String, Double> sortGradeMapping(Map<String, Double> gradeMap) {

        // we only ever order by bottom percents now
        DoubleComparator doubleComparator = new DoubleComparator(gradeMap);
        Map<String, Double> rval = new TreeMap<>(doubleComparator);
        rval.putAll(gradeMap);

        return rval;
    }

    @Override
    public int compareTo(Object o) {

        GradeMapping other = (GradeMapping) o;
        return new CompareToBuilder().append(getName(), other.getName()).toComparison();
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
}
