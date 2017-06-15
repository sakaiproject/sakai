package org.sakaiproject.service.gradebook.shared;

import org.apache.commons.lang3.StringUtils;

public class GradebookHelper {

    /**
     * Validate a grade item title by checking against the reserved characters
     * @param title
     * @throws InvalidGradeItemNameException
     */
    public static void validateGradeItemName(String title) throws InvalidGradeItemNameException {
        if (StringUtils.isBlank(title)
            || StringUtils.containsAny(title, GradebookService.INVALID_CHARS_IN_GB_ITEM_NAME)
            || StringUtils.startsWithAny(title, GradebookService.INVALID_CHARS_AT_START_OF_GB_ITEM_NAME)) {
            throw new InvalidGradeItemNameException("Grade Item name is invalid: " + title);
        }
    }
}
