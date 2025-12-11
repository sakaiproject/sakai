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

import org.apache.commons.lang3.StringUtils;

public class GradebookHelper {

    /**
     * Validate a grade item title by checking against the reserved characters
     * @param title
     * @throws InvalidGradeItemNameException
     * @throws ConflictingAssignmentNameException
     * returns validatedName
     */
    public static String validateGradeItemName(String title) throws InvalidGradeItemNameException, ConflictingAssignmentNameException {
        // validate the name
        title = StringUtils.trimToNull(title);
        if (StringUtils.isBlank(title)) {
            throw new ConflictingAssignmentNameException("You cannot save an assignment without a name");
        }
        else if (StringUtils.startsWithAny(title, GradingService.INVALID_CHARS_AT_START_OF_GB_ITEM_NAME)) {
            throw new InvalidGradeItemNameException("Grade Item name is invalid: " + title);
        }
        return title;
    }

    /**
     * Validate assignment points and name is valid
     * @param assignmentDefition
     * @param gradeType
     * @throws InvalidGradeItemNameException
     * @throws AssignmentHasIllegalPointsException
     * @throws ConflictingAssignmentNameException
     * @return validated name
     */

    public static String validateAssignmentNameAndPoints(Assignment assignmentDefinition, GradeType gradeType)
        throws InvalidGradeItemNameException, AssignmentHasIllegalPointsException, ConflictingAssignmentNameException {

        if (gradeType != GradeType.LETTER) {
            // Ensure that points is > zero.
            final Double points = assignmentDefinition.getPoints();
            if ((points == null) || (points <= 0)) {
                throw new AssignmentHasIllegalPointsException("Points must be > 0");
            }
        }

        return validateGradeItemName(assignmentDefinition.getName());
    }
}
