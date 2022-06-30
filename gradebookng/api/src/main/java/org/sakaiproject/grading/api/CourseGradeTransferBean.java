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
package org.sakaiproject.grading.api;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the course grade that contains the the calculated grade (ie 46.67), the mapped grade (ie F) and any entered grade override (ie D-).
 */
@Getter @Setter
public class CourseGradeTransferBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String enteredGrade;
    private String calculatedGrade;
    private Double enteredPoints;
    private String mappedGrade;
    private Double pointsEarned;
    private Double totalPointsPossible;
    private Date dateRecorded;
    private String autoCalculatedGrade;
    private String displayString;

    /**
     * Helper to get a grade override preferentially, or fallback to the standard mapped grade.
     * @return
     */
    public String getDisplayGrade() {
        return StringUtils.isNotBlank(enteredGrade) ? enteredGrade : mappedGrade;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
