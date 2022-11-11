/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GradeDefinition {

    private String studentUid;
    private String graderUid;
    private Date dateRecorded;
    private String grade;
    private String gradeComment;
    private GradeType gradeEntryType;
    private boolean gradeReleased;
    private boolean excused;
}
