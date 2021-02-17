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
import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.service.gradebook.shared.GradingEventStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A log of grading activity.  A GradingEvent should be saved any time a grade
 * record is added or modified.  GradingEvents should be added when the entered
 * value of a course grade record is added or modified, but not when the
 * autocalculated value changes.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GradingEvent implements Serializable {

	private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private Long id;
    @EqualsAndHashCode.Include
    private String graderId;
    @EqualsAndHashCode.Include
    private String studentId;
    private GradableObject gradableObject;
    private String grade;
    @EqualsAndHashCode.Include
    private Date dateGraded = new Date();
    private GradingEventStatus status;

    public static final Comparator<GradingEvent> compareByDateGraded = Comparator.comparing(GradingEvent::getDateGraded);

    public GradingEvent(GradableObject gradableObject, String graderId, String studentId, Object grade) {
        this.gradableObject = gradableObject;
        this.graderId = graderId;
        this.studentId = studentId;
        if (grade != null) {
        	this.grade = grade.toString();
        }
        this.status = GradingEventStatus.GRADE_NONE;
    }
}

