/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.grading.api;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JavaBean to hold data associated with a Gradebook assignment. The Course Grade is not considered an assignment.
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter @Setter
public class Assignment implements Serializable, Comparable<Assignment> {

    /**
     * @return Returns the name of the assignment. The assignment name is unique among currently defined assignments. However, it is not a
     *         safe UID for persistance, since an assignment can be renamed. Also, an assignment can be deleted and a new assignment can be
     *         created re-using the old name.
     */
    private String name;

    /**
     * @return Returns the gradable reference of the assignment. This might be null, but, if not, it
     * represents thing being graded, an assignemnt in the Assignments tool perhaps.
     */
    private String reference;

    /**
     *
     * @return Returns the ID of the assignment in the gradebook
     */
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * the total points the assignment is worth.
     */
    private Double points;

    /**
     * the maximum letter grade for letter-grade entry mode, or null if not applicable.
     */
    private String maxLetterGrade;

    /**
     * the due date for the assignment, or null if none is defined.
     */
    private Date dueDate;

    /**
     * true if the assignment is maintained by some software other than the Gradebook itself.
     */
    @JsonProperty(value = "externallyMaintained")
    private Boolean externallyMaintained = Boolean.FALSE;

    /**
     * the external id, or null if the assignment is maintained by the Gradebook
     */
    private String externalId;

    /**
     * the external app name, or null if the assignment is maintained by the Gradebook
     */
    private String externalAppName;

    /**
     * the external tool title, or null if the assignment is maintained by the Gradebook. Not persisted.
     */
    private String externalToolTitle;

    /**
     * the external data, or null if the assignment is maintained by the Gradebook
     */
    private String externalData;

    /**
     * true if the assignment has been released for view to students
     */
    @JsonProperty(value = "released")
    private Boolean released = Boolean.FALSE;

    /**
     * Note that any calls setSortOrder will not be persisted, if you want to change the sort order of an assignment you must call
     * GradingService.updateAssignmentOrder as that method properly handles the reordering of all other assignments for the gradebook.
     */
    private Integer sortOrder;
    private Boolean counted = Boolean.FALSE;
    private String categoryName;
    private Double weight;
    private Boolean ungraded = Boolean.FALSE;
    @JsonProperty(value = "extraCredit")
    private Boolean extraCredit = Boolean.FALSE;
    private Boolean categoryExtraCredit = Boolean.FALSE;
    private Boolean categoryEqualWeight = Boolean.FALSE;
    private Long categoryId;
    private Integer categoryOrder;
    private Integer categorizedSortOrder;
    private String lineItem;
    @Getter
    @Setter
    private boolean createTask;

    /**
     * For editing. Not persisted.
     */
    @Getter
    @Setter
    private Boolean scaleGrades = Boolean.FALSE;
    @Getter
    @Setter
    private String context;
    @Getter
    @Setter
    private String gradebookId;
    @Getter
    @Setter
    private String gradebookUid;

    @Override
    public int compareTo(final Assignment o) {
        return new CompareToBuilder()
                .append(this.id, o.id)
                .toComparison();
    }
}
