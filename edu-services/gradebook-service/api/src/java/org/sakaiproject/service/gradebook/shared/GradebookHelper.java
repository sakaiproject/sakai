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
}
