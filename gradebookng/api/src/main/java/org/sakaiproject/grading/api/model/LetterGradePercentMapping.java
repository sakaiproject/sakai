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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GB_LETTERGRADE_PERCENT_MAPPING", uniqueConstraints = {
    @UniqueConstraint(name = "uniqueTypeGradebook", columnNames = {"MAPPING_TYPE", "GRADEBOOK_ID"})
})
@Getter @Setter
public class LetterGradePercentMapping implements PersistableEntity<Long>, Serializable {

    @Id
    @Column(name = "LGP_MAPPING_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "gb_lettergrade_percent_mapping_id_sequence")
    @SequenceGenerator(name = "gb_lettergrade_percent_mapping_id_sequence", sequenceName = "GB_LETTER_MAPPING_S")
    private Long id;

    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    @Column(name = "GRADEBOOK_ID", nullable = true)
    private Long gradebookId;

    @Column(name = "MAPPING_TYPE", nullable = false)
    private Integer mappingType = 1; //value of 1 or 2 - 1 is the default mapping in the system.

    @ElementCollection
    @CollectionTable(name = "GB_LETTERGRADE_MAPPING", joinColumns = @JoinColumn(name = "LG_MAPPING_ID"))
    @MapKeyColumn(name = "grade")
    @Column(name = "value")
    private Map<String, Double> gradeMap;

    public Double getValue(String grade)  {

        if (gradeMap != null && gradeMap.containsKey(grade)) {
            return gradeMap.get(grade);
        }
        return null;
    }

    public String getGrade(Double value)  {

        if (gradeMap != null) {
            List percentList = new ArrayList();
            for (Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();) {
                percentList.add(gradeMap.get((iter.next())));
            }
            Collections.sort(percentList);
            for (int i=0; i<percentList.size(); i++) {
                Double mappingDouble = (Double)percentList.get(percentList.size() - 1 - i);
                if (mappingDouble.compareTo(value) <= 0) {
                    return getGradeMapping(mappingDouble);
                }
            }

            //return the last grade if double value is less than the minimum value in gradeMapping - "F"
            return getGradeMapping(((Double)percentList.get(percentList.size() - 1)));
        }
        return null;
    }

    /*
     * this method returns the mapping letter grade value for Double value
     * according to the exact pair of key-value in gradeMap.
     */
    private String getGradeMapping(Double value)  {

        if (gradeMap != null) {
            for (String key : gradeMap.keySet()) {
                Double gradeValue = gradeMap.get(key);
                if (gradeValue.equals(value)) {
                    return key;
                }
            }
            return null;
        }
        return null;
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
        for (String grade : gradeMap.keySet()) {
            if (grade.equalsIgnoreCase(inputGrade)) {
                standardizedGrade = grade;
                break;
            }
        }
        return standardizedGrade;
    }
}
