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
package org.sakaiproject.service.gradebook.shared;

import org.apache.commons.lang3.StringUtils;

public class GradebookHelper {

    /**
     * Validate a grade item title by checking against the reserved characters
     * @param title
     * @throws InvalidGradeItemNameException
     */
    public static void validateGradeItemName(final String title) throws InvalidGradeItemNameException {
        if (StringUtils.isBlank(title)
            || StringUtils.startsWithAny(title, GradebookService.INVALID_CHARS_AT_START_OF_GB_ITEM_NAME)) {
            throw new InvalidGradeItemNameException("Grade Item name is invalid: " + title);
        }
    }

    /**
     * Validate assignment points and name is valid
     * @param assignmentDefition
     * @throws InvalidGradeItemNameException
     * @throws AssignmentHasIllegalPointsException
     * @throws ConflictingAssignmentNameException
     */
    
	public static void validateAssignmentNameAndPoints(final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) 
		throws InvalidGradeItemNameException, AssignmentHasIllegalPointsException, ConflictingAssignmentNameException {
		// Ensure that points is > zero.
		final Double points = assignmentDefinition.getPoints();
		if ((points == null) || (points <= 0)) {
			throw new AssignmentHasIllegalPointsException("Points must be > 0");
		}	
		
		// validate the name
		final String validatedName = StringUtils.trimToNull(assignmentDefinition.getName());
		if (validatedName == null) {
			throw new ConflictingAssignmentNameException("You cannot save an assignment without a name");
		}

		// name cannot contain these chars as they are reserved for special columns in import/export
		validateGradeItemName(validatedName);
		
	}
}
