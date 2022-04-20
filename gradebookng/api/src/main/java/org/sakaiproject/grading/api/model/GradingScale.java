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
import java.util.List;
import java.util.Map;

import org.sakaiproject.grading.api.GradingScaleDefinition;
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Index;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "GB_GRADING_SCALE_T")
@DiscriminatorColumn(name = "OBJECT_TYPE_ID")
@DiscriminatorValue("0")
@Getter @Setter
@ToString(onlyExplicitlyIncluded = true)
public class GradingScale implements PersistableEntity<Long>, Comparable<Object>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "VERSION")
    private Integer version = 1;

    @Column(name = "SCALE_UID", unique = true, nullable = false)
    @ToString.Include
    private String uid;

    @Column(name = "NAME", nullable = false)
    private String name;

    /**
     * Because the Gradebook now supports non-calculated manual-only grades with
     * no percentage equivalent, it is possible for the list of grades to include
     * codes that are not included in the defaultBottomPercents map. In other
     * words, callers shouldn't expect getDefaultBottomPercents.keySet() to be
     * equivalent to this list.
     * @return list of supported grade codes, ordered from highest to lowest
     */
    @ElementCollection
    @CollectionTable(name = "GB_GRADING_SCALE_GRADES_T", joinColumns = @JoinColumn(name = "GRADING_SCALE_ID"), indexes = @Index(columnList = "GRADING_SCALE_ID"))
    @OrderColumn(name = "GRADE_IDX")
    @Column(name = "LETTER_GRADE", nullable = false)
    private List<String> grades;

    @ElementCollection
    @CollectionTable(name = "GB_GRADING_SCALE_PERCENTS_T", joinColumns = @JoinColumn(name = "GRADING_SCALE_ID"))
    @MapKeyColumn(name = "LETTER_GRADE")
    @Column(name = "PERCENT")
    private Map<String, Double> defaultBottomPercents;  // From grade to percentage

    @Column(name = "UNAVAILABLE")
    private Boolean unavailable = Boolean.FALSE;

    @Override
    public int compareTo(Object o) {
        return name.compareTo(((GradingScale) o).getName());
    }

    /**
     * Convert this GradeingScale instance to a GradingScaleDefinition
     * @return
     */
    public GradingScaleDefinition toGradingScaleDefinition() {

        GradingScaleDefinition scaleDef = new GradingScaleDefinition();
        scaleDef.setUid(this.getUid());
        scaleDef.setName(this.getName());

        Map<String, Double> mapBottomPercents = this.getDefaultBottomPercents();
        scaleDef.setDefaultBottomPercents(mapBottomPercents);

        //build the bottom percents as a list as well
        List<Object> listBottomPercents = new ArrayList<>();
        List<String> grades = new ArrayList<>();
        for (Map.Entry<String, Double> pair : mapBottomPercents.entrySet()) {
            listBottomPercents.add(pair.getValue());
            grades.add(pair.getKey());
        }
        scaleDef.setGrades(grades);
        scaleDef.setDefaultBottomPercentsAsList(listBottomPercents);

        return scaleDef;
    }
}
